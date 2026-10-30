package business;

import java.time.LocalDate;

/**
 * Represents a consumable product (e.g. eye drops or cleaning solution).
 * Applies a 40% discount when the product expires in less than three months.
 */
public class Consumable extends PhysicalProduct {

    private double volumeMl;
    private LocalDate expirationDate;
    private boolean containsPreservatives;

    /**
     * Constructs a Consumable product.
     *
     * @param productId             unique product identifier (e.g. {@code CON-001})
     * @param name                  product name
     * @param brand                 brand name
     * @param model                 model name
     * @param volumeMl              volume of the product in millilitres
     * @param expirationDate        expiration date of the product
     * @param containsPreservatives {@code true} if the product contains preservatives
     */
    public Consumable(
        String productId,
        String name,
        String brand,
        String model,
        double volumeMl,
        LocalDate expirationDate,
        boolean containsPreservatives
    ) {
        super(productId, name, brand, model);
        this.volumeMl = volumeMl;
        this.expirationDate = expirationDate;
        this.containsPreservatives = containsPreservatives;
    }

    /**
     * Returns the volume of this consumable product in millilitres.
     *
     * @return volume in ml
     */
    public double getVolumeMl() {
        return volumeMl;
    }

    /**
     * Returns the expiration date of this consumable product.
     *
     * @return expiration date
     */
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    /**
     * Indicates whether this product contains preservatives.
     *
     * @return {@code true} if preservatives are present
     */
    public boolean containsPreservatives() {
        return containsPreservatives;
    }

    /**
     * Checks whether this product expires within the next three months.
     * Used to determine eligibility for the near-expiry discount.
     *
     * @return {@code true} if expiration is less than three months away
     */
    private boolean expiresInLessThanThreeMonths() {
        return expirationDate != null && expirationDate.isBefore(LocalDate.now().plusMonths(3));
    }

    @Override
    /**
     * Returns the product type label for consumables.
     *
     * @return {@code "Consumable"}
     */
    public String getProductType() {
        return "Consumable";
    }

    @Override
    /**
     * Returns display lines showing volume, expiration date, and preservatives status.
     *
     * @return array of formatted info strings
     */
    public String[] getSpecificInfoLines() {
        String preservativesText;
        if (containsPreservatives) {
            preservativesText = "Yes";
        } else {
            preservativesText = "No";
        }
        return new String[] {
            "Volume: " + String.format("%.2f", volumeMl) + " ml",
            "Expiration date: " + expirationDate,
            "Contains preservatives: " + preservativesText
        };
    }

    @Override
    /**
     * Calculates the final price, applying standard VAT (21%) and a 40% near-expiry
     * discount when the product expires within three months.
     *
     * @param sellingPrice base selling price set by the provider
     * @param applyVAT     {@code true} to add 21% VAT
     * @return final price after VAT and any applicable discount
     */
    public float calculatePrice(float sellingPrice, boolean applyVAT) {
        float price;
        if (applyVAT) {
            price = sellingPrice * PricingConstants.STANDARD_VAT_MULTIPLIER;
        } else {
            price = sellingPrice;
        }

        if (expiresInLessThanThreeMonths()) {
            price = price * PricingConstants.EXPIRING_CONSUMABLE_DISCOUNT_MULTIPLIER;
        }
        return price;
    }
}
