package mina.concurrencycontroll.account.application.port.`in`

interface WithdrawUseCase {
    fun withdraw(Command: WithdrawCommand): Boolean
}