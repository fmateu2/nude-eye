package presentation;

import business.BusinessException;
import business.Client;
import business.ClientManager;
import business.PhoneNumber;
import business.Product;
import business.ProductForSale;
import business.ProductManager;
import business.Provider;
import business.ProviderManager;
import business.Sale;
import business.SaleManager;
import business.ShoppingCart;
import edu.salle.url.api.ApiHelper;
import edu.salle.url.api.exception.ApiException;

/**
 * Main application controller coordinating the presentation and business layers.
 * Initialises managers, handles the authentication flow, and dispatches all
 * user interactions to the appropriate business operations.
 */
public class Controller {

    private static final String BASE_URL = System.getProperty("nudeeye.api.url", "http://localhost:8080");

    private UserInterface ui;
    private ClientManager clientManager;
    private ProductManager productManager;
    private ProviderManager providerManager;
    private SaleManager saleManager;
    private boolean ready;

    /**
     * Default constructor: shows the banner, detects persistence mode, and initialises
     * all business managers accordingly.
     */
    public Controller() {
        this.ui = new UserInterface();
        ui.showBanner();
        boolean useApi = determinePersistenceMode();
        initializeManagers(useApi);
    }

    /**
     * Dependency-injection constructor for testing or direct wiring.
     *
     * @param ui              the user interface to use
     * @param clientManager   the client manager to use
     * @param productManager  the product manager to use
     * @param providerManager the provider manager to use
     * @param saleManager     the sale manager to use
     */
    public Controller(
        UserInterface ui,
        ClientManager clientManager,
        ProductManager productManager,
        ProviderManager providerManager,
        SaleManager saleManager
    ) {
        this.ui = ui;
        this.clientManager = clientManager;
        this.productManager = productManager;
        this.providerManager = providerManager;
        this.saleManager = saleManager;
        this.ready = true;
    }

    /**
     * Probes the remote API to determine whether cloud persistence is available.
     *
     * @return {@code true} if the API is reachable; {@code false} to fall back to local files
     */
    private boolean determinePersistenceMode() {
        ui.showMessage("Checking API status...");
        try {
            ApiHelper apiHelper = new ApiHelper();
            apiHelper.getFromUrl(BASE_URL + "/shared/products");
            ui.showMessage("API OK.");
            ui.showMessage("Starting program with cloud persistence...");
            return true;
        } catch (ApiException e) {
            ui.showMessage("Error: The API isn't available.");
            ui.showMessage("Verifying local files...");
            return false;
        }
    }

    /**
     * Instantiates all business managers in the given persistence mode.
     * Falls back to local mode automatically if API initialisation fails.
     *
     * @param useApi {@code true} for API mode; {@code false} for local file mode
     */
    private void initializeManagers(boolean useApi) {
        try {
            createManagers(useApi);
            if (!useApi) {
                ui.showMessage("Files OK.");
                ui.showMessage("Starting program with local persistence...");
            }
            ready = true;
        } catch (BusinessException e) {
            if (useApi) {
                ui.showMessage("Error: API persistence could not be initialized.");
                ui.showMessage("Verifying local files...");
                initializeManagers(false);
                return;
            }
            ui.showMessage(e.getMessage());
            ui.showMessage("Shutting down...");
            ready = false;
        }
    }

    /**
     * Creates and assigns all business manager instances.
     *
     * @param useApi {@code true} for API mode; {@code false} for local file mode
     * @throws BusinessException if any manager cannot be initialised
     */
    private void createManagers(boolean useApi) throws BusinessException {
        this.productManager = new ProductManager(useApi);
        this.providerManager = new ProviderManager(useApi);
        this.clientManager = new ClientManager(useApi);
        this.saleManager = new SaleManager(useApi, productManager);
    }

    /**
     * Starts the main application loop, presenting the authentication menu until the
     * user chooses to exit.
     */
    public void run() {
        if (!ready) {
            return;
        }

        boolean exit = false;
        while (!exit) {
            ui.showAuthMenu();
            int option = ui.intInput("\nChoose an option: ");
            switch (option) {
                case 1:
                    handleLogin();
                    break;
                case 2:
                    handleRegister();
                    break;
                case 0:
                    exit = true;
                    ui.showMessage("We hope to see you again!");
                    break;
                default:
                    ui.showMessage("Invalid option.");
            }
        }
    }

    private void handleLogin() {
        try {
            int clientId = ui.intInput("Enter client ID: ");
            Client client = clientManager.login(clientId);
            handleMainMenu(client);
        } catch (BusinessException e) {
            ui.showMessage(e.getMessage());
        }
    }

    private void handleRegister() {
        try {
            String fullName = ui.stringInput("Enter full name / company name: ");
            PhoneNumber[] phones = askPhoneNumbers();
            ui.showClientTypeMenu();
            int clientType = ui.intInput("Choose client type: ");
            Client client;
            switch (clientType) {
                case 1:
                    client = clientManager.registerInPerson(fullName, phones);
                    break;
                case 2:
                    client = registerOnlineClient(fullName, phones);
                    break;
                case 3:
                    client = registerCorporateClient(fullName, phones);
                    break;
                default:
                    ui.showMessage("Invalid client type.");
                    return;
            }
            handleMainMenu(client);
        } catch (BusinessException e) {
            ui.showMessage(e.getMessage());
        }
    }

    private PhoneNumber[] askPhoneNumbers() {
        int numPhones = ui.intInput("How many phone numbers? ");
        PhoneNumber[] phones = new PhoneNumber[numPhones];
        for (int i = 0; i < numPhones; i++) {
            String countryPrefix = ui.stringInput("Country prefix: ");
            String number = ui.stringInput("Phone number: ");
            phones[i] = new PhoneNumber(countryPrefix, number);
        }
        return phones;
    }

    private Client registerOnlineClient(String fullName, PhoneNumber[] phones)
        throws BusinessException {
        String email = ui.stringInput("Email: ");
        ui.showMessage("Shipping address:");
        String street = ui.stringInput("Street: ");
        int number = ui.intInput("Number: ");
        String city = ui.stringInput("City: ");
        String country = ui.stringInput("Country: ");
        String postalCode = ui.stringInput("Postal code: ");
        return clientManager.registerOnline(
            fullName,
            phones,
            email,
            street,
            number,
            city,
            country,
            postalCode
        );
    }

    private Client registerCorporateClient(String companyName, PhoneNumber[] phones)
        throws BusinessException {
        String nif = ui.stringInput("NIF/CIF: ");
        String contactPerson = ui.stringInput("Contact person: ");
        ui.showMessage("Billing address:");
        String billingStreet = ui.stringInput("Street: ");
        int billingNumber = ui.intInput("Number: ");
        String billingCity = ui.stringInput("City: ");
        String billingCountry = ui.stringInput("Country: ");
        String billingPostalCode = ui.stringInput("Postal code: ");
        ui.showMessage("Shipping address:");
        String shippingStreet = ui.stringInput("Street: ");
        int shippingNumber = ui.intInput("Number: ");
        String shippingCity = ui.stringInput("City: ");
        String shippingCountry = ui.stringInput("Country: ");
        String shippingPostalCode = ui.stringInput("Postal code: ");
        return clientManager.registerCorporate(
            companyName,
            phones,
            nif,
            contactPerson,
            billingStreet,
            billingNumber,
            billingCity,
            billingCountry,
            billingPostalCode,
            shippingStreet,
            shippingNumber,
            shippingCity,
            shippingCountry,
            shippingPostalCode
        );
    }

    private void handleMainMenu(Client client) {
        boolean logout = false;
        while (!logout) {
            ui.showMainMenu(client.getFullName());
            int option = ui.intInput("\nChoose an option: ");
            switch (option) {
                case 1:
                    showUserProfile(client);
                    break;
                case 2:
                    findProductsByName(client);
                    break;
                case 3:
                    findProductsByProvider(client);
                    break;
                case 4:
                    showCart(client);
                    break;
                case 0:
                    clientManager.logout();
                    logout = true;
                    ui.showMessage("Logged out.");
                    break;
                default:
                    ui.showMessage("Invalid option.");
            }
        }
    }

    private void showUserProfile(Client client) {
        try {
            PhoneNumber[] phones = client.getPhoneNumbers();
            String[] phoneLines = new String[phones.length];
            for (int i = 0; i < phones.length; i++) {
                phoneLines[i] = "\t" + phones[i].toString();
            }
            Sale[] history = clientManager.getPurchaseHistory(client.getClientId());
            int limit = Math.min(history.length, 10);
            String[] historyLines = buildHistoryLines(history, limit);
            ui.showUserProfile(
                client.getClientId(),
                client.getFullName(),
                phoneLines,
                client.getSpecificProfileLines(),
                historyLines
            );
        } catch (BusinessException e) {
            ui.showMessage(e.getMessage());
        }
    }

    private String[] buildHistoryLines(Sale[] history, int limit) {
        if (limit == 0) {
            return new String[] { "No purchases yet." };
        }
        String[] historyLines = new String[limit];
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy - HH:mm");
        for (int i = 0; i < limit; i++) {
            Sale sale = history[i];
            String productDescription = sale.getProductId();
            Product product = findProductSafely(sale.getProductId());
            if (product != null) {
                productDescription = formatProductName(product);
            }
            historyLines[i] =
                "\t" +
                productDescription +
                " - € " +
                String.format("%.2f", sale.getPricePaid()) +
                " - " +
                sdf.format(sale.getPurchaseDate());
        }
        return historyLines;
    }

    private void findProductsByName(Client client) {
        try {
            String input = ui.stringInput("Search criteria: ");
            Product[] products = productManager.findByName(input);
            String[] productLines = new String[products.length];
            for (int i = 0; i < products.length; i++) {
                productLines[i] = "\t" + (i + 1) + ") " + products[i].getProductName();
            }
            ui.showProductList(productLines);
            if (products.length == 0) {
                return;
            }
            int option = ui.intInput("Choose an option: ");
            if (option == 0) {
                return;
            }
            if (option < 1 || option > products.length) {
                ui.showMessage("Invalid option.");
                return;
            }
            showProductDetail(products[option - 1], client);
        } catch (BusinessException e) {
            ui.showMessage(e.getMessage());
        }
    }

    private void showProductDetail(Product product, Client client) {
        try {
            Provider[] providers = providerManager.findByProductId(product.getProductId());
            String[][] providerLines = new String[providers.length][3];
            for (int i = 0; i < providers.length; i++) {
                ProductForSale offer = findOfferInProvider(providers[i], product.getProductId());
                providerLines[i][0] = "\t" + (i + 1) + ") " + providers[i].getCompanyName();
                if (offer == null) {
                    providerLines[i][1] = "\t\t- Sale price: unavailable,";
                    providerLines[i][2] = "\t\t- Available stock: unavailable";
                } else {
                    providerLines[i][1] = "\t\t- Sale price: " + offer.getSellingPrice() + "€,";
                    providerLines[i][2] = "\t\t- Available stock: " + offer.getUnitsInStock();
                }
            }
            ui.showProductDetail(
                product.getProductId(),
                product.getProductName(),
                product.getBrand(),
                product.getModel(),
                product.getProductType(),
                product.getSpecificInfoLines(),
                providerLines
            );
            if (providers.length > 0) {
                handleProductPurchase(product, providers, client);
            }
        } catch (BusinessException e) {
            ui.showMessage(e.getMessage());
        }
    }

    private void handleProductPurchase(Product product, Provider[] providers, Client client) {
        String answer = ui.stringInput("\nDo you want to add this product to the shopping cart? ");
        if (!"yes".equalsIgnoreCase(answer)) {
            return;
        }
        int providerOption = ui.intInput("Choose a provider: ");
        if (providerOption < 1 || providerOption > providers.length) {
            ui.showMessage("Invalid option.");
            return;
        }
        Provider selectedProvider = providers[providerOption - 1];
        ProductForSale offer = findOfferInProvider(selectedProvider, product.getProductId());
        if (offer == null) {
            ui.showMessage("Selected provider offer is not available.");
            return;
        }
        try {
            saleManager.addItem(offer, client, product);
            ui.showMessage("Product added to shopping cart.");
        } catch (BusinessException e) {
            ui.showMessage(e.getMessage());
        }
    }

    private void findProductsByProvider(Client client) {
        try {
            Provider[] providers = providerManager.getAllProductsForSale();
            String[] providerLines = new String[providers.length];
            for (int i = 0; i < providers.length; i++) {
                providerLines[i] = "\t" + (i + 1) + ") " + providers[i].getCompanyName();
            }
            ui.showProviderList(providerLines);
            if (providers.length == 0) {
                return;
            }
            int option = ui.intInput("\nChoose an option: ");
            if (option == 0) {
                return;
            }
            if (option < 1 || option > providers.length) {
                ui.showMessage("Invalid option.");
                return;
            }
            showProviderDetail(providers[option - 1], client);
        } catch (BusinessException e) {
            ui.showMessage(e.getMessage());
        }
    }

    private void showProviderDetail(Provider provider, Client client) {
        ProductForSale[] offers = provider.getProductsForSale();
        String[][] productLines = new String[offers.length][3];
        Product[] resolvedProducts = new Product[offers.length];
        for (int i = 0; i < offers.length; i++) {
            ProductForSale offer = offers[i];
            resolvedProducts[i] = findProductSafely(offer.getProductId());
            String productDescription = offer.getProductId();
            if (resolvedProducts[i] != null) {
                productDescription =
                    offer.getProductId() + " - " + resolvedProducts[i].getProductName();
            }
            productLines[i][0] = "\t" + (i + 1) + ") " + productDescription;
            productLines[i][1] = "\t\t- Sale price: " + offer.getSellingPrice() + "€,";
            productLines[i][2] = "\t\t- Available stock: " + offer.getUnitsInStock();
        }
        ui.showProviderDetail(
            provider.getCompanyName(),
            provider.getCif(),
            provider.getContactName(),
            provider.getPhone(),
            provider.getEmail(),
            productLines
        );
        if (offers.length == 0) {
            return;
        }
        int option = ui.intInput("\nChoose an option: ");
        if (option == 0) {
            return;
        }
        if (option < 1 || option > offers.length) {
            ui.showMessage("Invalid option.");
            return;
        }
        ProductForSale selectedOffer = offers[option - 1];
        Product selectedProduct = resolvedProducts[option - 1];
        String answer = ui.stringInput("\nDo you want to add this product to the shopping cart? ");
        if (!"yes".equalsIgnoreCase(answer)) {
            return;
        }
        try {
            saleManager.addItem(selectedOffer, client, selectedProduct);
            ui.showMessage("Product added to shopping cart.");
        } catch (BusinessException e) {
            ui.showMessage(e.getMessage());
        }
    }

    private void showCart(Client client) {
        try {
            boolean backToMenu = false;
            while (!backToMenu) {
                ShoppingCart cart = saleManager.getCart();
                ProductForSale[] items = cart.getItems();
                String[] cartLines = new String[items.length];
                for (int i = 0; i < items.length; i++) {
                    ProductForSale item = items[i];
                    Product product = findProductSafely(item.getProductId());
                    String productDescription = item.getProductId();
                    if (product != null) {
                        productDescription = item.getProductId() + " - " + product.getProductName();
                    }
                    String providerName = resolveProviderName(item.getProviderId());
                    String leftText = "+ " + productDescription + " (" + providerName + ")";
                    cartLines[i] = String.format(
                        "\t%-55s %8.2f€",
                        leftText,
                        saleManager.calculateItemPrice(item, client, product)
                    );
                }
                float totalPrice = saleManager.calculateTotal(client);
                ui.showCart(cartLines, totalPrice);
                if (cart.isEmpty()) {
                    return;
                }
                ui.showCartMenu();
                int option = ui.intInput("Choose an option: ");
                switch (option) {
                    case 1:
                        removeCartItem(items);
                        break;
                    case 2:
                        saleManager.checkout(client);
                        ui.showMessage("\nThank you for your purchase!");
                        backToMenu = true;
                        break;
                    case 0:
                        backToMenu = true;
                        break;
                    default:
                        ui.showMessage("Invalid option.");
                }
            }
        } catch (BusinessException e) {
            ui.showMessage(e.getMessage());
        }
    }

    private void removeCartItem(ProductForSale[] items) {
        int removeOption = ui.intInput("Choose product number to remove: ");
        if (removeOption < 1 || removeOption > items.length) {
            ui.showMessage("Invalid option.");
            return;
        }
        saleManager.removeFromCart(items[removeOption - 1].getProductId());
        ui.showMessage("Product removed from cart.");
    }

    private String resolveProviderName(int providerId) {
        try {
            Provider provider = providerManager.findById(providerId);
            if (provider != null) {
                return provider.getCompanyName();
            }
        } catch (BusinessException e) {
            return "Provider " + providerId;
        }
        return "Provider " + providerId;
    }

    private ProductForSale findOfferInProvider(Provider provider, String productId) {
        for (ProductForSale offer : provider.getProductsForSale()) {
            if (productId.equals(offer.getProductId())) {
                return offer;
            }
        }
        return null;
    }

    private Product findProductSafely(String productId) {
        try {
            return productManager.findById(productId);
        } catch (BusinessException e) {
            return null;
        }
    }

    private String formatProductName(Product product) {
        if (product.getBrand().isBlank() && product.getModel().isBlank()) {
            return product.getProductName();
        }
        return (
            product.getProductName() + " - " + product.getBrand() + " (" + product.getModel() + ")"
        );
    }
}
