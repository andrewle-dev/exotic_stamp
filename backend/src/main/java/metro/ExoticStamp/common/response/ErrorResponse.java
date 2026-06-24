package metro.ExoticStamp.common.response;

import java.time.LocalDateTime;

public record ErrorResponse(
        String code,
        String message,
        int status,
        String path,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String code, String message, int status, String path) {
        return new ErrorResponse(code, message, status, path, LocalDateTime.now());
    }
}
