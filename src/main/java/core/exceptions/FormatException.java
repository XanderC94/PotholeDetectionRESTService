package core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Malformed URI")
public class FormatException extends Exception {

    public FormatException(String message) {
        super(message);
    }
}
