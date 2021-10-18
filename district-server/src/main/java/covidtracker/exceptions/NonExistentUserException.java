package covidtracker.exceptions;

public class NonExistentUserException extends Exception {

    public NonExistentUserException() {
        super("This user does not exist");
    }
}
