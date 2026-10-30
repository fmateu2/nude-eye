package business;

/**
 * Immutable value object representing an international phone number.
 * Stores the country prefix and local number separately.
 */
public class PhoneNumber {

    private final String countryPrefix;

    private final String number;

    /**
     * Constructs a PhoneNumber with the given country prefix and local number.
     *
     * @param countryPrefix international dialling prefix (e.g. {@code "+34"})
     * @param number        local phone number
     */
    public PhoneNumber(String countryPrefix, String number) {
        this.countryPrefix = countryPrefix;
        this.number = number;
    }

    /**
     * Returns the international dialling prefix.
     *
     * @return country prefix string
     */
    public String getCountryPrefix() {
        return countryPrefix;
    }

    /**
     * Returns the local phone number.
     *
     * @return phone number string
     */
    public String getNumber() {
        return number;
    }

    @Override
    /**
     * Returns a human-readable representation in the format {@code (prefix) number}.
     *
     * @return formatted phone number string
     */
    public String toString() {
        return "(" + countryPrefix + ") " + number;
    }
}
