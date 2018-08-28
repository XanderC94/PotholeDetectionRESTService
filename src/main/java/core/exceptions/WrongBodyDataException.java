package core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * WrongBodyDataException is thrown when the message body is wrong and doesn't pass the check phase
 */
@ResponseStatus(value=HttpStatus.BAD_REQUEST, reason="Message body data was incorrect")
public class WrongBodyDataException extends IllegalArgumentException{
    public WrongBodyDataException(String s){
        super(s);
    }
}
