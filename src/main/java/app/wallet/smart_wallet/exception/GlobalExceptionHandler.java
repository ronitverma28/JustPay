package app.wallet.smart_wallet.exception;

import com.smartwallet.dto.ApiResponse;
import com.smartwallet.util.ApiResponseUtil;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(ApiResponseUtil.error("Validation failed", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(ApiResponseUtil.error("Validation failed", ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseUtil.error(ex.getMessage(), null));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ApiResponse<String>> handleForbidden(ForbiddenOperationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponseUtil.error(ex.getMessage(), null));
    }

    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<ApiResponse<String>> handleDuplicate(DuplicateTransactionException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseUtil.error(ex.getMessage(), null));
    }

    @ExceptionHandler({BadRequestException.class, InsufficientBalanceException.class, PaymentVerificationException.class})
    public ResponseEntity<ApiResponse<String>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ApiResponseUtil.error(ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponseUtil.error("Unexpected error occurred", ex.getMessage()));
    }
}
