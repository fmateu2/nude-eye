package business;

/**
 * Represents an optician service (e.g. eye exam, lens fitting).
 * Price is calculated as {@code sellingPrice × completionTimeHours}, with optional VAT.
 */
public class Service extends Product {

    private double completionTimeHours;

    /**
     * Constructs a Service product.
     *
     * @param productId           unique product identifier (e.g. {@code SRV-001})
     * @param name                service name
     * @param completionTimeHours estimated duration of the service in hours
     */
    public Service(String productId, String name, double completionTimeHours) {
        super(productId, name);
        this.completionTimeHours = completionTimeHours;
    }

    /**
     * Returns the estimated completion time for this service in hours.
     *
     * @return completion time in hours
     */
    public double getCompletionTimeHours() {
        return completionTimeHours;
    }

    @Override
    /**
     * Returns the product type label for services.
     *
     * @return {@code "Service"}
     */
    public String getProductType() {
        return "Service";
    }

    @Override
    /**
     * Returns a display line showing the estimated completion time in hours.
     *
     * @return array with one formatted info string
     */
    public String[] getSpecificInfoLines() {
        return new String[] {
            "Completion time: " + String.format("%.2f", completionTimeHours) + " hours"
        };
    }

    @Override
    /**
     * Calculates the final price as {@code sellingPrice × completionTimeHours},
     * with standard VAT (21%) applied if required.
     *
     * @param sellingPrice hourly rate set by the provider
     * @param applyVAT     {@code true} to add 21% VAT
     * @return total price for the service
     */
    public float calculatePrice(float sellingPrice, boolean applyVAT) {
        float price = (float) (sellingPrice * completionTimeHours);
        if (applyVAT) {
            price = price * PricingConstants.STANDARD_VAT_MULTIPLIER;
        }
        return price;
    }
}
