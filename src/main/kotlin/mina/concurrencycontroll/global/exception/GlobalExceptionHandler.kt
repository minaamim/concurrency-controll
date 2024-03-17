package mina.concurrencycontroll.global.exception

import mina.concurrencycontroll.global.exception.response.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    val log = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(value = [BusinessException::class])
    fun handlingBusinessException(ex: BusinessException): ResponseEntity<ErrorResponse> {
        val errorCode: ErrorCode = ex.errorCode
        val errorResponse = ErrorResponse(errorCode = errorCode.name, message = errorCode.message)
        return ResponseEntity(errorResponse, errorCode.status)
    }

}