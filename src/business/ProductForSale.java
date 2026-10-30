package business;

/**
 * Represents a product offering by a specific provider, including price and available stock.
 */
public class ProductForSale {

    private final String productId;

    private final int providerId;

    private final float sellingPrice;

    private int unitsInStock;

    /**
     * Constructs a ProductForSale entry.
     *
     * @param productId     identifier of the product being offered
     * @param providerId    identifier of the provider making the offer
     * @param sellingPrice  price at which the provider sells the product (before VAT)
     * @param unitsInStock  number of units currently available
     */
    public ProductForSale(String productId, int providerId, float sellingPrice, int unitsInStock) {
        this.productId = productId;
        this.providerId = providerId;
        this.sellingPrice = sellingPrice;
        this.unitsInStock = unitsInStock;
    }

    /**
     * Returns the product identifier for this offer.
     *
     * @return product ID string
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Returns the provider identifier for this offer.
     *
     * @return provider ID
     */
    public int getProviderId() {
        return providerId;
    }

    /**
     * Returns the selling price set by the provider (before any VAT or discounts).
     *
     * @return selling price in euros
     */
    public float getSellingPrice() {
        return sellingPrice;
    }

    /**
     * Returns the number of units currently in stock for this offer.
     *
     * @return units in stock
     */
    public int getUnitsInStock() {
        return unitsInStock;
    }

    /**
     * Decreases the available stock by the specified number of units.
     * The stock will not go below zero.
     *
     * @param units number of units sold
     */
    public void decreaseStock(int units) {
        this.unitsInStock = Math.max(0, this.unitsInStock - units);
    }
}
