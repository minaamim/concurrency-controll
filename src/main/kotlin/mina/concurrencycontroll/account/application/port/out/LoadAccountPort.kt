package mina.concurrencycontroll.account.application.port.out

import mina.concurrencycontroll.account.domain.Account

interface LoadAccountPort {
    fun loadAccount(accountId: Long) : Account?
}