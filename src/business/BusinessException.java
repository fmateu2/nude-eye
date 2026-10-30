package business;

/**
 * Base checked exception for business-layer errors.
 * All domain-level exceptions extend this class.
 */
public class BusinessException extends Exception {

    /**
     * Constructs a BusinessException with the given message.
     *
     * @param message human-readable description of the error
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Constructs a BusinessException with the given message and root cause.
     *
     * @param message human-readable description of the error
     * @param cause   the underlying exception
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
