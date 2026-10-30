package persistence.dao;

import business.Client;
import persistence.exception.PersistenceException;

/**
 * DAO interface for client persistence operations.
 * Implemented by {@link persistence.impl.ClientJSONDAO} and {@link persistence.impl.ClientAPIDAO}.
 */
public interface ClientDAO {
    /**
     * Returns all persisted clients.
     *
     * @return array of all {@link business.Client} objects
     * @throws PersistenceException if the data source cannot be read
     */
    Client[] findAll() throws PersistenceException;

    /**
     * Persists a new client to the data source.
     *
     * @param client the client to save
     * @throws PersistenceException if the client cannot be written
     */
    void save(Client client) throws PersistenceException;

    /**
     * Returns the client with the given identifier, or {@code null} if not found.
     *
     * @param clientId the client ID to search for
     * @return matching {@link business.Client}, or {@code null}
     * @throws PersistenceException if the data source cannot be read
     */
    Client findById(int clientId) throws PersistenceException;
}
