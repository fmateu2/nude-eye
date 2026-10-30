package business;

import geolocationAPI.Address;
import geolocationAPI.Geo;
import geolocationAPI.GeolocationApiManager;

/**
 * Represents a client who shops online.
 * Cannot purchase services, and a distance-based shipping cost is added at checkout.
 */
public class OnlineClient extends Client {

    private String email;
    private Address shippingAddress;

    /**
     * Constructs an online client.
     *
     * @param clientId        unique client identifier
     * @param fullName        full name of the client
     * @param phoneNumbers    array of associated phone numbers
     * @param email           contact email address
     * @param shippingAddress delivery address used for shipping cost calculation
     */
    public OnlineClient(
        int clientId,
        String fullName,
        PhoneNumber[] phoneNumbers,
        String email,
        Address shippingAddress
    ) {
        super(clientId, fullName, phoneNumbers);
        this.email = email;
        this.shippingAddress = shippingAddress;
    }

    /**
     * Returns the email address of this online client.
     *
     * @return email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the shipping address of this online client.
     *
     * @return shipping {@link geolocationAPI.Address}
     */
    public Address getShippingAddress() {
        return shippingAddress;
    }

    @Override
    /**
     * Returns the client type identifier for online clients.
     *
     * @return {@code "online"}
     */
    public String getClientType() {
        return "online";
    }

    @Override
    /**
     * Returns profile lines including client type, email, and shipping address.
     *
     * @return array of formatted profile strings
     */
    public String[] getSpecificProfileLines() {
        return new String[] {
            "Client type: Online client",
            "Email: " + email,
            "Shipping address: " + formatAddress(shippingAddress)
        };
    }

    @Override
    /**
     * Online clients cannot purchase services; all other product types are allowed.
     *
     * @param product the product to check
     * @return {@code false} if the product is a {@link Service}, {@code true} otherwise
     */
    public boolean canBuy(Product product) {
        return !(product instanceof Service);
    }

    @Override
    /**
     * Adds a distance-based shipping cost to the cart total.
     * Shipping is calculated as {@code SHIPPING_BASE_COST + SHIPPING_COST_PER_KM × distance}
     * where distance is the straight-line distance from La Salle to the shipping address.
     *
     * @param total      pre-shipping cart total
     * @param geoManager geolocation service used to resolve coordinates and distance
     * @return total including the shipping cost
     * @throws BusinessException if the geolocation or distance calculation fails
     */
    public float applyConstraintRules(float total, GeolocationApiManager geoManager)
        throws BusinessException {
        try {
            Geo origin = new Geo(
                PricingConstants.LASALLE_LATITUDE,
                PricingConstants.LASALLE_LONGITUDE
            );
            Geo destination = geoManager.getGeolocationFromAddress(shippingAddress);
            double distanceKm = geoManager.calculateDistance(origin, destination);
            double shippingCost =
                PricingConstants.SHIPPING_BASE_COST +
                PricingConstants.SHIPPING_COST_PER_KM * distanceKm;
            return (float) (total + shippingCost);
        } catch (Exception e) {
            throw new BusinessException("Error calculating shipping cost.", e);
        }
    }
}
