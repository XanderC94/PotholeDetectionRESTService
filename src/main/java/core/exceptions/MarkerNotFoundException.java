package core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.NoSuchElementException;

/**
 * MarkerNotFoundException is thrown when a marker request on the DB return an empty result
 */
@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No markers Found")
public class MarkerNotFoundException extends NoSuchElementException {

    public MarkerNotFoundException(String s){
        super(s);
    }
}

