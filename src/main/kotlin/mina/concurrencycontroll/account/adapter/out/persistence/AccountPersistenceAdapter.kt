package mina.concurrencycontroll.account.adapter.out.persistence

import mina.concurrencycontroll.account.application.port.out.LoadAccountPort
import mina.concurrencycontroll.account.application.port.out.UpdateAccountStatePort
import mina.concurrencycontroll.account.domain.Account
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class AccountPersistenceAdapter(
    private val accountRepository: AccountRepository
) : LoadAccountPort, UpdateAccountStatePort {
    override fun loadAccount(accountId: Long): Account? = accountRepository.findByIdOrNull(accountId)

    override fun updateAccount(account: Account) {
        accountRepository.save(account)
    }
}