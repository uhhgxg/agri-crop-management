package com.xxx.agri.exception;

import com.xxx.agri.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleBodyValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.joining("；"));
        return badRequest(message, exception);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Result<Void>> handleMethodValidation(HandlerMethodValidationException exception) {
        if (exception.isForReturnValue()) {
            log.error("返回值校验失败", exception);
            return response(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误，请稍后重试");
        }
        String message = exception.getParameterValidationResults().stream()
                .map(ParameterValidationResult::getResolvableErrors)
                .flatMap(Collection::stream)
                .map(MessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.joining("；"));
        return badRequest(message, exception);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return badRequest("参数" + exception.getName() + "格式不正确", exception);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleUnreadableBody(HttpMessageNotReadableException exception) {
        return badRequest("请求体必须是有效的JSON，且字段类型正确", exception);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException exception) {
        log.error("业务处理失败", exception);
        return response(exception.getStatus(), exception.getMessage());
    }

    private static ResponseEntity<Result<Void>> handleHttpError(ErrorResponse error, Exception exception) {
        int code = error.getStatusCode().value();
        log.error("HTTP请求处理失败 status={}", code, exception);
        String message = switch (code) {
            case 404 -> "接口不存在";
            case 405 -> "请求方式不受支持";
            case 415 -> "请求内容类型不受支持";
            default -> code >= 500 ? "服务器内部错误，请稍后重试" : "请求无效";
        };
        return ResponseEntity.status(code).headers(error.getHeaders()).body(Result.failure(code, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnknown(Exception exception, HttpServletRequest request) {
        if (exception instanceof ErrorResponse error) {
            return handleHttpError(error, exception);
        }
        log.error("未处理的异常 method={} path={}", request.getMethod(), request.getRequestURI(), exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误，请稍后重试");
    }

    private static ResponseEntity<Result<Void>> badRequest(String message, Exception exception) {
        log.error("参数校验失败", exception);
        return response(HttpStatus.BAD_REQUEST, message.isBlank() ? "参数校验失败" : message);
    }

    private static ResponseEntity<Result<Void>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Result.failure(status.value(), message));
    }
}
