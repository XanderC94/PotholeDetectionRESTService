package core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST)
public class EmptyTokenException extends Exception {

    public EmptyTokenException(String message) {
        super(message);
    }
}
