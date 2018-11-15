package core.exceptions;

public class AlreadyRegisteredException extends Exception {

    public AlreadyRegisteredException(String token) {
        super(String.format("Token %s already registered!", token));
    }
    public AlreadyRegisteredException(String token, Integer id) {
        super(String.format("Token %s already registered for ID %s!", token, id));
    }
}
