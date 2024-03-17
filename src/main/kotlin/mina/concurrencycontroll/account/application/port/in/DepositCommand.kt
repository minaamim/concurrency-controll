package mina.concurrencycontroll.account.application.port.`in`

import jakarta.validation.constraints.Min

class DepositCommand(
    val accountId: Long,
    @field: Min(1L)
    val amount: Long
) {
}