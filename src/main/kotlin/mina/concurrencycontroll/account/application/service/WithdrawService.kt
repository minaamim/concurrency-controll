package mina.concurrencycontroll.account.application.service

import mina.concurrencycontroll.account.application.port.`in`.WithdrawCommand
import mina.concurrencycontroll.account.application.port.`in`.WithdrawUseCase
import mina.concurrencycontroll.account.application.port.out.LoadAccountPort
import mina.concurrencycontroll.account.application.port.out.UpdateAccountStatePort
import mina.concurrencycontroll.global.exception.BusinessException
import mina.concurrencycontroll.global.exception.ErrorCode
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class WithdrawService(
    private val loadAccountPort: LoadAccountPort,
    private val updateAccountStatePort: UpdateAccountStatePort,
    private val redissonClient: RedissonClient
) : WithdrawUseCase {

    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val WAIT_TIME: Long = 10L
        private const val RELEASE_TIME: Long = 1L
    }

    override fun withdraw(command: WithdrawCommand): Boolean {
        val account =
            loadAccountPort.loadAccount(command.accountId) ?: throw BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)

        val lock: RLock = redissonClient.getLock("withdraw:account:${command.accountId}")

        try {
            val available = lock.tryLock(WAIT_TIME, RELEASE_TIME, TimeUnit.SECONDS)
            if (!available) {
                log.info("[withdraw] account getLock timeout")
                throw IllegalAccessException()
            }

            account.withdraw(command.amount)
                .also { log.info("[withdraw] exec - id: $account.id, amount: ${command.amount}, balance: $it") }
            updateAccountStatePort.updateAccount(account)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } finally {
            if (lock.isLocked && lock.isHeldByCurrentThread) {
                lock.unlock()
            } else {
                log.error("[withdraw] IllegalThreadStateException occurred")
                throw IllegalThreadStateException()
            }
        }
        return true
    }
}