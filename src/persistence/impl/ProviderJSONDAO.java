package persistence.impl;

import business.ProductForSale;
import business.Provider;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import persistence.dao.ProviderDAO;
import persistence.exception.PersistenceException;
import persistence.exception.PersistenceFileNotFoundException;
import persistence.exception.PersistenceWriteException;

/**
 * JSON-based implementation of {@link persistence.dao.ProviderDAO}.
 * Reads and writes provider data including product-for-sale catalogues from/to {@code providers.json}.
 */
public class ProviderJSONDAO implements ProviderDAO {

    private static final String FILE_PATH = "providers.json";

    private JSONArray readJSONArray() throws PersistenceFileNotFoundException {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Object parsed = parser.parse(reader);
            if (!(parsed instanceof JSONArray)) {
                throw new PersistenceFileNotFoundException(
                    "Error: The providers.json file has an invalid structure."
                );
            }
            return (JSONArray) parsed;
        } catch (IOException e) {
            throw new PersistenceFileNotFoundException(
                "Error: The providers.json file can't be accessed.",
                e
            );
        } catch (ParseException e) {
            throw new PersistenceFileNotFoundException(
                "Error: The providers.json file contains invalid JSON.",
                e
            );
        }
    }

    @Override
    public Provider[] findAll() throws PersistenceException {
        JSONArray array = readJSONArray();
        List<Provider> providers = new ArrayList<>();
        for (Object item : array) {
            if (!(item instanceof JSONObject)) {
                throw new PersistenceFileNotFoundException(
                    "Error: Unexpected element in providers.json array."
                );
            }
            JSONObject providerObj = (JSONObject) item;
            try {
                Number idNum = readNumber(providerObj.get("provider_id"));
                String companyName = readString(providerObj.get("company_name"));
                String cif = readString(providerObj.get("cif"));
                String contactName = readString(providerObj.get("contact_name"));
                String phone = readString(providerObj.get("phone"));
                String email = readString(providerObj.get("email"));
                JSONArray products = readArray(providerObj.get("products_for_sale"));
                if (
                    idNum == null ||
                    companyName == null ||
                    cif == null ||
                    contactName == null ||
                    phone == null ||
                    email == null ||
                    products == null
                ) {
                    throw new PersistenceFileNotFoundException(
                        "Error: A provider entry in providers.json is missing required fields."
                    );
                }
                int providerId = idNum.intValue();
                List<ProductForSale> offers = new ArrayList<>();
                for (Object productItem : products) {
                    if (!(productItem instanceof JSONObject)) {
                        throw new PersistenceFileNotFoundException(
                            "Error: Unexpected product entry in providers.json."
                        );
                    }
                    JSONObject offerObj = (JSONObject) productItem;
                    String productId = readString(offerObj.get("product_id"));
                    Number priceNum = readNumber(offerObj.get("selling_price"));
                    Number unitsNum = readNumber(offerObj.get("units_in_stock"));
                    if (productId == null || priceNum == null || unitsNum == null) {
                        throw new PersistenceFileNotFoundException(
                            "Error: A products_for_sale entry in providers.json is missing required fields."
                        );
                    }
                    offers.add(
                        new ProductForSale(
                            productId,
                            providerId,
                            priceNum.floatValue(),
                            unitsNum.intValue()
                        )
                    );
                }
                providers.add(
                    new Provider(
                        providerId,
                        companyName,
                        cif,
                        contactName,
                        phone,
                        email,
                        offers.toArray(new ProductForSale[0])
                    )
                );
            } catch (ClassCastException e) {
                throw new PersistenceFileNotFoundException(
                    "Error: A provider entry in providers.json has an unexpected field type.",
                    e
                );
            }
        }
        return providers.toArray(new Provider[0]);
    }

    @Override
    public Provider findById(int providerId) throws PersistenceException {
        for (Provider provider : findAll()) {
            if (provider.getProviderId() == providerId) {
                return provider;
            }
        }
        return null;
    }

    @Override
    public Provider[] findByProductId(String productId) throws PersistenceException {
        if (productId == null) {
            return new Provider[0];
        }
        List<Provider> result = new ArrayList<>();
        for (Provider provider : findAll()) {
            for (ProductForSale offer : provider.getProductsForSale()) {
                if (productId.equals(offer.getProductId()) && offer.getUnitsInStock() > 0) {
                    result.add(provider);
                    break;
                }
            }
        }
        return result.toArray(new Provider[0]);
    }

    @Override
    public void updateStock(int providerId, String productId, int units)
        throws PersistenceException {
        Provider[] providers = findAll();
        boolean found = false;
        for (Provider provider : providers) {
            if (provider.getProviderId() == providerId) {
                for (ProductForSale offer : provider.getProductsForSale()) {
                    if (productId.equals(offer.getProductId())) {
                        offer.decreaseStock(units);
                        found = true;
                    }
                }
            }
        }
        if (!found) {
            throw new PersistenceWriteException(
                "Error: Provider " + providerId + " / product " + productId + " not found."
            );
        }
        persistProviderToJSON(providers);
    }

    @SuppressWarnings("unchecked")
    private void persistProviderToJSON(Provider[] providers) throws PersistenceWriteException {
        JSONArray array = new JSONArray();
        for (Provider provider : providers) {
            JSONObject providerObj = new JSONObject();
            providerObj.put("provider_id", (long) provider.getProviderId());
            providerObj.put("company_name", provider.getCompanyName());
            providerObj.put("cif", provider.getCif());
            providerObj.put("contact_name", provider.getContactName());
            providerObj.put("phone", provider.getPhone());
            providerObj.put("email", provider.getEmail());
            JSONArray productsArray = new JSONArray();
            for (ProductForSale offer : provider.getProductsForSale()) {
                JSONObject offerObj = new JSONObject();
                offerObj.put("product_id", offer.getProductId());
                offerObj.put("selling_price", (double) offer.getSellingPrice());
                offerObj.put("units_in_stock", (long) offer.getUnitsInStock());
                productsArray.add(offerObj);
            }
            providerObj.put("products_for_sale", productsArray);
            array.add(providerObj);
        }
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writer.write(array.toJSONString());
        } catch (IOException e) {
            throw new PersistenceWriteException("Error: Could not write to providers.json.", e);
        }
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
