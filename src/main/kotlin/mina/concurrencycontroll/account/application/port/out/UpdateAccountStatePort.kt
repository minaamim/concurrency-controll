package mina.concurrencycontroll.account.application.port.out

import mina.concurrencycontroll.account.domain.Account

interface UpdateAccountStatePort {
    fun updateAccount(account: Account)
}