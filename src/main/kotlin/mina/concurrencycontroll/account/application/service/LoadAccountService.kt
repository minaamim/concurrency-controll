package mina.concurrencycontroll.account.application.service

import mina.concurrencycontroll.account.application.port.`in`.BalanceUseCase
import mina.concurrencycontroll.account.application.port.out.LoadAccountPort
import mina.concurrencycontroll.global.exception.BusinessException
import mina.concurrencycontroll.global.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class LoadAccountService (
    private val loadAccountPort: LoadAccountPort
) : BalanceUseCase {
    override fun balance(accountId: Long): Long {
        val account = loadAccountPort.loadAccount(accountId) ?: throw BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)
        return account.balance
    }

}