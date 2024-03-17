package mina.concurrencycontroll.account.application.port.`in`

import jakarta.validation.constraints.Min

class TransferCommand(
    val sourceAccountId: Long,
    val targetAccountId: Long,
    @field: Min(1)
    val amount: Long
    ) {
}