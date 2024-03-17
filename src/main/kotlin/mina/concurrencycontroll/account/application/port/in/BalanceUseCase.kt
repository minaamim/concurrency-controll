package mina.concurrencycontroll.account.application.port.`in`

interface BalanceUseCase {
    fun balance(accountId: Long) : Long
}