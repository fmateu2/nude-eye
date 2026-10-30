package business;

/**
 * Represents a product provider (supplier) that offers items for sale.
 * Each provider holds a catalogue of {@link ProductForSale} offers.
 */
public class Provider {

    private final int providerId;

    private final String companyName;

    private final String cif;

    private final String contactName;

    private final String phone;

    private final String email;

    private final ProductForSale[] productsForSale;

    /**
     * Constructs a Provider with full details.
     *
     * @param providerId      unique provider identifier
     * @param companyName     legal company name
     * @param cif             tax identification number (CIF)
     * @param contactName     name of the contact person
     * @param phone           contact phone number
     * @param email           contact email address
     * @param productsForSale array of products offered by this provider
     */
    public Provider(
        int providerId,
        String companyName,
        String cif,
        String contactName,
        String phone,
        String email,
        ProductForSale[] productsForSale
    ) {
        this.providerId = providerId;
        this.companyName = companyName;
        this.cif = cif;
        this.contactName = contactName;
        this.phone = phone;
        this.email = email;
        this.productsForSale = productsForSale;
    }

    /**
     * Returns the unique identifier of this provider.
     *
     * @return provider ID
     */
    public int getProviderId() {
        return providerId;
    }

    /**
     * Returns the legal name of this provider's company.
     *
     * @return company name
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Returns the CIF (tax identification number) of this provider.
     *
     * @return CIF string
     */
    public String getCif() {
        return cif;
    }

    /**
     * Returns the name of the contact person for this provider.
     *
     * @return contact person name
     */
    public String getContactName() {
        return contactName;
    }

    /**
     * Returns the contact phone number of this provider.
     *
     * @return phone number string
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Returns the contact email address of this provider.
     *
     * @return email address string
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the list of products and their sale conditions offered by this provider.
     *
     * @return array of {@link ProductForSale} offers
     */
    public ProductForSale[] getProductsForSale() {
        return productsForSale;
    }
}
