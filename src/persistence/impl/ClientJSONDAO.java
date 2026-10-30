package persistence.impl;

import business.Client;
import business.CorporateClient;
import business.InPersonClient;
import business.OnlineClient;
import business.PhoneNumber;
import geolocationAPI.Address;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import persistence.dao.ClientDAO;
import persistence.exception.PersistenceException;
import persistence.exception.PersistenceFileNotFoundException;
import persistence.exception.PersistenceWriteException;

/**
 * JSON-based implementation of {@link persistence.dao.ClientDAO}.
 * Reads and writes client data (including all subtypes) from/to {@code clients.json}.
 */
public class ClientJSONDAO implements ClientDAO {

    private static final String FILE_PATH = "clients.json";

    private JSONArray readJSONArray() throws PersistenceFileNotFoundException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new JSONArray();
        }
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(file)) {
            Object parsed = parser.parse(reader);
            if (!(parsed instanceof JSONArray)) {
                throw new PersistenceFileNotFoundException(
                    "Error: The clients.json file has an invalid structure."
                );
            }
            return (JSONArray) parsed;
        } catch (IOException e) {
            throw new PersistenceFileNotFoundException(
                "Error: The clients.json file can't be accessed.",
                e
            );
        } catch (ParseException e) {
            throw new PersistenceFileNotFoundException(
                "Error: The clients.json file contains invalid JSON.",
                e
            );
        }
    }

    @Override
    public Client[] findAll() throws PersistenceException {
        JSONArray array = readJSONArray();
        List<Client> clients = new ArrayList<>();
        for (Object item : array) {
            if (!(item instanceof JSONObject)) {
                throw new PersistenceFileNotFoundException(
                    "Error: Unexpected element in clients.json array."
                );
            }
            JSONObject jsonObject = (JSONObject) item;
            try {
                Number idNum = readNumber(jsonObject.get("client_id"));
                String fullName = readString(jsonObject.get("full_name"));
                if (fullName == null) {
                    fullName = readString(jsonObject.get("company_name"));
                }
                if (fullName == null) {
                    fullName = readString(jsonObject.get("contact_name"));
                }
                Object phonesObject = jsonObject.get("phone_numbers");
                PhoneNumber[] phoneNumbers = readPhones(phonesObject);
                if (idNum == null || fullName == null || phoneNumbers == null) {
                    throw new PersistenceFileNotFoundException(
                        "Error: A client entry in clients.json is missing required fields."
                    );
                }
                String type = readString(jsonObject.get("client_type"));
                if (type == null) {
                    type = readString(jsonObject.get("type"));
                }
                Address shippingAddress = readAddressFromObject(jsonObject.get("shipping_address"));
                Address billingAddress = readAddressFromObject(jsonObject.get("billing_address"));
                if ("online".equalsIgnoreCase(type)) {
                    String email = readString(jsonObject.get("email"));
                    if (email == null) {
                        email = readString(jsonObject.get("contact_email"));
                    }
                    if (email == null) {
                        email = "";
                    }
                    if (shippingAddress == null) {
                        shippingAddress = defaultAddress();
                    }
                    clients.add(
                        new OnlineClient(
                            idNum.intValue(),
                            fullName,
                            phoneNumbers,
                            email,
                            shippingAddress
                        )
                    );
                } else if ("corporate".equalsIgnoreCase(type)) {
                    String nif = readString(jsonObject.get("nif"));
                    if (nif == null) {
                        nif = readString(jsonObject.get("cif"));
                    }
                    String contactPerson = readString(jsonObject.get("contact_person"));
                    if (contactPerson == null) {
                        contactPerson = readString(jsonObject.get("contact_person_name"));
                    }
                    if (contactPerson == null) {
                        contactPerson = readString(jsonObject.get("contact_name"));
                    }
                    if (nif == null) {
                        nif = "";
                    }
                    if (contactPerson == null) {
                        contactPerson = fullName;
                    }
                    if (billingAddress == null) {
                        billingAddress = defaultAddress();
                    }
                    if (shippingAddress == null) {
                        shippingAddress = defaultAddress();
                    }
                    clients.add(
                        new CorporateClient(
                            idNum.intValue(),
                            fullName,
                            phoneNumbers,
                            nif,
                            contactPerson,
                            billingAddress,
                            shippingAddress
                        )
                    );
                } else {
                    clients.add(new InPersonClient(idNum.intValue(), fullName, phoneNumbers));
                }
            } catch (ClassCastException e) {
                throw new PersistenceFileNotFoundException(
                    "Error: A client entry in clients.json has an unexpected field type.",
                    e
                );
            }
        }
        return clients.toArray(new Client[0]);
    }

    @Override
    public void save(Client client) throws PersistenceException {
        Client[] existing = findAll();
        Client[] updated = new Client[existing.length + 1];
        System.arraycopy(existing, 0, updated, 0, existing.length);
        updated[existing.length] = client;
        persistClientToJSON(updated);
    }

    @Override
    public Client findById(int clientId) throws PersistenceException {
        for (Client client : findAll()) {
            if (client.getClientId() == clientId) {
                return client;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void persistClientToJSON(Client[] clients) throws PersistenceWriteException {
        JSONArray array = new JSONArray();
        for (Client client : clients) {
            JSONObject clientObj = new JSONObject();
            clientObj.put("client_id", (long) client.getClientId());
            clientObj.put("client_type", client.getClientType());
            clientObj.put("full_name", client.getFullName());
            clientObj.put("phone_numbers", phonesToJSON(client.getPhoneNumbers()));
            if (client instanceof OnlineClient) {
                OnlineClient onlineClient = (OnlineClient) client;
                clientObj.put("email", onlineClient.getEmail());
                clientObj.put("shipping_address", addressToJSON(onlineClient.getShippingAddress()));
            }
            if (client instanceof CorporateClient) {
                CorporateClient corporateClient = (CorporateClient) client;
                clientObj.put("nif", corporateClient.getNif());
                clientObj.put("contact_person", corporateClient.getContactPersonName());
                clientObj.put(
                    "billing_address",
                    addressToJSON(corporateClient.getBillingAddress())
                );
                clientObj.put(
                    "shipping_address",
                    addressToJSON(corporateClient.getShippingAddress())
                );
            }
            array.add(clientObj);
        }
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writer.write(array.toJSONString());
        } catch (IOException e) {
            throw new PersistenceWriteException("Error: Could not write to clients.json.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private JSONArray phonesToJSON(PhoneNumber[] phoneNumbers) {
        JSONArray phonesArray = new JSONArray();
        for (PhoneNumber phone : phoneNumbers) {
            JSONObject phoneObj = new JSONObject();
            phoneObj.put("country_prefix", phone.getCountryPrefix());
            phoneObj.put("number", phone.getNumber());
            phonesArray.add(phoneObj);
        }
        return phonesArray;
    }

    @SuppressWarnings("unchecked")
    private JSONObject addressToJSON(Address address) {
        JSONObject addressObj = new JSONObject();
        addressObj.put("street", address.getStreet());
        addressObj.put("number", (long) address.getNumber());
        addressObj.put("city", address.getCity());
        addressObj.put("country", address.getCountry());
        addressObj.put("postal_code", address.getPostalCode());
        return addressObj;
    }

    private PhoneNumber[] readPhones(Object phonesObject) throws PersistenceFileNotFoundException {
        if (!(phonesObject instanceof JSONArray)) {
            return null;
        }
        JSONArray phones = (JSONArray) phonesObject;
        List<PhoneNumber> phoneList = new ArrayList<>();
        for (Object item : phones) {
            if (!(item instanceof JSONObject)) {
                throw new PersistenceFileNotFoundException(
                    "Error: A phone_numbers entry in clients.json has an invalid structure."
                );
            }
            JSONObject phoneObj = (JSONObject) item;
            String prefix = readString(phoneObj.get("country_prefix"));
            String number = readString(phoneObj.get("number"));
            if (prefix == null || number == null) {
                throw new PersistenceFileNotFoundException(
                    "Error: A phone_numbers entry in clients.json is missing fields."
                );
            }
            phoneList.add(new PhoneNumber(prefix, number));
        }
        return phoneList.toArray(new PhoneNumber[0]);
    }

    private Address readAddressFromObject(Object value) {
        if (value instanceof JSONObject) {
            JSONObject addressObj = (JSONObject) value;
            String street = readString(addressObj.get("street"));
            Number number = readNumber(addressObj.get("number"));
            String city = readString(addressObj.get("city"));
            String country = readString(addressObj.get("country"));
            String postalCode = readString(addressObj.get("postal_code"));
            if (postalCode == null) {
                postalCode = readString(addressObj.get("postalCode"));
            }
            if (
                street != null &&
                number != null &&
                city != null &&
                country != null &&
                postalCode != null
            ) {
                return new Address(street, number.intValue(), city, country, postalCode);
            }
        }
        return null;
    }

    private Address defaultAddress() {
        return new Address("Sant Joan de La Salle", 42, "Barcelona", "Spain", "08022");
    }

    private String readString(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    private Number readNumber(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        }
        return null;
    }

    private JSONArray readArray(Object value) {
        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }
        return null;
    }
}
