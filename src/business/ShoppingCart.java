package business;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory shopping cart that holds {@link ProductForSale} items
 * for the current session until checkout or logout.
 */
public class ShoppingCart {

    private final List<ProductForSale> items;

    /**
     * Constructs an empty shopping cart.
     */
    public ShoppingCart() {
        this.items = new ArrayList<>();
    }

    /**
     * Adds a product offer to the cart.
     *
     * @param item the {@link ProductForSale} offer to add
     */
    public void addItem(ProductForSale item) {
        items.add(item);
    }

    /**
     * Removes all cart items with the given product identifier.
     *
     * @param productId the product ID to remove
     */
    public void remove(String productId) {
        items.removeIf(item -> item.getProductId().equals(productId));
    }

    /**
     * Returns all items currently in the cart as an array.
     *
     * @return array of {@link ProductForSale} items
     */
    public ProductForSale[] getItems() {
        return items.toArray(new ProductForSale[0]);
    }

    /**
     * Indicates whether the cart is empty.
     *
     * @return {@code true} if there are no items in the cart
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Removes all items from the cart.
     */
    public void clear() {
        items.clear();
    }
}
