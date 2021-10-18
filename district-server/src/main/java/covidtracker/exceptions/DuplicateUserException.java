package covidtracker.exceptions;

public class DuplicateUserException extends Exception {

    public DuplicateUserException() {
        super("This username is already registered");
    }
}
