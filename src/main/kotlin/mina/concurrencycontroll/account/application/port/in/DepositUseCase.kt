package mina.concurrencycontroll.account.application.port.`in`

interface DepositUseCase {
    fun deposit(depositCommand: DepositCommand): Boolean
}