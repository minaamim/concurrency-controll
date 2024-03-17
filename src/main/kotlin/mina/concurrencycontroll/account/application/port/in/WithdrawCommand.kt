package mina.concurrencycontroll.account.application.port.`in`

class WithdrawCommand(
    val accountId: Long,
    val amount: Long
) {
}