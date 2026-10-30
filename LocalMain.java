import business.BusinessException;
import business.ClientManager;
import business.ProductManager;
import business.ProviderManager;
import business.SaleManager;
import presentation.Controller;
import presentation.UserInterface;

/** Portfolio convenience entry point: choose file persistence without the startup API probe. */
public class LocalMain {

    public static void main(String[] args) throws BusinessException {
        ProductManager products = new ProductManager(false);
        Controller controller = new Controller(
            new UserInterface(),
            new ClientManager(false),
            products,
            new ProviderManager(false),
            new SaleManager(false, products)
        );
        controller.run();
    }
}
