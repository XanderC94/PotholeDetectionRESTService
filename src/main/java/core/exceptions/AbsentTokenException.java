package core.exceptions;

public class AbsentTokenException extends Exception {
    public AbsentTokenException(String token) {
        super(String.format("Token %s is absent or unregistered. Please Register the token first.", token));
    }
}
