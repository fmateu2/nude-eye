package business;

/**
 * Abstract base class representing a product in the Nude Eye catalogue.
 * All product types (glasses, contact lenses, consumables, services) extend this class.
 */
public abstract class Product {

    protected String productId;
    protected String name;

    /**
     * Constructs a product with the given identifier and name.
     *
     * @param productId unique product identifier (e.g. {@code GLS-001})
     * @param name      human-readable product name
     */
    public Product(String productId, String name) {
        this.productId = productId;
        this.name = name;
    }

    /**
     * Returns the unique product identifier.
     *
     * @return product ID string
     */
    public String getProductId() {
        return productId;
    }

    /**
     * Returns the display name of the product.
     *
     * @return product name
     */
    public String getProductName() {
        return name;
    }

    /**
     * Alias for {@link #getProductName()}.
     *
     * @return product name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the brand of the product.
     * Overridden by {@link PhysicalProduct}; returns an empty string for non-physical products.
     *
     * @return brand name, or empty string if not applicable
     */
    public String getBrand() {
        return "";
    }

    /**
     * Returns the model of the product.
     * Overridden by {@link PhysicalProduct}; returns an empty string for non-physical products.
     *
     * @return model name, or empty string if not applicable
     */
    public String getModel() {
        return "";
    }

    /**
     * Returns a human-readable label for the product type.
     * Subclasses should override this to return their specific type string.
     *
     * @return product type label
     */
    public String getProductType() {
        return "Product";
    }

    /**
     * Returns product-type-specific display lines for the UI.
     * Subclasses override this to add fields like prescription, volume, etc.
     *
     * @return array of formatted info strings (empty by default)
     */
    public String[] getSpecificInfoLines() {
        return new String[0];
    }

    /**
     * Calculates the final price for this product.
     *
     * @param sellingPrice base selling price set by the provider
     * @param applyVAT     {@code true} if VAT should be added
     * @return final price after any applicable taxes or adjustments
     */
    public abstract float calculatePrice(float sellingPrice, boolean applyVAT);
}
