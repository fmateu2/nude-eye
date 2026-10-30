package business;

import persistence.dao.ProviderDAO;
import persistence.exception.PersistenceException;
import persistence.impl.ProviderAPIDAO;
import persistence.impl.ProviderJSONDAO;

/**
 * Business-layer facade for provider and product-offer operations.
 * Selects the appropriate {@link persistence.dao.ProviderDAO} based on persistence mode.
 */
public class ProviderManager {

    private final ProviderDAO providerDAO;

    /**
     * Constructs a ProviderManager selecting the DAO based on the persistence mode.
     *
     * @param useApi {@code true} to use the remote API DAO; {@code false} for local JSON
     * @throws BusinessException if the DAO cannot be initialised or validated
     */
    public ProviderManager(boolean useApi) throws BusinessException {
        this.providerDAO = createProviderDAO(useApi);
        validateProviderDAO();
    }

    /**
     * Constructs a ProviderManager with an explicit DAO (for testing or injection).
     *
     * @param providerDAO the DAO instance to use
     */
    public ProviderManager(ProviderDAO providerDAO) {
        this.providerDAO = providerDAO;
    }

    private ProviderDAO createProviderDAO(boolean useApi) throws BusinessException {
        if (useApi) {
            try {
                return new ProviderAPIDAO();
            } catch (PersistenceException e) {
                throw new BusinessException(e.getMessage(), e);
            }
        }
        return new ProviderJSONDAO();
    }

    private void validateProviderDAO() throws BusinessException {
        try {
            providerDAO.findAll();
        } catch (PersistenceException e) {
            throw new BusinessException(e.getMessage(), e);
        }
    }

    /**
     * Returns all providers that have products available for sale.
     *
     * @return array of all {@link Provider} objects
     * @throws BusinessException if reading providers fails
     */
    public Provider[] getAllProductsForSale() throws BusinessException {
        try {
            return providerDAO.findAll();
        } catch (PersistenceException e) {
            throw new BusinessException("Error reading providers.", e);
        }
    }

    /**
     * Returns the provider with the given identifier, or {@code null} if not found.
     *
     * @param providerId the provider ID to look up
     * @return matching {@link Provider} or {@code null}
     * @throws BusinessException if access to the data source fails
     */
    public Provider findById(int providerId) throws BusinessException {
        try {
            return providerDAO.findById(providerId);
        } catch (PersistenceException e) {
            throw new BusinessException("Error accessing provider data.", e);
        }
    }

    /**
     * Returns all providers that offer the product with the given ID and have stock.
     *
     * @param productId the product ID to search for
     * @return array of matching {@link Provider} objects
     * @throws BusinessException if the search fails
     */
    public Provider[] findByProductId(String productId) throws BusinessException {
        try {
            return providerDAO.findByProductId(productId);
        } catch (PersistenceException e) {
            throw new BusinessException("Error searching providers by product.", e);
        }
    }

    /**
     * Finds a specific offer from a provider for a given product.
     *
     * @param providerId the provider to search within
     * @param productId  the product to look for
     * @return the matching {@link ProductForSale} offer, or {@code null} if not found
     * @throws BusinessException if the lookup fails
     */
    public ProductForSale findOffer(int providerId, String productId) throws BusinessException {
        try {
            Provider provider = providerDAO.findById(providerId);
            if (provider == null) {
                return null;
            }
            for (ProductForSale offer : provider.getProductsForSale()) {
                if (offer.getProductId().equals(productId)) {
                    return offer;
                }
            }
            return null;
        } catch (PersistenceException e) {
            throw new BusinessException("Error finding offer.", e);
        }
    }

    /**
     * Returns all product offers for the given provider.
     *
     * @param providerId the provider whose offers should be retrieved
     * @return array of {@link ProductForSale} offers, or an empty array if the provider is not found
     * @throws BusinessException if the lookup fails
     */
    public ProductForSale[] getProductsForProvider(int providerId) throws BusinessException {
        try {
            Provider provider = providerDAO.findById(providerId);
            if (provider == null) {
                return new ProductForSale[0];
            }
            return provider.getProductsForSale();
        } catch (PersistenceException e) {
            throw new BusinessException("Error reading provider products.", e);
        }
    }
}
