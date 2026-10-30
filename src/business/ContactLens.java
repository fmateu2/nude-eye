package business;

/**
 * Represents a box of contact lenses in the product catalogue.
 * Applies a reduced VAT rate (10%) as mandated by Spanish tax law for optical health products.
 */
public class ContactLens extends PhysicalProduct {

    private double prescription;
    private int numberOfLenses;
    private boolean uvFilter;

    /**
     * Constructs a ContactLens product.
     *
     * @param productId      unique product identifier (e.g. {@code CLS-001})
     * @param name           product name
     * @param brand          brand name
     * @param model          model name
     * @param prescription   dioptre prescription value
     * @param numberOfLenses number of lenses in the package
     * @param uvFilter       {@code true} if the lenses include UV protection
     */
    public ContactLens(
        String productId,
        String name,
        String brand,
        String model,
        double prescription,
        int numberOfLenses,
        boolean uvFilter
    ) {
        super(productId, name, brand, model);
        this.prescription = prescription;
        this.numberOfLenses = numberOfLenses;
        this.uvFilter = uvFilter;
    }

    /**
     * Returns the prescription (dioptre value) for these contact lenses.
     *
     * @return prescription value
     */
    public double getPrescription() {
        return prescription;
    }

    /**
     * Returns the number of lenses included in this package.
     *
     * @return number of lenses
     */
    public int getNumberOfLenses() {
        return numberOfLenses;
    }

    /**
     * Indicates whether these contact lenses include UV protection.
     *
     * @return {@code true} if UV filter is present
     */
    public boolean hasUvFilter() {
        return uvFilter;
    }

    @Override
    /**
     * Returns the product type label for contact lenses.
     *
     * @return {@code "Contact lens"}
     */
    public String getProductType() {
        return "Contact lens";
    }

    @Override
    /**
     * Returns display lines showing prescription, number of lenses, and UV filter status.
     *
     * @return array of formatted info strings
     */
    public String[] getSpecificInfoLines() {
        String uvFilterText;
        if (uvFilter) {
            uvFilterText = "Yes";
        } else {
            uvFilterText = "No";
        }
        return new String[] {
            "Prescription: " + String.format("%.2f", prescription),
            "Number of lenses: " + numberOfLenses,
            "UV filter: " + uvFilterText
        };
    }

    @Override
    /**
     * Calculates the final price applying the reduced 10% VAT rate for contact lenses.
     *
     * @param sellingPrice base selling price set by the provider
     * @param applyVAT     {@code true} to add 10% VAT
     * @return price with or without reduced VAT
     */
    public float calculatePrice(float sellingPrice, boolean applyVAT) {
        if (applyVAT) {
            return sellingPrice * PricingConstants.CONTACT_LENS_VAT_MULTIPLIER;
        }
        return sellingPrice;
    }
}
