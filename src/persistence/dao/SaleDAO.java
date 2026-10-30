package persistence.dao;

import business.Sale;
import persistence.exception.PersistenceException;

/**
 * DAO interface for sale record persistence operations.
 * Implemented by {@link persistence.impl.SaleCSVDAO} and {@link persistence.impl.SaleAPIDAO}.
 */
public interface SaleDAO {
    /**
     * Persists a new sale record to the data source.
     *
     * @param sale the sale to save
     * @throws PersistenceException if the sale cannot be written
     */
    void save(Sale sale) throws PersistenceException;

    /**
     * Returns all sales associated with the given client, ordered by date descending.
     *
     * @param clientId the client whose sales should be retrieved
     * @return array of matching {@link business.Sale} records
     * @throws PersistenceException if the data source cannot be read
     */
    Sale[] findByClientId(int clientId) throws PersistenceException;
}
