package mina.concurrencycontroll.account.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import io.mockk.verify
import mina.concurrencycontroll.ConcurrencyControllApplicationTests
import mina.concurrencycontroll.account.application.port.`in`.DepositCommand
import mina.concurrencycontroll.account.application.port.out.LoadAccountPort
import mina.concurrencycontroll.account.application.port.out.UpdateAccountStatePort
import mina.concurrencycontroll.account.domain.Account
import mina.concurrencycontroll.global.exception.BusinessException
import mina.concurrencycontroll.global.exception.ErrorCode
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

private val loadAccountPort: LoadAccountPort = mockk()
private val updateAccountStatePort: UpdateAccountStatePort = mockk(relaxed = true)

@InjectMockKs
val depositService: DepositService = DepositService(loadAccountPort, updateAccountStatePort)

@SpringBootTest(classes = [ConcurrencyControllApplicationTests::class])
@ActiveProfiles("test")
class DepositServiceTest : BehaviorSpec({

    val log = LoggerFactory.getLogger(this::class.java)

    Given("입금 계좌와 금액이 주어진다") {
        val accountId = 1L
        val amount = 10L

        val account = Account(
            id = 1L,
            balance = 0L,
            updatedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        every { loadAccountPort.loadAccount(accountId) } returns account

        When("송금 서비스 호출") {
            val result = depositService.deposit(DepositCommand(accountId, amount))
            Then("결과 확인") {
                result shouldBe true
                account.balance shouldBeEqual 10L
                verify(exactly = 1) { updateAccountStatePort.updateAccount(account) }
            }
        }
    }

    Given("입금 계좌가 존재하지 않는 상황일 때") {
        val accountId = -1L
        val amount = 0L

        every { loadAccountPort.loadAccount(accountId) } returns null

        When("송금 서비스를 호출하면") {
            val exception = shouldThrow<BusinessException> {
                depositService.deposit(DepositCommand(accountId, amount))
            }

            Then("예외가 발생해야 한다.") {
                log.info("[입금 계좌가 존재하지 않을 때] ${exception.errorCode.message}")
                exception.errorCode shouldBe ErrorCode.ACCOUNT_NOT_FOUND
            }
        }
    }

    Given("송금이 동시에 여러 번 이루어지는 상황에서") {
        val executorService = Executors.newFixedThreadPool(20)
        val count = 500
        val countDownLatch = CountDownLatch(count)

        val accountId = 1L
        val amount = 1L

        val account = Account(
            id = 1L,
            balance = 0L,
            updatedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        every { loadAccountPort.loadAccount(accountId) } returns account

        When("송금 서비스 호출") {
            repeat(count) {
                executorService.submit {
                    try {
                        depositService.deposit(DepositCommand(accountId, amount))
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }

            countDownLatch.await()

            Then("결과 확인") {
                account.balance shouldBeEqual 500L
                verify(exactly = 100) { updateAccountStatePort.updateAccount(account) }
            }
        }
    }
})
