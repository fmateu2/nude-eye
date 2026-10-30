package business;

/**
 * Thrown when a requested client cannot be found in the data source.
 */
public class ClientNotFoundException extends BusinessException {

    /**
     * Constructs a ClientNotFoundException with the given message.
     *
     * @param message description of the error
     */
    public ClientNotFoundException(String message) {
        super(message);
    }
}
