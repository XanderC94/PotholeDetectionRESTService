package core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.UNAUTHORIZED)
public class AbsentTokenException extends Exception {
    public AbsentTokenException(String token) {
        super(String.format("Token %s is absent or unregistered. Please Register the token first.", token));
    }
}
