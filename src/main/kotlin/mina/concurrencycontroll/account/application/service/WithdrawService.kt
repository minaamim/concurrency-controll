package mina.concurrencycontroll.account.application.service

import mina.concurrencycontroll.account.application.port.`in`.WithdrawCommand
import mina.concurrencycontroll.account.application.port.`in`.WithdrawUseCase
import mina.concurrencycontroll.account.application.port.out.LoadAccountPort
import mina.concurrencycontroll.account.application.port.out.UpdateAccountStatePort
import mina.concurrencycontroll.global.exception.BusinessException
import mina.concurrencycontroll.global.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class WithdrawService(
    private val loadAccountPort: LoadAccountPort,
    private val updateAccountStatePort: UpdateAccountStatePort
) : WithdrawUseCase {
    override fun withdraw(command: WithdrawCommand): Boolean {
        val account =
            loadAccountPort.loadAccount(command.accountId) ?: throw BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)
        val result = account.withdraw(command.amount)
        updateAccountStatePort.updateAccount(account)
        return result
    }
}