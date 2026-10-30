package business;

import geolocationAPI.GeolocationApiManager;
import java.util.Date;
import persistence.dao.ProviderDAO;
import persistence.dao.SaleDAO;
import persistence.exception.PersistenceException;
import persistence.impl.ProviderAPIDAO;
import persistence.impl.ProviderJSONDAO;
import persistence.impl.SaleAPIDAO;
import persistence.impl.SaleCSVDAO;

/**
 * Business-layer facade for shopping cart management and sale checkout.
 * Handles item addition, price calculation (including geolocation-based shipping),
 * checkout, and stock update.
 */
public class SaleManager {

    private static final String GROUP_ID = "5";

    private final SaleDAO saleDAO;
    private final ProviderDAO providerDAO;
    private final ProductManager productManager;
    private final GeolocationApiManager geolocationApiManager;
    private final ShoppingCart cart;

    /**
     * Constructs a SaleManager selecting DAOs based on the persistence mode.
     *
     * @param useApi         {@code true} to use the remote API DAOs; {@code false} for local files
     * @param productManager the product manager used to resolve product details
     * @throws BusinessException if any DAO cannot be initialised
     */
    public SaleManager(boolean useApi, ProductManager productManager) throws BusinessException {
        this.saleDAO = createSaleDAO(useApi);
        this.providerDAO = createProviderDAO(useApi);
        this.productManager = productManager;
        this.geolocationApiManager = new GeolocationApiManager();
        this.cart = new ShoppingCart();
    }

    /**
     * Constructs a SaleManager with explicit dependencies (for testing or injection).
     *
     * @param saleDAO               the sale DAO to use
     * @param providerDAO           the provider DAO to use for stock updates
     * @param productManager        the product manager to use
     * @param geolocationApiManager the geolocation service for shipping calculations
     */
    public SaleManager(
        SaleDAO saleDAO,
        ProviderDAO providerDAO,
        ProductManager productManager,
        GeolocationApiManager geolocationApiManager
    ) {
        this.saleDAO = saleDAO;
        this.providerDAO = providerDAO;
        this.productManager = productManager;
        this.geolocationApiManager = geolocationApiManager;
        this.cart = new ShoppingCart();
    }

    private SaleDAO createSaleDAO(boolean useApi) throws BusinessException {
        if (useApi) {
            try {
                return new SaleAPIDAO(GROUP_ID);
            } catch (PersistenceException e) {
                throw new BusinessException(e.getMessage(), e);
            }
        }
        return new SaleCSVDAO();
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

    /**
     * Returns the current session shopping cart.
     *
     * @return the active {@link ShoppingCart}
     */
    public ShoppingCart getCart() {
        return cart;
    }

    /**
     * Adds a product offer to the cart, resolving the product details automatically.
     *
     * @param item   the offer to add
     * @param client the client adding the item (used for purchase eligibility check)
     * @throws BusinessException    if the client cannot buy the product
     * @throws OutOfStockException  if the product has no remaining stock
     */
    public void addItem(ProductForSale item, Client client) throws BusinessException {
        Product product = productManager.findById(item.getProductId());
        addItem(item, client, product);
    }

    /**
     * Adds a product offer to the cart with an already-resolved product object.
     *
     * @param item    the offer to add
     * @param client  the client adding the item
     * @param product the resolved product (may be {@code null} to skip type check)
     * @throws BusinessException    if the client cannot buy the product
     * @throws OutOfStockException  if the product has no remaining stock
     */
    public void addItem(ProductForSale item, Client client, Product product)
        throws BusinessException {
        if (item == null) {
            throw new BusinessException("Selected provider offer is not available.");
        }
        if (item.getUnitsInStock() <= 0) {
            throw new OutOfStockException("This product is out of stock.");
        }
        if (product != null && !client.canBuy(product)) {
            throw new BusinessException("This client cannot buy this product.");
        }
        cart.addItem(item);
    }

    /**
     * Removes the item with the given product ID from the cart.
     *
     * @param productId the ID of the product to remove
     */
    public void removeFromCart(String productId) {
        cart.remove(productId);
    }

    /**
     * Calculates the full cart total for the given client, including VAT, discounts,
     * and any client-specific surcharges (e.g. shipping for online clients).
     *
     * @param client the client for whom the total is calculated
     * @return total price in euros
     * @throws BusinessException if price calculation or geolocation fails
     */
    public float calculateTotal(Client client) throws BusinessException {
        float total = 0;
        for (ProductForSale item : cart.getItems()) {
            Product product = findProductOrNull(item.getProductId());
            total += calculateItemPrice(item, client, product);
        }
        return client.applyConstraintRules(total, geolocationApiManager);
    }

    /**
     * Calculates the price for a single cart item, applying the correct VAT and discounts
     * based on the product type and client.
     *
     * @param item    the product offer
     * @param client  the purchasing client
     * @param product the resolved product, or {@code null} to fall back to a generic calculation
     * @return item price in euros
     */
    public float calculateItemPrice(ProductForSale item, Client client, Product product) {
        Product resolvedProduct = product;
        if (resolvedProduct == null) {
            resolvedProduct = findProductOrNull(item.getProductId());
        }
        if (resolvedProduct != null) {
            return resolvedProduct.calculatePrice(item.getSellingPrice(), client.appliesVAT());
        }
        if (client.appliesVAT()) {
            return item.getSellingPrice() * PricingConstants.STANDARD_VAT_MULTIPLIER;
        }
        return item.getSellingPrice();
    }

    /**
     * Processes the checkout: validates each item, persists the sales, updates provider
     * stock, and clears the cart.
     *
     * @param client the client completing the purchase
     * @throws BusinessException if any item cannot be purchased, or if persistence fails
     */
    public void checkout(Client client) throws BusinessException {
        try {
            for (ProductForSale item : cart.getItems()) {
                Product product = findProductOrNull(item.getProductId());
                if (product != null && !client.canBuy(product)) {
                    throw new BusinessException("This client cannot buy this product.");
                }
                Sale sale = createSale(client, item, product);
                saleDAO.save(sale);
                providerDAO.updateStock(item.getProviderId(), item.getProductId(), 1);
            }
            cart.clear();
        } catch (PersistenceException e) {
            throw new BusinessException("Error during checkout.", e);
        }
    }

    private Sale createSale(Client client, ProductForSale item, Product product) {
        float finalPrice = calculateItemPrice(item, client, product);
        return new Sale(client.getClientId(), item.getProductId(), finalPrice, new Date());
    }

    private Product findProductOrNull(String productId) {
        try {
            return productManager.findById(productId);
        } catch (BusinessException e) {
            return null;
        }
    }
}
