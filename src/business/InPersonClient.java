package business;

/**
 * Represents a client who visits the shop in person.
 * Has no purchase restrictions and VAT always applies.
 */
public class InPersonClient extends Client {

    /**
     * Constructs an in-person client.
     *
     * @param clientId     unique client identifier
     * @param fullName     full name of the client
     * @param phoneNumbers array of associated phone numbers
     */
    public InPersonClient(int clientId, String fullName, PhoneNumber[] phoneNumbers) {
        super(clientId, fullName, phoneNumbers);
    }

    @Override
    /**
     * Returns the client type identifier for in-person clients.
     *
     * @return {@code "in_person"}
     */
    public String getClientType() {
        return "in_person";
    }
}
