package persistence.exception;

/**
 * Base checked exception for persistence-layer errors.
 * All data-access exceptions extend this class.
 */
public class PersistenceException extends Exception {

    /**
     * Constructs a PersistenceException with the given message.
     *
     * @param message human-readable description of the error
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * Constructs a PersistenceException with the given message and root cause.
     *
     * @param message human-readable description of the error
     * @param cause   the underlying exception
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
