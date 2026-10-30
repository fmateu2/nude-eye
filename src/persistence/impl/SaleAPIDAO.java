package persistence.impl;

import business.Sale;
import edu.salle.url.api.ApiHelper;
import edu.salle.url.api.exception.ApiException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import persistence.dao.SaleDAO;
import persistence.exception.PersistenceException;
import persistence.exception.PersistenceFileNotFoundException;
import persistence.exception.PersistenceWriteException;

/**
 * REST API implementation of {@link persistence.dao.SaleDAO}.
 * Reads and writes sale records from/to the group-scoped DPOO API endpoint.
 */
public class SaleAPIDAO implements SaleDAO {

    private static final String BASE_URL = System.getProperty("nudeeye.api.url", "http://localhost:8080");

    private final ApiHelper apiHelper;
    private final String groupId;

    public SaleAPIDAO(String groupId) throws PersistenceException {
        try {
            this.apiHelper = new ApiHelper();
        } catch (ApiException e) {
            throw new PersistenceException("Error: The sales API can't be initialized.", e);
        }
        this.groupId = groupId;
    }

    private String getSalesUrl() {
        return BASE_URL + "/" + groupId + "/sales";
    }

    private JSONArray readArrayFromAPI(String url) throws PersistenceException {
        try {
            String response = apiHelper.getFromUrl(url);
            Object parsed = new JSONParser().parse(response);
            if (!(parsed instanceof JSONArray)) {
                throw new PersistenceFileNotFoundException(
                    "Error: The sales API response has an invalid structure."
                );
            }
            return (JSONArray) parsed;
        } catch (ApiException e) {
            throw new PersistenceException("Error: The sales API can't be accessed.", e);
        } catch (ParseException e) {
            throw new PersistenceFileNotFoundException(
                "Error: The sales API returned invalid JSON.",
                e
            );
        }
    }

    @Override
    public void save(Sale sale) throws PersistenceException {
        try {
            apiHelper.postToUrl(getSalesUrl(), saleToJSON(sale));
        } catch (ApiException e) {
            throw new PersistenceWriteException("Error: Could not save sale through API.", e);
        }
    }

    @Override
    public Sale[] findByClientId(int clientId) throws PersistenceException {
        JSONArray array = readArrayFromAPI(getSalesUrl() + "?client_id=" + clientId);
        List<Sale> sales = new ArrayList<>();
        for (Object item : array) {
            if (!(item instanceof JSONObject)) {
                throw new PersistenceFileNotFoundException(
                    "Error: Unexpected element in sales API response."
                );
            }
            JSONObject saleObj = (JSONObject) item;
            try {
                Number clientIdNum = readNumber(saleObj.get("client_id"));
                String productId = readString(saleObj.get("product_id"));
                Number priceNum = readNumber(saleObj.get("price_paid"));
                if (priceNum == null) {
                    priceNum = readNumber(saleObj.get("price"));
                }
                Number dateNum = readNumber(saleObj.get("purchase_date"));
                if (
                    clientIdNum == null || productId == null || priceNum == null || dateNum == null
                ) {
                    throw new PersistenceFileNotFoundException(
                        "Error: A sale entry from the API is missing required fields."
                    );
                }
                long timestamp = dateNum.longValue();
                if (timestamp < 10000000000L) {
                    timestamp *= 1000L;
                }
                sales.add(
                    new Sale(
                        clientIdNum.intValue(),
                        productId,
                        priceNum.floatValue(),
                        new Date(timestamp)
                    )
                );
            } catch (ClassCastException e) {
                throw new PersistenceFileNotFoundException(
                    "Error: A sale entry from the API has an unexpected field type.",
                    e
                );
            }
        }
        sales.sort(Comparator.comparing(Sale::getPurchaseDate).reversed());
        return sales.toArray(new Sale[0]);
    }

    @SuppressWarnings("unchecked")
    private String saleToJSON(Sale sale) {
        JSONObject saleObj = new JSONObject();
        saleObj.put("client_id", (long) sale.getClientId());
        saleObj.put("product_id", sale.getProductId());
        saleObj.put("price_paid", (double) sale.getPricePaid());
        saleObj.put("purchase_date", sale.getPurchaseDate().getTime() / 1000L);
        return saleObj.toJSONString();
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
}
