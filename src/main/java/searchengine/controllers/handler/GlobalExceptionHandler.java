package searchengine.controllers.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import searchengine.dto.message.ErrorMessage;
import searchengine.exception.IndexingException;
import searchengine.exception.SearchingException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IndexingException.class)
    public ResponseEntity<ErrorMessage> handleIndexingException(IndexingException e) {
        log.error("Произошла ошибка индексации: {}", e.getMessage());
        return getErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SearchingException.class)
    public ResponseEntity<ErrorMessage> handleSearchingException(SearchingException e) {
        log.error("Произошла ошибка поиска: {}", e.getMessage());
        return getErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGeneralException(Exception e) {
        log.error("Произошла непредвиденная ошибка: ", e);
        return getErrorResponse("Ошибка выполнения " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorMessage> getErrorResponse(String message, HttpStatus status) {
        ErrorMessage errorMessage = new ErrorMessage(message);
        return new ResponseEntity<>(errorMessage, status);
    }
}
