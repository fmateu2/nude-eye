package business;

import geolocationAPI.Address;
import persistence.dao.ClientDAO;
import persistence.dao.SaleDAO;
import persistence.exception.PersistenceException;
import persistence.impl.ClientAPIDAO;
import persistence.impl.ClientJSONDAO;
import persistence.impl.SaleAPIDAO;
import persistence.impl.SaleCSVDAO;

/**
 * Business-layer facade for client registration, authentication, and purchase history.
 * Selects the appropriate DAO implementations based on the persistence mode.
 */
public class ClientManager {

    private static final String GROUP_ID = "5";

    private final ClientDAO clientDAO;
    private final SaleDAO saleDAO;
    private Client currentClient;

    /**
     * Constructs a ClientManager selecting DAOs based on the persistence mode.
     *
     * @param useApi {@code true} to use the remote API DAOs; {@code false} for local files
     * @throws BusinessException if any DAO cannot be initialised
     */
    public ClientManager(boolean useApi) throws BusinessException {
        this.clientDAO = createClientDAO(useApi);
        this.saleDAO = createSaleDAO(useApi);
        validateClientDAO();
    }

    /**
     * Constructs a ClientManager with explicit DAO instances (for testing or injection).
     *
     * @param clientDAO the client DAO to use
     * @param saleDAO   the sale DAO to use
     */
    public ClientManager(ClientDAO clientDAO, SaleDAO saleDAO) {
        this.clientDAO = clientDAO;
        this.saleDAO = saleDAO;
    }

    private ClientDAO createClientDAO(boolean useApi) throws BusinessException {
        if (useApi) {
            try {
                return new ClientAPIDAO(GROUP_ID);
            } catch (PersistenceException e) {
                throw new BusinessException(e.getMessage(), e);
            }
        }
        return new ClientJSONDAO();
    }

    private SaleDAO createSaleDAO(boolean useApi) throws BusinessException {
        if (useApi) {
            try {
                return new SaleAPIDAO(GROUP_ID);
            } catch (PersistenceException e) {
                throw new BusinessException(e.getMessage(), e);
            }
        }
        return new SaleCSVDAO();
    }

    private void validateClientDAO() throws BusinessException {
        try {
            clientDAO.findAll();
        } catch (PersistenceException e) {
            throw new BusinessException(e.getMessage(), e);
        }
    }

    /**
     * Authenticates a client by ID and sets them as the current session client.
     *
     * @param clientId the client ID to log in with
     * @return the authenticated {@link Client} object
     * @throws ClientNotFoundException if no client with that ID exists
     * @throws BusinessException       if access to the data source fails
     */
    public Client login(int clientId) throws BusinessException {
        try {
            Client client = clientDAO.findById(clientId);
            if (client == null) {
                throw new ClientNotFoundException("Client not found.");
            }
            currentClient = client;
            return client;
        } catch (PersistenceException e) {
            throw new BusinessException("Error accessing client data.", e);
        }
    }

    /**
     * Registers a new in-person client and saves them to the data source.
     *
     * @param fullName full name of the client
     * @param phones   array of phone numbers
     * @return the newly created {@link InPersonClient}
     * @throws BusinessException if saving the client fails
     */
    public Client registerInPerson(String fullName, PhoneNumber[] phones) throws BusinessException {
        try {
            Client client = new InPersonClient(generateNextClientId(), fullName, phones);
            clientDAO.save(client);
            currentClient = client;
            return client;
        } catch (PersistenceException e) {
            throw new BusinessException("Error saving new client.", e);
        }
    }

    /**
     * Registers a new online client from individual address fields.
     *
     * @param fullName   full name of the client
     * @param phones     array of phone numbers
     * @param email      contact email address
     * @param street     street name of the shipping address
     * @param number     street number
     * @param city       city
     * @param country    country
     * @param postalCode postal code
     * @return the newly created {@link OnlineClient}
     * @throws BusinessException if saving the client fails
     */
    public Client registerOnline(
        String fullName,
        PhoneNumber[] phones,
        String email,
        String street,
        int number,
        String city,
        String country,
        String postalCode
    ) throws BusinessException {
        Address shippingAddress = new Address(street, number, city, country, postalCode);
        return registerOnline(fullName, phones, email, shippingAddress);
    }

    /**
     * Registers a new online client with a pre-built shipping address.
     *
     * @param fullName        full name of the client
     * @param phones          array of phone numbers
     * @param email           contact email address
     * @param shippingAddress delivery address
     * @return the newly created {@link OnlineClient}
     * @throws BusinessException if saving the client fails
     */
    public Client registerOnline(
        String fullName,
        PhoneNumber[] phones,
        String email,
        Address shippingAddress
    ) throws BusinessException {
        try {
            Client client = new OnlineClient(
                generateNextClientId(),
                fullName,
                phones,
                email,
                shippingAddress
            );
            clientDAO.save(client);
            currentClient = client;
            return client;
        } catch (PersistenceException e) {
            throw new BusinessException("Error saving new client.", e);
        }
    }

    /**
     * Registers a new corporate client from individual address fields.
     *
     * @param companyName         company name
     * @param phones              array of phone numbers
     * @param nif                 tax identification number (NIF/CIF)
     * @param contactPersonName   name of the contact person
     * @param billingStreet       billing address street
     * @param billingNumber       billing address number
     * @param billingCity         billing address city
     * @param billingCountry      billing address country
     * @param billingPostalCode   billing address postal code
     * @param shippingStreet      shipping address street
     * @param shippingNumber      shipping address number
     * @param shippingCity        shipping address city
     * @param shippingCountry     shipping address country
     * @param shippingPostalCode  shipping address postal code
     * @return the newly created {@link CorporateClient}
     * @throws BusinessException if saving the client fails
     */
    public Client registerCorporate(
        String companyName,
        PhoneNumber[] phones,
        String nif,
        String contactPersonName,
        String billingStreet,
        int billingNumber,
        String billingCity,
        String billingCountry,
        String billingPostalCode,
        String shippingStreet,
        int shippingNumber,
        String shippingCity,
        String shippingCountry,
        String shippingPostalCode
    ) throws BusinessException {
        Address billingAddress = new Address(
            billingStreet,
            billingNumber,
            billingCity,
            billingCountry,
            billingPostalCode
        );
        Address shippingAddress = new Address(
            shippingStreet,
            shippingNumber,
            shippingCity,
            shippingCountry,
            shippingPostalCode
        );
        return registerCorporate(
            companyName,
            phones,
            nif,
            contactPersonName,
            billingAddress,
            shippingAddress
        );
    }

    /**
     * Registers a new corporate client with pre-built address objects.
     *
     * @param companyName       company name
     * @param phones            array of phone numbers
     * @param nif               tax identification number (NIF/CIF)
     * @param contactPersonName name of the contact person
     * @param billingAddress    billing address
     * @param shippingAddress   shipping address
     * @return the newly created {@link CorporateClient}
     * @throws BusinessException if saving the client fails
     */
    public Client registerCorporate(
        String companyName,
        PhoneNumber[] phones,
        String nif,
        String contactPersonName,
        Address billingAddress,
        Address shippingAddress
    ) throws BusinessException {
        try {
            Client client = new CorporateClient(
                generateNextClientId(),
                companyName,
                phones,
                nif,
                contactPersonName,
                billingAddress,
                shippingAddress
            );
            clientDAO.save(client);
            currentClient = client;
            return client;
        } catch (PersistenceException e) {
            throw new BusinessException("Error saving new client.", e);
        }
    }

    /**
     * Convenience method that registers a new in-person client.
     * Delegates to {@link #registerInPerson(String, PhoneNumber[])}.
     *
     * @param fullName full name of the client
     * @param phones   array of phone numbers
     * @return the newly created client
     * @throws BusinessException if saving the client fails
     */
    public Client register(String fullName, PhoneNumber[] phones) throws BusinessException {
        return registerInPerson(fullName, phones);
    }

    /**
     * Returns the client currently logged in, or {@code null} if no session is active.
     *
     * @return current {@link Client} or {@code null}
     */
    public Client getCurrentClient() {
        return currentClient;
    }

    /**
     * Returns the purchase history for the given client, ordered by date descending.
     *
     * @param clientId the client whose history to retrieve
     * @return array of {@link Sale} records
     * @throws BusinessException if reading the history fails
     */
    public Sale[] getPurchaseHistory(int clientId) throws BusinessException {
        try {
            return saleDAO.findByClientId(clientId);
        } catch (PersistenceException e) {
            throw new BusinessException("Error reading purchase history.", e);
        }
    }

    /**
     * Ends the current session by clearing the logged-in client reference.
     */
    public void logout() {
        currentClient = null;
    }

    private int generateNextClientId() throws PersistenceException {
        Client[] clients = clientDAO.findAll();
        int maxId = 0;
        for (Client client : clients) {
            if (client.getClientId() > maxId) {
                maxId = client.getClientId();
            }
        }
        return maxId + 1;
    }
}
