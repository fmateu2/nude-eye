package business;

/**
 * Represents a pair of glasses in the product catalogue.
 * Inherits brand and model from {@link PhysicalProduct} and applies standard VAT pricing.
 */
public class Glasses extends PhysicalProduct {

    /**
     * Constructs a Glasses product.
     *
     * @param productId unique product identifier (e.g. {@code GLS-001})
     * @param name      product name
     * @param brand     brand name
     * @param model     model name
     */
    public Glasses(String productId, String name, String brand, String model) {
        super(productId, name, brand, model);
    }

    @Override
    /**
     * Returns the product type label for glasses.
     *
     * @return {@code "Glasses"}
     */
    public String getProductType() {
        return "Glasses";
    }
}
