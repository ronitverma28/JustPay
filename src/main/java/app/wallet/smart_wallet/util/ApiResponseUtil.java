package app.wallet.smart_wallet.util;

import com.smartwallet.dto.ApiResponse;
import java.time.LocalDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiResponseUtil {

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
            .success(true)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .data(data)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
