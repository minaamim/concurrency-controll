package mina.concurrencycontroll.account.application.service

import mina.concurrencycontroll.account.application.port.`in`.TransferCommand
import mina.concurrencycontroll.account.application.port.`in`.TransferUseCase
import mina.concurrencycontroll.account.application.port.out.LoadAccountPort
import mina.concurrencycontroll.account.application.port.out.UpdateAccountStatePort
import mina.concurrencycontroll.global.exception.BusinessException
import mina.concurrencycontroll.global.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class TransferService(
    private val loadAccountPort: LoadAccountPort,
    private val updateAccountStatePort: UpdateAccountStatePort
) : TransferUseCase {
    override fun transfer(command: TransferCommand): Boolean {
        val sourceAccount =
            loadAccountPort.loadAccount(command.sourceAccountId) ?: throw BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)
        val targetAccount =
            loadAccountPort.loadAccount(command.targetAccountId) ?: throw BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)

        sourceAccount.withdraw(command.amount)
        targetAccount.deposit(command.amount)

        updateAccountStatePort.updateAccount(sourceAccount)
        updateAccountStatePort.updateAccount(targetAccount)
        return true
    }
}