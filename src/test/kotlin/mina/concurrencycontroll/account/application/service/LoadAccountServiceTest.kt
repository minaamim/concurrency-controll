package mina.concurrencycontroll.account.application.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import mina.concurrencycontroll.ConcurrencyControllApplicationTests
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [ConcurrencyControllApplicationTests::class])
@ActiveProfiles("test")
class LoadAccountServiceTest(
    private val loadAccountService: LoadAccountService
) : BehaviorSpec({
    val log = LoggerFactory.getLogger(this::class.java)
    Given("계좌 잔액을 조회 시도") {
        val accountId = 1L
        When("잔액 조회 서비스 호출") {
            val balance = loadAccountService.balance(accountId)
            Then("서비스 결과 확인") {
                balance shouldBeGreaterThanOrEqual 0
            }
        }
    }
})