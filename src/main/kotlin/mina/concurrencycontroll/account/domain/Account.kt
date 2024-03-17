package mina.concurrencycontroll.account.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import mina.concurrencycontroll.global.exception.BusinessException
import mina.concurrencycontroll.global.exception.ErrorCode
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
class Account(
    var balance: Long,
    @UpdateTimestamp var updatedAt: LocalDateTime,
    @CreationTimestamp var createdAt: LocalDateTime,
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null
) {
//    fun transfer(targetAccount: Account, amount: Long): Boolean {
//        mayWithdraw(amount)
//        balance -= amount
//        targetAccount.balance += amount
//        return true
//    }

    fun deposit(amount: Long) {
        balance += amount
    }

    fun withdraw(amount: Long): Boolean {
        mayWithdraw(amount)
        balance -= amount
        return true
    }

    private fun mayWithdraw(amount: Long) {
        if (balance - amount < 0) throw BusinessException(ErrorCode.LACK_OF_BALANCE)
    }
}