package mina.concurrencycontroll.account.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import io.mockk.verify
import mina.concurrencycontroll.ConcurrencyControllApplicationTests
import mina.concurrencycontroll.account.application.port.`in`.TransferCommand
import mina.concurrencycontroll.account.application.port.out.LoadAccountPort
import mina.concurrencycontroll.account.application.port.out.UpdateAccountStatePort
import mina.concurrencycontroll.account.domain.Account
import mina.concurrencycontroll.global.exception.BusinessException
import mina.concurrencycontroll.global.exception.ErrorCode
import mina.concurrencycontroll.global.redis.RedisConfig
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

private val loadAccountPort: LoadAccountPort = mockk()
private val updateAccountStatePort: UpdateAccountStatePort = mockk(relaxed = true)
private val redisConfig: RedisConfig = RedisConfig()

@InjectMockKs
val transferService: TransferService =
    TransferService(loadAccountPort, updateAccountStatePort, redisConfig.redissonClient())

@SpringBootTest(classes = [ConcurrencyControllApplicationTests::class])
@ActiveProfiles("test")
class TransferServiceTest : BehaviorSpec({

    val log = LoggerFactory.getLogger(this::class.java)

    Given("계좌 이체 시도") {
        val sourceAccountId = 11L
        val targetAccountId = 22L
        val amount = 1L

        val sourceAccount = Account(
            id = 11L,
            balance = 111L,
            updatedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        val targetAccount = Account(
            id = 22L,
            balance = 222L,
            updatedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        every { loadAccountPort.loadAccount(sourceAccountId) } returns sourceAccount
        every { loadAccountPort.loadAccount(targetAccountId) } returns targetAccount

        When("계좌 이체 서비스 호출") {
            transferService.transfer(TransferCommand(sourceAccountId, targetAccountId, amount))
            Then("송신 계좌의 잔액은 송금액만큼 빠져나가야 한다") {
                sourceAccount.balance shouldBeExactly 110L
            }
            Then("수신 계좌의 잔액은 송금액만큼 늘어나야 한다") {
                targetAccount.balance shouldBeExactly 223L
            }
            Then("송금이 반영되어야 한다") {
                verify(exactly = 1) { updateAccountStatePort.updateAccount(sourceAccount) }
                verify(exactly = 1) { updateAccountStatePort.updateAccount(targetAccount) }
            }
        }
    }

    Given("수신/송신 계좌가 존재하지 않는 상황일 때") {
        val accountId = 11L
        val targetAccountId = -1L
        val amount = 1L

        val account = Account(
            id = 11L,
            balance = 111L,
            updatedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        every { loadAccountPort.loadAccount(accountId) } returns account
        every { loadAccountPort.loadAccount(targetAccountId) } returns null

        When("송금하기를 요청하면") {
            val exception = shouldThrow<BusinessException> {
                transferService.transfer(TransferCommand(accountId, targetAccountId, amount))
            }

            Then("예외가 발생해야 한다") {
                exception.errorCode shouldBe ErrorCode.ACCOUNT_NOT_FOUND
            }
        }

        When("송금하기를 요청하면") {
            val exception = shouldThrow<BusinessException> {
                transferService.transfer(TransferCommand(targetAccountId, accountId, amount))
            }

            Then("예외가 발생해야 한다") {
                exception.errorCode shouldBe ErrorCode.ACCOUNT_NOT_FOUND
            }
        }
    }

    Given("계좌이체 요청이 동시에 여러 개 발생한 상황에서") {
        val executorService = Executors.newFixedThreadPool(10)
        val count = 100
        val countDownLatch = CountDownLatch(count)

        val sourceAccountId = 11L
        val targetAccountId = 22L
        val amount = 1L

        val sourceAccount = Account(
            id = 11L,
            balance = 500L,
            updatedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        val targetAccount = Account(
            id = 22L,
            balance = 0L,
            updatedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        every { loadAccountPort.loadAccount(sourceAccountId) } returns sourceAccount
        every { loadAccountPort.loadAccount(targetAccountId) } returns targetAccount

        When("계좌이체 서비스를 호출하면") {
            repeat(count) {
                executorService.submit {
                    try {
                        transferService.transfer(TransferCommand(sourceAccountId, targetAccountId, amount))
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }
            countDownLatch.await()
            Then("송신 계좌의 잔액은 송금액만큼 빠져나가야 한다") {
                log.info("[verify] source account balance: ${sourceAccount.balance}")
                sourceAccount.balance shouldBeExactly 400L
            }
            Then("수신 계좌의 잔액은 송금액만큼 늘어나야 한다") {
                targetAccount.balance shouldBeExactly 100L
            }
            Then("송신이 반영되어야 한다") {
                verify(exactly = 100) { updateAccountStatePort.updateAccount(sourceAccount) }
            }
            Then("수신이 반영되어야 한다") {
                verify(exactly = 100) { updateAccountStatePort.updateAccount(targetAccount) }
            }
        }
    }
})