package persistence.exception;

/**
 * Thrown when a required persistence file or API resource is missing,
 * malformed, or cannot be parsed.
 */
public class PersistenceFileNotFoundException extends PersistenceException {

    /**
     * Constructs a PersistenceFileNotFoundException with the given message.
     *
     * @param message description of the error
     */
    public PersistenceFileNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a PersistenceFileNotFoundException with the given message and root cause.
     *
     * @param message description of the error
     * @param cause   the underlying exception
     */
    public PersistenceFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
