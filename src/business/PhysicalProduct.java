package business;

/**
 * Intermediate abstract class for products that have a physical form (brand and model).
 * Extends {@link Product} and is further specialised by {@link Glasses},
 * {@link ContactLens}, and {@link Consumable}.
 */
public class PhysicalProduct extends Product {

    private String brand;
    private String model;

    /**
     * Constructs a physical product with the given identifiers.
     *
     * @param productId unique product identifier
     * @param name      product name
     * @param brand     brand name
     * @param model     model name
     */
    public PhysicalProduct(String productId, String name, String brand, String model) {
        super(productId, name);
        this.brand = brand;
        this.model = model;
    }

    @Override
    /**
     * Returns the brand of this physical product.
     *
     * @return brand name
     */
    public String getBrand() {
        return brand;
    }

    @Override
    /**
     * Returns the model of this physical product.
     *
     * @return model name
     */
    public String getModel() {
        return model;
    }

    @Override
    /**
     * Returns the product type label for physical products.
     *
     * @return {@code "Physical product"}
     */
    public String getProductType() {
        return "Physical product";
    }

    @Override
    /**
     * Calculates the final price applying standard VAT (21%) if required.
     *
     * @param sellingPrice base selling price set by the provider
     * @param applyVAT     {@code true} to add 21% VAT
     * @return price with or without VAT
     */
    public float calculatePrice(float sellingPrice, boolean applyVAT) {
        if (applyVAT) {
            return sellingPrice * PricingConstants.STANDARD_VAT_MULTIPLIER;
        }
        return sellingPrice;
    }
}
