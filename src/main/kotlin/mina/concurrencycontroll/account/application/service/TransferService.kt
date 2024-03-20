package mina.concurrencycontroll.account.application.service

import mina.concurrencycontroll.account.application.port.`in`.TransferCommand
import mina.concurrencycontroll.account.application.port.`in`.TransferUseCase
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
class TransferService(
    private val loadAccountPort: LoadAccountPort,
    private val updateAccountStatePort: UpdateAccountStatePort,
    private val redissonClient: RedissonClient
) : TransferUseCase {
    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val WAIT_TIME: Long = 10L
        private const val RELEASE_TIME: Long = 1L
    }

    override fun transfer(command: TransferCommand): Boolean {
        val sourceAccount =
            loadAccountPort.loadAccount(command.sourceAccountId) ?: throw BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)
        val targetAccount =
            loadAccountPort.loadAccount(command.targetAccountId) ?: throw BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)

        val sourceLock: RLock = redissonClient.getLock("transfer:source:${command.sourceAccountId}")
        val targetLock: RLock = redissonClient.getLock("transfer:target:${command.targetAccountId}")

        try {
            val sourceAvailable = sourceLock.tryLock(WAIT_TIME, RELEASE_TIME, TimeUnit.SECONDS)
            if (!sourceAvailable) {
                log.info("[withdraw] source account getLock timeout")
                throw IllegalAccessException()
            }

            val targetAvailable = targetLock.tryLock(WAIT_TIME, RELEASE_TIME, TimeUnit.SECONDS)
            if (!targetAvailable) {
                log.info("[deposit] target account getLock timeout")
                throw IllegalAccessException()
            }

            sourceAccount.withdraw(command.amount)
                .also { log.info("[withdraw] exec - id: $sourceAccount.id, amount: ${command.amount}, balance: $it") }
            targetAccount.deposit(command.amount)
                .also { log.info("[deposit] exec - id: $targetAccount.id, amount: ${command.amount}, balance: $it") }

            updateAccountStatePort.updateAccount(sourceAccount)
            updateAccountStatePort.updateAccount(targetAccount)
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        } finally {
            if (sourceLock.isLocked && sourceLock.isHeldByCurrentThread && targetLock.isLocked && targetLock.isHeldByCurrentThread) {
                sourceLock.unlock()
                targetLock.unlock()

            } else {
                log.error("[transfer] IllegalThreadStateException occurred")
                throw IllegalThreadStateException()
            }
        }
        return true
    }
}