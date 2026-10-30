package business;

/**
 * Utility class holding all pricing and geography constants used across the application.
 * Not instantiable.
 */
public final class PricingConstants {

    /** Standard VAT multiplier (21%) applied to most physical products and services. */
    public static final float STANDARD_VAT_MULTIPLIER = 1.21f;

    /** Reduced VAT multiplier (10%) applied to contact lenses. */
    public static final float CONTACT_LENS_VAT_MULTIPLIER = 1.10f;

    /** Price multiplier (60%) applied to consumables expiring within three months. */
    public static final float EXPIRING_CONSUMABLE_DISCOUNT_MULTIPLIER = 0.60f;

    /** Fixed base cost (€) added to every online order for shipping. */
    public static final double SHIPPING_BASE_COST = 2.0;

    /** Variable shipping cost per kilometre (€/km) for online orders. */
    public static final double SHIPPING_COST_PER_KM = 0.01;

    /** Latitude of the La Salle campus, used as the shipping origin point. */
    public static final double LASALLE_LATITUDE = 41.408879;

    /** Longitude of the La Salle campus, used as the shipping origin point. */
    public static final double LASALLE_LONGITUDE = 2.130103;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private PricingConstants() {}
}
