package mina.concurrencycontroll.account.application.service

import mina.concurrencycontroll.account.application.port.`in`.DepositCommand
import mina.concurrencycontroll.account.application.port.`in`.DepositUseCase
import mina.concurrencycontroll.account.application.port.out.LoadAccountPort
import mina.concurrencycontroll.account.application.port.out.UpdateAccountStatePort
import mina.concurrencycontroll.global.exception.BusinessException
import mina.concurrencycontroll.global.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class DepositService(
    private val loadAccountPort: LoadAccountPort,
    private val updateAccountStatePort: UpdateAccountStatePort
) : DepositUseCase {
    override fun deposit(depositCommand: DepositCommand): Boolean {
        val account = loadAccountPort.loadAccount(depositCommand.accountId)
            ?: throw BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)
        account.deposit(depositCommand.amount)
        updateAccountStatePort.updateAccount(account)
        return true
    }
}