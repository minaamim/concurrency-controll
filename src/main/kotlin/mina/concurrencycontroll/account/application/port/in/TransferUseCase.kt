package mina.concurrencycontroll.account.application.port.`in`

interface TransferUseCase {
    fun transfer(transferCommand: TransferCommand) : Boolean
}