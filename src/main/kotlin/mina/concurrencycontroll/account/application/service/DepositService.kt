package mina.concurrencycontroll.account.application.service

import mina.concurrencycontroll.account.application.port.`in`.DepositCommand
import mina.concurrencycontroll.account.application.port.`in`.DepositUseCase
import mina.concurrencycontroll.account.application.port.out.LoadAccountPort
import mina.concurrencycontroll.account.application.port.out.UpdateAccountStatePort
import mina.concurrencycontroll.global.exception.BusinessException
import mina.concurrencycontroll.global.exception.ErrorCode
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class DepositService(
    private val loadAccountPort: LoadAccountPort,
    private val updateAccountStatePort: UpdateAccountStatePort,
    private val redissonClient: RedissonClient
) : DepositUseCase {

    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val WAIT_TIME: Long = 10L
        private const val RELEASE_TIME: Long = 1L
    }

    override fun deposit(depositCommand: DepositCommand): Boolean {
        val account = loadAccountPort.loadAccount(depositCommand.accountId)
            ?: throw BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)

        val lock = redissonClient.getLock("deposit:account:${depositCommand.accountId}")

        try {
            val available = lock.tryLock(WAIT_TIME, RELEASE_TIME, TimeUnit.SECONDS)
            if (!available) {
                log.info("[deposit] account getLock timeout")
                throw IllegalAccessException()
            }
            account.deposit(depositCommand.amount)
                .also { log.info("[withdraw] exec - id: $account.id, amount: ${depositCommand.amount}, balance: $it") }
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