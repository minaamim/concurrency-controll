package mina.concurrencycontroll.account.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.mockk
import io.mockk.verify
import mina.concurrencycontroll.account.application.port.`in`.WithdrawCommand
import mina.concurrencycontroll.account.application.port.out.LoadAccountPort
import mina.concurrencycontroll.account.application.port.out.UpdateAccountStatePort
import mina.concurrencycontroll.account.domain.Account
import mina.concurrencycontroll.global.exception.BusinessException
import mina.concurrencycontroll.global.exception.ErrorCode
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors


private val loadAccountPort: LoadAccountPort = mockk()
private val updateAccountStatePort: UpdateAccountStatePort = mockk(relaxed = true)

@InjectMockKs
val withdrawService: WithdrawService = WithdrawService(loadAccountPort, updateAccountStatePort)

class WithdrawServiceTest : BehaviorSpec({
    val log = LoggerFactory.getLogger(this::class.java)
    Given("출금 계좌와 금액이 주어진다") {
        val accountId = 1L
        val amount = 10L

        val account = Account(
            id = 1L,
            balance = 10L,
            updatedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        every { loadAccountPort.loadAccount(accountId) } returns account
        When("출금 서비스 호출") {
            val result = withdrawService.withdraw(WithdrawCommand(accountId, amount))
            Then("결과 확인") {
                result shouldBe true
                account.balance shouldBeEqual 0L
                verify(exactly = 1) { updateAccountStatePort.updateAccount(account) }
            }
        }
    }

    Given("출금 계좌가 존재하지 않는 상황에서") {
        val accountId = -1L
        val amount = 0L

        every { loadAccountPort.loadAccount(accountId) } returns null

        When("출금 서비스를 호출했을 때") {
            val exception = shouldThrow<BusinessException> {
                withdrawService.withdraw(WithdrawCommand(accountId, amount))
            }
            Then("예외가 발생해야 한다") {
                log.info("[출금 계좌가 존재하지 않을 때] ${exception.errorCode.message}")
                exception.errorCode shouldBe ErrorCode.ACCOUNT_NOT_FOUND
            }
        }
    }

    Given("출금액이 잔액을 초과하는 상황에서") {
        val accountId = 1L
        val amount = 30L

        val account = Account(
            id = 1L,
            balance = 10L,
            updatedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        every { loadAccountPort.loadAccount(accountId) } returns account
        When("출금 서비스를 호출했을 때") {
            val exception = shouldThrow<BusinessException> {
                withdrawService.withdraw(WithdrawCommand(accountId, amount))
            }
            Then("출금이 불가능해야 한다.") {
                exception.errorCode shouldBe ErrorCode.LACK_OF_BALANCE
                account.balance shouldBeEqual 10L
            }
        }
    }

    Given("출금 요청이 동시에 발생하는 상황에서") {
        val executorService = Executors.newFixedThreadPool(20)
        val count = 500
        val countDownLatch = CountDownLatch(count)

        val accountId = 1L
        val amount = 1L

        val account = Account(
            id = 1L,
            balance = 500L,
            updatedAt = LocalDateTime.now(),
            createdAt = LocalDateTime.now()
        )

        every { loadAccountPort.loadAccount(accountId) } returns account

        When("송금 서비스 호출") {
            repeat(count) {
                executorService.submit {
                    try {
                        withdrawService.withdraw(WithdrawCommand(accountId, amount))
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }
        }

        countDownLatch.await()

        Then("결과 확인") {
            account.balance shouldBeEqual 0L
            verify(exactly = 500) { updateAccountStatePort.updateAccount(account) }
        }
    }

})
