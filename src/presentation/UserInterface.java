package presentation;

import java.util.Scanner;

/**
 * Handles all console input and output for the Nude Eye application.
 * Methods print menus, prompts, and formatted data; input methods read
 * and validate user input.
 */
public class UserInterface {

    private final Scanner sc;

    /**
     * Constructs a UserInterface backed by {@code System.in}.
     */
    public UserInterface() {
        this.sc = new Scanner(System.in);
    }

    /**
     * Prints the application welcome banner to the console.
     */
    public void showBanner() {
        System.out.println("##################################");
        System.out.println("#                                #");
        System.out.println("#     NUDE EYE PROJECT           #");
        System.out.println("#                                #");
        System.out.println("##################################");
        System.out.println();
        System.out.println("Welcome to Nude Eye Project.");
        System.out.println("\"by glass wearers for glass wearers\"");
        System.out.println();
    }

    /**
     * Prints the authentication menu (Login / Sign up / Exit).
     */
    public void showAuthMenu() {
        System.out.println();
        System.out.println("\t1) Login");
        System.out.println("\t2) Sign up");
        System.out.println();
        System.out.println("\t0) Exit");
    }

    /**
     * Prints the client-type selection menu (In-person / Online / Corporate).
     */
    public void showClientTypeMenu() {
        System.out.println("Client type:");
        System.out.println("\t1) In-person client");
        System.out.println("\t2) Online client");
        System.out.println("\t3) Corporate client");
    }

    /**
     * Prints the main navigation menu personalised with the client's name.
     *
     * @param fullName the full name of the logged-in client
     */
    public void showMainMenu(String fullName) {
        System.out.println("Welcome " + fullName + "!");
        System.out.println();
        System.out.println("\t1) User profile");
        System.out.println("\t2) Find products by name");
        System.out.println("\t3) Find products by provider");
        System.out.println("\t4) Shopping cart");
        System.out.println();
        System.out.println("\t0) Logout");
    }

    /**
     * Prints the full user profile including client-type-specific fields.
     *
     * @param clientId      the client's ID
     * @param fullName      the client's full name
     * @param phoneLines    formatted phone number lines
     * @param specificLines client-type-specific info lines
     * @param historyLines  formatted purchase history lines
     */
    public void showUserProfile(
        int clientId,
        String fullName,
        String[] phoneLines,
        String[] specificLines,
        String[] historyLines
    ) {
        System.out.println("--| User Profile |--");
        System.out.println();
        System.out.println("Client ID: " + clientId);
        System.out.println("Full name: " + fullName);
        System.out.println("Phone numbers:");
        for (String line : phoneLines) {
            System.out.println(line);
        }
        for (String line : specificLines) {
            System.out.println(line);
        }
        System.out.println("Shopping history:");
        for (String line : historyLines) {
            System.out.println(line);
        }
    }

    /**
     * Prints the user profile without client-type-specific fields.
     * Delegates to {@link #showUserProfile(int, String, String[], String[], String[])}.
     *
     * @param clientId     the client's ID
     * @param fullName     the client's full name
     * @param phoneLines   formatted phone number lines
     * @param historyLines formatted purchase history lines
     */
    public void showUserProfile(
        int clientId,
        String fullName,
        String[] phoneLines,
        String[] historyLines
    ) {
        showUserProfile(clientId, fullName, phoneLines, new String[0], historyLines);
    }

    /**
     * Prints the product search results list, or a "no products found" message.
     *
     * @param productLines formatted product entry lines
     */
    public void showProductList(String[] productLines) {
        if (productLines.length == 0) {
            System.out.println("No products found.");
            return;
        }
        for (String line : productLines) {
            System.out.println(line);
        }
        System.out.println();
        System.out.println("\t0) Back");
    }

    /**
     * Prints detailed information about a product including its providers and prices.
     *
     * @param productId     unique product identifier
     * @param name          product name
     * @param brand         brand (blank string if not applicable)
     * @param model         model (blank string if not applicable)
     * @param productType   product type label
     * @param specificLines product-type-specific info lines
     * @param providerLines two-dimensional array of provider entry lines
     */
    public void showProductDetail(
        String productId,
        String name,
        String brand,
        String model,
        String productType,
        String[] specificLines,
        String[][] providerLines
    ) {
        System.out.println("Product ID: " + productId + ",");
        System.out.println("Name: " + name + ",");
        if (!brand.isBlank()) {
            System.out.println("Brand: " + brand + ",");
        }
        if (!model.isBlank()) {
            System.out.println("Model: " + model + ",");
        }
        System.out.println("Type: " + productType + ",");
        for (String line : specificLines) {
            System.out.println(line + ",");
        }
        System.out.println("Providers:");
        for (String[] lines : providerLines) {
            for (String line : lines) {
                System.out.println(line);
            }
        }
    }

    /**
     * Prints product details using the default product type and no specific info lines.
     * Delegates to the full overload.
     *
     * @param productId     unique product identifier
     * @param name          product name
     * @param brand         brand
     * @param model         model
     * @param providerLines two-dimensional array of provider entry lines
     */
    public void showProductDetail(
        String productId,
        String name,
        String brand,
        String model,
        String[][] providerLines
    ) {
        showProductDetail(productId, name, brand, model, "Product", new String[0], providerLines);
    }

    /**
     * Prints the list of providers, or a "no providers found" message.
     *
     * @param providerLines formatted provider entry lines
     */
    public void showProviderList(String[] providerLines) {
        if (providerLines.length == 0) {
            System.out.println("No providers found.");
            return;
        }
        for (String line : providerLines) {
            System.out.println(line);
        }
        System.out.println();
        System.out.println("\t0) Back");
    }

    /**
     * Prints detailed information about a provider including its product catalogue.
     *
     * @param companyName company name
     * @param cif         tax identification number
     * @param contactName contact person name
     * @param phone       contact phone number
     * @param email       contact email address
     * @param productLines two-dimensional array of product entry lines
     */
    public void showProviderDetail(
        String companyName,
        String cif,
        String contactName,
        String phone,
        String email,
        String[][] productLines
    ) {
        System.out.println("Name: " + companyName + ",");
        System.out.println("CIF: " + cif + ",");
        System.out.println("Contact person: " + contactName + ",");
        System.out.println("Phone number: " + phone + ",");
        System.out.println("Contact email: " + email + ",");
        System.out.println("Provider products:");
        System.out.println();
        for (String[] lines : productLines) {
            for (String line : lines) {
                System.out.println(line);
            }
        }
        System.out.println("\t0) Back");
    }

    /**
     * Prints the current shopping cart contents and total price.
     *
     * @param cartLines  formatted cart item lines
     * @param totalPrice total price including VAT, discounts, and shipping
     */
    public void showCart(String[] cartLines, float totalPrice) {
        System.out.println("Shopping cart:");
        if (cartLines.length == 0) {
            System.out.println("Cart is empty.");
            return;
        }
        for (String line : cartLines) {
            System.out.println(line);
        }
        System.out.println();
        System.out.printf("%-50s %8.2f€%n", "TOTAL PRICE:", totalPrice);
    }

    /**
     * Prints the shopping cart action menu (Remove / Proceed to purchase / Back).
     */
    public void showCartMenu() {
        System.out.println();
        System.out.println("\t1) Remove product");
        System.out.println("\t2) Proceed to purchase");
        System.out.println("\t0) Back");
    }

    /**
     * Displays a prompt and reads a line of text from the user.
     *
     * @param prompt text to display before waiting for input
     * @return the trimmed input string
     */
    public String stringInput(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    /**
     * Displays a prompt and reads an integer from the user, re-prompting on invalid input.
     *
     * @param prompt text to display before waiting for input
     * @return the parsed integer value entered by the user
     */
    public int intInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid option. Please enter a number.");
            }
        }
    }

    /**
     * Prints a single informational message to the console.
     *
     * @param message the message to display
     */
    public void showMessage(String message) {
        System.out.println(message);
    }
}
