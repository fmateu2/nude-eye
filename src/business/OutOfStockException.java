package business;

/**
 * Thrown when a client attempts to add a product to the cart that has no remaining stock.
 */
public class OutOfStockException extends BusinessException {

    /**
     * Constructs an OutOfStockException with the given message.
     *
     * @param message description of the error
     */
    public OutOfStockException(String message) {
        super(message);
    }
}
