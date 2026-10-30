package persistence.impl;

import business.ProductForSale;
import business.Provider;
import edu.salle.url.api.ApiHelper;
import edu.salle.url.api.exception.ApiException;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import persistence.dao.ProviderDAO;
import persistence.exception.PersistenceException;
import persistence.exception.PersistenceFileNotFoundException;

/**
 * REST API implementation of {@link persistence.dao.ProviderDAO}.
 * Fetches provider data from the shared DPOO API.
 * Note: {@link #updateStock} is a no-op in this implementation (API is read-only for stock).
 */
public class ProviderAPIDAO implements ProviderDAO {

    private static final String BASE_URL = System.getProperty("nudeeye.api.url", "http://localhost:8080");

    private final ApiHelper apiHelper;

    public ProviderAPIDAO() throws PersistenceException {
        try {
            this.apiHelper = new ApiHelper();
        } catch (ApiException e) {
            throw new PersistenceException("Error: The providers API can't be initialized.", e);
        }
    }

    private JSONArray readArrayFromAPI(String url) throws PersistenceException {
        try {
            String response = apiHelper.getFromUrl(url);
            Object parsed = new JSONParser().parse(response);
            if (!(parsed instanceof JSONArray)) {
                throw new PersistenceFileNotFoundException(
                    "Error: The providers API response has an invalid structure."
                );
            }
            return (JSONArray) parsed;
        } catch (ApiException e) {
            throw new PersistenceException("Error: The providers API can't be accessed.", e);
        } catch (ParseException e) {
            throw new PersistenceFileNotFoundException(
                "Error: The providers API returned invalid JSON.",
                e
            );
        }
    }

    @Override
    public Provider[] findAll() throws PersistenceException {
        JSONArray array = readArrayFromAPI(BASE_URL + "/shared/providers");
        List<Provider> providers = new ArrayList<>();
        for (Object item : array) {
            if (!(item instanceof JSONObject)) {
                throw new PersistenceFileNotFoundException(
                    "Error: Unexpected element in providers API response."
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
                        "Error: A provider entry from the API is missing required fields."
                    );
                }
                int providerId = idNum.intValue();
                List<ProductForSale> offers = new ArrayList<>();
                for (Object productItem : products) {
                    if (!(productItem instanceof JSONObject)) {
                        throw new PersistenceFileNotFoundException(
                            "Error: Unexpected product entry in providers API response."
                        );
                    }
                    JSONObject offerObj = (JSONObject) productItem;
                    String productId = readString(offerObj.get("product_id"));
                    Number priceNum = readNumber(offerObj.get("selling_price"));
                    Number unitsNum = readNumber(offerObj.get("units_in_stock"));
                    if (productId == null || priceNum == null || unitsNum == null) {
                        throw new PersistenceFileNotFoundException(
                            "Error: A products_for_sale entry from the API is missing required fields."
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
                    "Error: A provider entry from the API has an unexpected field type.",
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
        throws PersistenceException {}

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
