package org.mhh.exception;

import org.mhh.dto.ErrorResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException; // برای پارامترهای ضروری مثل city
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest; // برای دسترسی به جزئیات درخواست

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ۲. Handler برای خطای CityNotFoundException
    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCityNotFoundException(CityNotFoundException ex, WebRequest request) {
        log.warn("City not found: {}", ex.getMessage()); // لاگ کردن با جزئیات کمتر برای خطای قابل انتظار
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(), // 404
                "Not Found",
                ex.getMessage(),
                request.getDescription(false) // مسیر درخواست (بدون query string)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // ۳. Handler برای خطای InvalidInputException
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidInputException(InvalidInputException ex, WebRequest request) {
        log.warn("Invalid input: {}", ex.getMessage());
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(), // 400
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ۴. Handler برای خطای ExternalApiException
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponseDTO> handleExternalApiException(ExternalApiException ex, WebRequest request) {
        log.error("External API error: {}", ex.getMessage(), ex.getCause()); // لاگ کردن با جزئیات بیشتر و cause
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(), // 503 (یا 500 بسته به نوع خطا)
                "Service Unavailable",
                ex.getMessage(), // پیام اصلی exception کافیه، جزئیات در لاگ هست
                request.getDescription(false)
        );
        // می‌تونیم کد وضعیت رو بر اساس cause یا نوع دقیق‌تر ExternalApiException تغییر بدیم
        // مثلاً اگر به خاطر 401 بود، شاید 500 برگردونیم؟ یا همین 503؟
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    // ۵. Handler برای پارامترهای ضروری که ارسال نشده‌اند (مثلاً city)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingParams(MissingServletRequestParameterException ex, WebRequest request) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(), // 400
                "Bad Request",
                message,
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }


    // ۶. Handler عمومی برای بقیه خطاهای پیش‌بینی نشده (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex, WebRequest request) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex); // لاگ کردن کل stack trace
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), // 500
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.", // پیام عمومی به کاربر
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}