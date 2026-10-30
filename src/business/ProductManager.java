package business;

import persistence.dao.ProductDAO;
import persistence.exception.PersistenceException;
import persistence.impl.ProductAPIDAO;
import persistence.impl.ProductJSONDAO;

/**
 * Business-layer facade for product catalogue operations.
 * Delegates persistence to the appropriate {@link persistence.dao.ProductDAO} implementation
 * depending on whether the API or local JSON file mode is active.
 */
public class ProductManager {

    private final ProductDAO productDAO;

    /**
     * Constructs a ProductManager selecting the DAO based on the persistence mode.
     *
     * @param useApi {@code true} to use the remote API DAO; {@code false} for local JSON
     * @throws BusinessException if the DAO cannot be initialised or validated
     */
    public ProductManager(boolean useApi) throws BusinessException {
        this.productDAO = createProductDAO(useApi);
        validateProductDAO();
    }

    /**
     * Constructs a ProductManager with an explicit DAO (for testing or injection).
     *
     * @param productDAO the DAO instance to use
     */
    public ProductManager(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    private ProductDAO createProductDAO(boolean useApi) throws BusinessException {
        if (useApi) {
            try {
                return new ProductAPIDAO();
            } catch (PersistenceException e) {
                throw new BusinessException(e.getMessage(), e);
            }
        }
        return new ProductJSONDAO();
    }

    private void validateProductDAO() throws BusinessException {
        try {
            productDAO.findAll();
        } catch (PersistenceException e) {
            throw new BusinessException(e.getMessage(), e);
        }
    }

    /**
     * Returns all products whose name contains the given search string (case-insensitive).
     *
     * @param productName search term
     * @return array of matching {@link Product} objects
     * @throws BusinessException if the search fails
     */
    public Product[] findByName(String productName) throws BusinessException {
        try {
            return productDAO.findByName(productName);
        } catch (PersistenceException e) {
            throw new BusinessException("Error searching products.", e);
        }
    }

    /**
     * Returns the product with the given identifier.
     *
     * @param productId the product ID to look up
     * @return the matching {@link Product}
     * @throws ProductNotFoundException if no product with that ID exists
     * @throws BusinessException        if access to the data source fails
     */
    public Product findById(String productId) throws BusinessException {
        try {
            Product product = productDAO.findById(productId);
            if (product == null) {
                throw new ProductNotFoundException("Product not found.");
            }
            return product;
        } catch (PersistenceException e) {
            throw new BusinessException("Error accessing product data.", e);
        }
    }

    /**
     * Returns all products in the catalogue.
     *
     * @return array of all {@link Product} objects
     * @throws BusinessException if reading the catalogue fails
     */
    public Product[] getAllProducts() throws BusinessException {
        try {
            return productDAO.findAll();
        } catch (PersistenceException e) {
            throw new BusinessException("Error reading product catalogue.", e);
        }
    }
}
