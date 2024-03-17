package mina.concurrencycontroll.account.adapter.out.persistence

import mina.concurrencycontroll.account.adapter.out.persistence.querydsl.AccountQueryRepository
import mina.concurrencycontroll.account.domain.Account
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<Account, Long>, AccountQueryRepository {

}