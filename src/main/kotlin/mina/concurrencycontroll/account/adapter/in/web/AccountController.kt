package mina.concurrencycontroll.account.adapter.`in`.web

import mina.concurrencycontroll.account.application.port.`in`.*
import org.springframework.web.bind.annotation.*

@RestController
class AccountController(
    private val balanceUseCase: BalanceUseCase,
    private val transferUseCase: TransferUseCase,
    private val depositUseCase: DepositUseCase,
    private val withdrawUseCase: WithdrawUseCase
) {

    /**
     * 잔고 조회
     */
    @GetMapping("{id}/balance")
    fun loadAccount(@PathVariable id: Long): Long {
        return balanceUseCase.balance(id)
    }

    /**
     *  계좌 이체
     */
    @PostMapping("{sourceAccountId}/transfer")
    fun transfer(@PathVariable sourceAccountId: Long, @RequestBody targetAccountId: Long, amount: Long): Boolean {
        val command = TransferCommand(sourceAccountId, targetAccountId, amount)
        return transferUseCase.transfer(command)
    }

    /**
     * 입금
     */
    @PostMapping("{accountId}/deposit")
    fun deposit(@PathVariable accountId: Long, @RequestBody amount: Long): Boolean {
        val command = DepositCommand(accountId, amount)
        return depositUseCase.deposit(command)
    }

    /**
     *  잔고 출금
     */
    @PostMapping("{id}/withdraw")
    fun withdraw(@PathVariable accountId: Long, @RequestBody amount: Long): Boolean {
        val command = WithdrawCommand(accountId, amount)
        return withdrawUseCase.withdraw(command)
    }
}