package core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * DBQueryExecutionException is thrown when the db server fail to execute the query
 */
@ResponseStatus(value=HttpStatus.BAD_GATEWAY)
public class DBQueryExecutionException extends RuntimeException{
    public DBQueryExecutionException(String s){
        super(s);
    }
}
