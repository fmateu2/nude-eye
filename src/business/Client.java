package business;

import geolocationAPI.Address;
import geolocationAPI.GeolocationApiManager;

/**
 * Abstract base class representing a registered client.
 * Concrete subtypes are {@link InPersonClient}, {@link OnlineClient}, and {@link CorporateClient}.
 */
public abstract class Client {

    protected int clientId;
    protected String fullName;
    protected PhoneNumber[] phoneNumbers;

    /**
     * Constructs a client with the given identifiers.
     *
     * @param clientId     unique client identifier
     * @param fullName     full name or company name of the client
     * @param phoneNumbers array of associated phone numbers
     */
    public Client(int clientId, String fullName, PhoneNumber[] phoneNumbers) {
        this.clientId = clientId;
        this.fullName = fullName;
        this.phoneNumbers = phoneNumbers;
    }

    /**
     * Returns the unique client identifier.
     *
     * @return client ID
     */
    public int getClientId() {
        return clientId;
    }

    /**
     * Returns the full name or company name of the client.
     *
     * @return full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Alias for {@link #getFullName()}.
     *
     * @return full name
     */
    public String getName() {
        return fullName;
    }

    /**
     * Returns the phone numbers associated with this client.
     *
     * @return array of {@link PhoneNumber} objects
     */
    public PhoneNumber[] getPhoneNumbers() {
        return phoneNumbers;
    }

    /**
     * Returns a string identifier for the client type (e.g. {@code "in_person"}).
     * Used for serialisation.
     *
     * @return client type string
     */
    public abstract String getClientType();

    /**
     * Returns client-type-specific lines for the profile display.
     * Subclasses override this to include type-specific fields.
     *
     * @return array of formatted profile strings (empty by default)
     */
    public String[] getSpecificProfileLines() {
        return new String[0];
    }

    /**
     * Determines whether this client is allowed to purchase the given product.
     * Defaults to {@code true}; overridden by subtypes with purchase restrictions.
     *
     * @param product the product to check
     * @return {@code true} if the client may purchase the product
     */
    public boolean canBuy(Product product) {
        return true;
    }

    /**
     * Indicates whether VAT should be applied to this client's purchases.
     * Defaults to {@code true}; overridden by subtypes with different tax rules.
     *
     * @return {@code true} if VAT is applicable
     */
    public boolean appliesVAT() {
        return true;
    }

    /**
     * Applies client-specific cost adjustments to the cart total (e.g. shipping costs).
     * Defaults to returning the total unchanged; overridden by {@link OnlineClient}.
     *
     * @param total      pre-adjustment cart total
     * @param geoManager geolocation service for distance calculations
     * @return adjusted total
     * @throws BusinessException if the adjustment calculation fails
     */
    public float applyConstraintRules(float total, GeolocationApiManager geoManager)
        throws BusinessException {
        return total;
    }

    /**
     * Formats an {@link geolocationAPI.Address} as a single human-readable string.
     *
     * @param address the address to format, or {@code null}
     * @return formatted address string, or an empty string if {@code null}
     */
    protected String formatAddress(Address address) {
        if (address == null) {
            return "";
        }
        return (
            address.getStreet() +
            " " +
            address.getNumber() +
            ", " +
            address.getPostalCode() +
            " " +
            address.getCity() +
            ", " +
            address.getCountry()
        );
    }
}
