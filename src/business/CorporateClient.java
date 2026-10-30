package business;

import geolocationAPI.Address;

/**
 * Represents a corporate (B2B) client.
 * Has separate billing and shipping addresses; VAT applies only for Spanish billing addresses.
 */
public class CorporateClient extends Client {

    private String nif;
    private String contactPersonName;
    private Address billingAddress;
    private Address shippingAddress;

    /**
     * Constructs a corporate client.
     *
     * @param clientId          unique client identifier
     * @param fullName          company name
     * @param phoneNumbers      array of associated phone numbers
     * @param nif               tax identification number (NIF/CIF)
     * @param contactPersonName name of the primary contact person
     * @param billingAddress    billing address (used for VAT determination)
     * @param shippingAddress   delivery address
     */
    public CorporateClient(
        int clientId,
        String fullName,
        PhoneNumber[] phoneNumbers,
        String nif,
        String contactPersonName,
        Address billingAddress,
        Address shippingAddress
    ) {
        super(clientId, fullName, phoneNumbers);
        this.nif = nif;
        this.contactPersonName = contactPersonName;
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
    }

    /**
     * Returns the tax identification number (NIF/CIF) of this corporate client.
     *
     * @return NIF/CIF string
     */
    public String getNif() {
        return nif;
    }

    /**
     * Returns the name of the primary contact person for this corporate client.
     *
     * @return contact person name
     */
    public String getContactPersonName() {
        return contactPersonName;
    }

    /**
     * Returns the billing address of this corporate client.
     *
     * @return billing {@link geolocationAPI.Address}
     */
    public Address getBillingAddress() {
        return billingAddress;
    }

    /**
     * Returns the shipping address of this corporate client.
     *
     * @return shipping {@link geolocationAPI.Address}
     */
    public Address getShippingAddress() {
        return shippingAddress;
    }

    @Override
    /**
     * Returns the client type identifier for corporate clients.
     *
     * @return {@code "corporate"}
     */
    public String getClientType() {
        return "corporate";
    }

    @Override
    /**
     * Returns profile lines including client type, NIF, contact person, and addresses.
     *
     * @return array of formatted profile strings
     */
    public String[] getSpecificProfileLines() {
        return new String[] {
            "Client type: Corporate client",
            "NIF: " + nif,
            "Contact person: " + contactPersonName,
            "Billing address: " + formatAddress(billingAddress),
            "Shipping address: " + formatAddress(shippingAddress)
        };
    }

    @Override
    /**
     * Corporate clients can purchase all product types.
     *
     * @param product the product to check
     * @return always {@code true}
     */
    public boolean canBuy(Product product) {
        return true;
    }

    @Override
    /**
     * VAT applies only when the billing address is in Spain.
     *
     * @return {@code true} if the billing address country is {@code "Spain"}
     */
    public boolean appliesVAT() {
        return billingAddress != null && "Spain".equalsIgnoreCase(billingAddress.getCountry());
    }
}
