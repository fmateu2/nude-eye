package persistence.exception;

/**
 * Thrown when a write operation to a persistence resource fails.
 */
public class PersistenceWriteException extends PersistenceException {

    /**
     * Constructs a PersistenceWriteException with the given message.
     *
     * @param message description of the error
     */
    public PersistenceWriteException(String message) {
        super(message);
    }

    /**
     * Constructs a PersistenceWriteException with the given message and root cause.
     *
     * @param message description of the error
     * @param cause   the underlying exception
     */
    public PersistenceWriteException(String message, Throwable cause) {
        super(message, cause);
    }
}
