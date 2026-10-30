package business;

/**
 * Thrown when a requested product cannot be found in the data source.
 */
public class ProductNotFoundException extends BusinessException {

    /**
     * Constructs a ProductNotFoundException with the given message.
     *
     * @param message description of the error
     */
    public ProductNotFoundException(String message) {
        super(message);
    }
}
