package business;

import java.util.Date;

/**
 * Immutable record of a completed purchase transaction.
 */
public class Sale {

    private final int clientId;

    private final String productId;

    private final float pricePaid;

    private final Date purchaseDate;

    /**
     * Constructs a Sale record.
     *
     * @param clientId     identifier of the client who made the purchase
     * @param productId    identifier of the product that was purchased
     * @param pricePaid    final price paid by the client (after VAT and discounts)
     * @param purchaseDate date and time when the purchase was completed
     */
    public Sale(int clientId, String productId, float pricePaid, Date purchaseDate) {
        this.clientId = clientId;
        this.productId = productId;
        this.pricePaid = pricePaid;
        this.purchaseDate = purchaseDate;
    }

    /**
     * Returns the identifier of the client who made this purchase.
     *
     * @return client ID
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Returns the identifier of the product that was purchased.
     *
     * @return product ID string
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Returns the final price paid for this sale.
     *
     * @return price paid in euros
     */
    public float getPricePaid() {
        return pricePaid;
    }

    /**
     * Returns the date and time when this purchase was completed.
     *
     * @return purchase timestamp
     */
    public Date getPurchaseDate() {
        return purchaseDate;
    }
}
