package persistence.dao;

import business.Product;
import persistence.exception.PersistenceException;

/**
 * DAO interface for product catalogue persistence operations.
 * Implemented by {@link persistence.impl.ProductJSONDAO} and {@link persistence.impl.ProductAPIDAO}.
 */
public interface ProductDAO {
    /**
     * Returns all products in the catalogue.
     *
     * @return array of all {@link business.Product} objects
     * @throws PersistenceException if the data source cannot be read
     */
    Product[] findAll() throws PersistenceException;

    /**
     * Returns the product with the given identifier, or {@code null} if not found.
     *
     * @param productId the product ID to search for
     * @return matching {@link business.Product}, or {@code null}
     * @throws PersistenceException if the data source cannot be read
     */
    Product findById(String productId) throws PersistenceException;

    /**
     * Returns all products whose name contains the given search string (case-insensitive).
     *
     * @param name the search term (passing {@code null} or blank returns all products)
     * @return array of matching {@link business.Product} objects
     * @throws PersistenceException if the data source cannot be read
     */
    Product[] findByName(String name) throws PersistenceException;
}
