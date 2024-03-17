package mina.concurrencycontroll.account.domain

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import mina.concurrencycontroll.ConcurrencyControllApplicationTests
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@SpringBootTest(classes = [ConcurrencyControllApplicationTests::class])
@ActiveProfiles("test")
class AccountTest : BehaviorSpec({

    val log = LoggerFactory.getLogger(this::class.java)

    /**
     * withdraw test
     */
    Given("출금 시도") {
        val sourceAccount = Account(111L, LocalDateTime.now(), LocalDateTime.now())
        When("도메인의 withdraw 함수를 호출한다") {
            val result = sourceAccount.withdraw(11L)
            Then("출금에 성공한다") {
                result shouldBe true
                sourceAccount.balance shouldBeGreaterThanOrEqual 0
                sourceAccount.balance shouldBeExactly 100L
                log.info("[account domain test] sourceAccount balance: ${sourceAccount.balance}")
            }
        }
    }

    Given("입금 시도") {
        val sourceAccount = Account(111L, LocalDateTime.now(), LocalDateTime.now())
        When("도메인의  deposit 함수를 호출한다") {
            sourceAccount.deposit(11L)
            Then("입금에 성공한다") {
                sourceAccount.balance shouldBeGreaterThanOrEqual 0
                sourceAccount.balance shouldBeExactly 122L
                log.info("[account domain test] sourceAccount balance: ${sourceAccount.balance}")
            }
        }
    }
})
