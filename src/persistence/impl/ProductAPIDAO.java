package persistence.impl;

import business.Consumable;
import business.ContactLens;
import business.Glasses;
import business.Product;
import business.Service;
import edu.salle.url.api.ApiHelper;
import edu.salle.url.api.exception.ApiException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import persistence.dao.ProductDAO;
import persistence.exception.PersistenceException;
import persistence.exception.PersistenceFileNotFoundException;

/**
 * REST API implementation of {@link persistence.dao.ProductDAO}.
 * Fetches product data from the shared DPOO API and builds the appropriate
 * {@link business.Product} subtype. Results are cached in memory to reduce API calls.
 */
public class ProductAPIDAO implements ProductDAO {

    private static final String BASE_URL = System.getProperty("nudeeye.api.url", "http://localhost:8080");

    private final ApiHelper apiHelper;
    private final Map<String, Product> productCache;

    public ProductAPIDAO() throws PersistenceException {
        try {
            this.apiHelper = new ApiHelper();
        } catch (ApiException e) {
            throw new PersistenceException("Error: The products API can't be initialized.", e);
        }
        this.productCache = new HashMap<>();
    }

    private JSONArray readArrayFromAPI(String url) throws PersistenceException {
        try {
            String response = apiHelper.getFromUrl(url);
            Object parsed = new JSONParser().parse(response);
            if (!(parsed instanceof JSONArray)) {
                throw new PersistenceFileNotFoundException(
                    "Error: The products API response has an invalid structure."
                );
            }
            return (JSONArray) parsed;
        } catch (ApiException e) {
            throw new PersistenceException("Error: The products API can't be accessed.", e);
        } catch (ParseException e) {
            throw new PersistenceFileNotFoundException(
                "Error: The products API returned invalid JSON.",
                e
            );
        }
    }

    @Override
    public Product[] findAll() throws PersistenceException {
        return readProductsFromAPI(BASE_URL + "/shared/products");
    }

    @Override
    public Product findById(String productId) throws PersistenceException {
        if (productId == null) {
            return null;
        }
        Product cachedProduct = productCache.get(productId);
        if (cachedProduct != null) {
            return cachedProduct;
        }
        for (Product product : findAll()) {
            if (productId.equals(product.getProductId())) {
                return product;
            }
        }
        Product fallback = createFallbackProduct(productId);
        productCache.put(productId, fallback);
        return fallback;
    }

    @Override
    public Product[] findByName(String name) throws PersistenceException {
        if (name == null || name.isBlank()) {
            return findAll();
        }
        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        return readProductsFromAPI(BASE_URL + "/shared/products?product_name=" + encodedName);
    }

    private Product[] readProductsFromAPI(String url) throws PersistenceException {
        JSONArray array = readArrayFromAPI(url);
        List<Product> products = new ArrayList<>();
        for (Object item : array) {
            if (!(item instanceof JSONObject)) {
                throw new PersistenceFileNotFoundException(
                    "Error: Unexpected element in products API response."
                );
            }
            JSONObject jsonObject = (JSONObject) item;
            try {
                String productId = readString(jsonObject.get("product_id"));
                if (productId == null) {
                    productId = readString(jsonObject.get("id"));
                }
                String productName = readString(jsonObject.get("product_name"));
                if (productName == null) {
                    productName = readString(jsonObject.get("name"));
                }
                String type = readString(jsonObject.get("product_type"));
                if (type == null) {
                    type = readString(jsonObject.get("type"));
                }
                String brand = readString(jsonObject.get("brand"));
                String model = readString(jsonObject.get("model"));
                Number completionTime = readNumber(jsonObject.get("completion_time_hours"));
                if (completionTime == null) {
                    completionTime = readNumber(jsonObject.get("completion_time"));
                }
                if (completionTime == null) {
                    completionTime = readNumber(jsonObject.get("hours"));
                }
                Number prescription = readNumber(jsonObject.get("prescription"));
                Number numberOfLenses = readNumber(jsonObject.get("number_of_lenses"));
                if (numberOfLenses == null) {
                    numberOfLenses = readNumber(jsonObject.get("lenses_in_package"));
                }
                Boolean uvFilter = readBoolean(jsonObject.get("uv_filter"));
                if (uvFilter == null) {
                    uvFilter = readBoolean(jsonObject.get("has_uv_filter"));
                }
                Number volume = readNumber(jsonObject.get("volume_ml"));
                if (volume == null) {
                    volume = readNumber(jsonObject.get("volume"));
                }
                String expirationText = readString(jsonObject.get("expiration_date"));
                if (expirationText == null) {
                    expirationText = readString(jsonObject.get("expires_on"));
                }
                Boolean preservatives = readBoolean(jsonObject.get("contains_preservatives"));
                if (preservatives == null) {
                    preservatives = readBoolean(jsonObject.get("preservatives"));
                }
                Product product = buildProduct(
                    productId,
                    productName,
                    type,
                    brand,
                    model,
                    completionTime,
                    prescription,
                    numberOfLenses,
                    uvFilter,
                    volume,
                    expirationText,
                    preservatives
                );
                products.add(product);
                productCache.put(product.getProductId(), product);
            } catch (ClassCastException e) {
                throw new PersistenceFileNotFoundException(
                    "Error: A product entry from the API has an unexpected field type.",
                    e
                );
            } catch (java.time.format.DateTimeParseException e) {
                throw new PersistenceFileNotFoundException(
                    "Error: A consumable entry from the API has an invalid expiration date.",
                    e
                );
            }
        }
        return products.toArray(new Product[0]);
    }

    private Product buildProduct(
        String productId,
        String productName,
        String type,
        String brand,
        String model,
        Number completionTime,
        Number prescription,
        Number numberOfLenses,
        Boolean uvFilter,
        Number volume,
        String expirationText,
        Boolean preservatives
    ) throws PersistenceException {
        if (productId == null || productName == null) {
            throw new PersistenceFileNotFoundException(
                "Error: A product entry from the API is missing required fields."
            );
        }
        if (isService(type, productId, completionTime)) {
            if (completionTime == null) {
                completionTime = 1;
            }
            return new Service(productId, productName, completionTime.doubleValue());
        }
        if (brand == null) {
            brand = "";
        }
        if (model == null) {
            model = "";
        }
        if (isContactLens(type, productId, prescription, numberOfLenses)) {
            if (prescription == null) {
                prescription = 0;
            }
            if (numberOfLenses == null) {
                numberOfLenses = 1;
            }
            if (uvFilter == null) {
                uvFilter = false;
            }
            return new ContactLens(
                productId,
                productName,
                brand,
                model,
                prescription.doubleValue(),
                numberOfLenses.intValue(),
                uvFilter
            );
        }
        if (isConsumable(type, productId, volume, expirationText)) {
            if (volume == null) {
                volume = 0;
            }
            LocalDate expirationDate;
            if (expirationText == null) {
                expirationDate = LocalDate.now().plusMonths(12);
            } else {
                expirationDate = LocalDate.parse(expirationText);
            }
            if (preservatives == null) {
                preservatives = false;
            }
            return new Consumable(
                productId,
                productName,
                brand,
                model,
                volume.doubleValue(),
                expirationDate,
                preservatives
            );
        }
        return new Glasses(productId, productName, brand, model);
    }

    private Product createFallbackProduct(String productId) {
        String name = productId;
        if (productId.startsWith("SRV-")) {
            return new Service(productId, name, 1);
        }
        if (productId.startsWith("CLS-")) {
            return new ContactLens(productId, name, "", "", 0, 1, false);
        }
        if (productId.startsWith("CON-")) {
            return new Consumable(
                productId,
                name,
                "",
                "",
                0,
                LocalDate.now().plusMonths(12),
                false
            );
        }
        return new Glasses(productId, name, "", "");
    }

    private boolean isService(String type, String productId, Number completionTime) {
        return (
            "service".equalsIgnoreCase(type) ||
            productId.startsWith("SRV-") ||
            completionTime != null
        );
    }

    private boolean isContactLens(
        String type,
        String productId,
        Number prescription,
        Number numberOfLenses
    ) {
        return (
            "contact_lens".equalsIgnoreCase(type) ||
            "contact lens".equalsIgnoreCase(type) ||
            productId.startsWith("CLS-") ||
            prescription != null ||
            numberOfLenses != null
        );
    }

    private boolean isConsumable(
        String type,
        String productId,
        Number volume,
        String expirationText
    ) {
        return (
            "consumable".equalsIgnoreCase(type) ||
            productId.startsWith("CON-") ||
            volume != null ||
            expirationText != null
        );
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

    private Boolean readBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return null;
    }
}
