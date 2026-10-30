package persistence.impl;

import business.Consumable;
import business.ContactLens;
import business.Glasses;
import business.Product;
import business.Service;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import persistence.dao.ProductDAO;
import persistence.exception.PersistenceException;
import persistence.exception.PersistenceFileNotFoundException;

/**
 * JSON-based implementation of {@link persistence.dao.ProductDAO}.
 * Reads product data from {@code products.json} and constructs the appropriate
 * {@link business.Product} subtype based on field heuristics.
 */
public class ProductJSONDAO implements ProductDAO {

    private static final String FILE_PATH = "products.json";

    private JSONArray readJSONArray() throws PersistenceFileNotFoundException {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Object parsed = parser.parse(reader);
            if (!(parsed instanceof JSONArray)) {
                throw new PersistenceFileNotFoundException(
                    "Error: The products.json file has an invalid structure."
                );
            }
            return (JSONArray) parsed;
        } catch (IOException e) {
            throw new PersistenceFileNotFoundException(
                "Error: The products.json file can't be accessed.",
                e
            );
        } catch (ParseException e) {
            throw new PersistenceFileNotFoundException(
                "Error: The products.json file contains invalid JSON.",
                e
            );
        }
    }

    @Override
    public Product[] findAll() throws PersistenceException {
        JSONArray array = readJSONArray();
        List<Product> products = new ArrayList<>();
        for (Object item : array) {
            if (!(item instanceof JSONObject)) {
                throw new PersistenceFileNotFoundException(
                    "Error: Unexpected element in products.json array."
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
                products.add(
                    buildProduct(
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
                    )
                );
            } catch (ClassCastException e) {
                throw new PersistenceFileNotFoundException(
                    "Error: A product entry in products.json has an unexpected field type.",
                    e
                );
            } catch (java.time.format.DateTimeParseException e) {
                throw new PersistenceFileNotFoundException(
                    "Error: A consumable entry in products.json has an invalid expiration date.",
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
                "Error: A product entry in products.json is missing required fields."
            );
        }
        if (isService(type, productId, completionTime)) {
            if (completionTime == null) {
                throw new PersistenceFileNotFoundException(
                    "Error: A service entry in products.json is missing completion time."
                );
            }
            return new Service(productId, productName, completionTime.doubleValue());
        }
        if (brand == null || model == null) {
            throw new PersistenceFileNotFoundException(
                "Error: A physical product entry in products.json is missing brand or model."
            );
        }
        if (isContactLens(type, productId, prescription, numberOfLenses)) {
            if (prescription == null || numberOfLenses == null || uvFilter == null) {
                throw new PersistenceFileNotFoundException(
                    "Error: A contact lens entry in products.json is missing required fields."
                );
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
            if (volume == null || expirationText == null || preservatives == null) {
                throw new PersistenceFileNotFoundException(
                    "Error: A consumable entry in products.json is missing required fields."
                );
            }
            return new Consumable(
                productId,
                productName,
                brand,
                model,
                volume.doubleValue(),
                LocalDate.parse(expirationText),
                preservatives
            );
        }
        return new Glasses(productId, productName, brand, model);
    }

    @Override
    public Product findById(String productId) throws PersistenceException {
        if (productId == null) {
            return null;
        }
        for (Product product : findAll()) {
            if (productId.equals(product.getProductId())) {
                return product;
            }
        }
        return null;
    }

    @Override
    public Product[] findByName(String name) throws PersistenceException {
        Product[] all = findAll();
        if (name == null || name.isBlank()) {
            return all;
        }
        String lower = name.toLowerCase();
        List<Product> result = new ArrayList<>();
        for (Product product : all) {
            if (product.getProductName().toLowerCase().contains(lower)) {
                result.add(product);
            }
        }
        return result.toArray(new Product[0]);
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
