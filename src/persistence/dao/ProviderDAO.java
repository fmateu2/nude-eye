package persistence.dao;

import business.Provider;
import persistence.exception.PersistenceException;

/**
 * DAO interface for provider persistence operations.
 * Implemented by {@link persistence.impl.ProviderJSONDAO} and {@link persistence.impl.ProviderAPIDAO}.
 */
public interface ProviderDAO {
    /**
     * Returns all providers.
     *
     * @return array of all {@link business.Provider} objects
     * @throws PersistenceException if the data source cannot be read
     */
    Provider[] findAll() throws PersistenceException;

    /**
     * Returns the provider with the given identifier, or {@code null} if not found.
     *
     * @param providerId the provider ID to search for
     * @return matching {@link business.Provider}, or {@code null}
     * @throws PersistenceException if the data source cannot be read
     */
    Provider findById(int providerId) throws PersistenceException;

    /**
     * Returns all providers that offer the product with the given ID and have stock available.
     *
     * @param productId the product ID to filter by
     * @return array of matching {@link business.Provider} objects
     * @throws PersistenceException if the data source cannot be read
     */
    Provider[] findByProductId(String productId) throws PersistenceException;

    /**
     * Decreases the stock of the given product at the given provider by the specified units.
     *
     * @param providerId the provider whose stock should be updated
     * @param productId  the product ID whose stock should decrease
     * @param units      number of units to deduct
     * @throws PersistenceException if the provider/product is not found or the write fails
     */
    void updateStock(int providerId, String productId, int units) throws PersistenceException;
}
