package persistence.impl;

import business.Sale;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import persistence.dao.SaleDAO;
import persistence.exception.PersistenceException;
import persistence.exception.PersistenceFileNotFoundException;
import persistence.exception.PersistenceWriteException;

/**
 * CSV-based implementation of {@link persistence.dao.SaleDAO}.
 * Reads and appends sale records from/to {@code sales.csv}.
 */
public class SaleCSVDAO implements SaleDAO {

    private static final String FILE_PATH = "sales.csv";

    private static final String CSV_HEADER = "client_id,product_id,price_paid,purchase_date";

    /**
     * Reads all sale records from the CSV file.
     * Skips the header row and blank lines; handles both timestamp formats (seconds/milliseconds).
     *
     * @return list of parsed {@link business.Sale} objects (empty list if the file does not exist)
     * @throws PersistenceFileNotFoundException if the file is malformed or cannot be accessed
     */
    private List<Sale> retrieveSaleFromCSV() throws PersistenceFileNotFoundException {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        List<Sale> sales = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (line.startsWith("client_id")) {
                        continue;
                    }
                }
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length != 4) {
                    throw new PersistenceFileNotFoundException(
                        "Error: sales.csv has a malformed line: " + line
                    );
                }
                try {
                    int clientId = Integer.parseInt(parts[0].trim());
                    String productId = parts[1].trim();
                    float pricePaid = Float.parseFloat(parts[2].trim());
                    if (pricePaid > 1000f) {
                        pricePaid = pricePaid / 100f;
                    }
                    long timestampSecs = Long.parseLong(parts[3].trim());
                    sales.add(
                        new Sale(clientId, productId, pricePaid, new Date(timestampSecs * 1000L))
                    );
                } catch (NumberFormatException e) {
                    throw new PersistenceFileNotFoundException(
                        "Error: sales.csv contains a line with invalid numeric values: " + line,
                        e
                    );
                }
            }
        } catch (IOException e) {
            throw new PersistenceFileNotFoundException(
                "Error: The sales.csv file can't be accessed.",
                e
            );
        }
        return sales;
    }

    /**
     * Appends a single sale record to the CSV file, writing the header row if the file
     * does not yet exist.
     *
     * @param sale the sale to persist
     * @throws PersistenceWriteException if the file cannot be written
     */
    private void persistSaleToCSV(Sale sale) throws PersistenceWriteException {
        File file = new File(FILE_PATH);
        boolean needsHeader = !file.exists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (needsHeader) {
                writer.write(CSV_HEADER);
                writer.newLine();
            }
            long timestampSecs = sale.getPurchaseDate().getTime() / 1000L;
            writer.write(
                sale.getClientId() +
                    "," +
                    sale.getProductId() +
                    "," +
                    sale.getPricePaid() +
                    "," +
                    timestampSecs
            );
            writer.newLine();
        } catch (IOException e) {
            throw new PersistenceWriteException("Error: Could not write to sales.csv.", e);
        }
    }

    @Override
    /**
     * Persists a new sale by appending it to the CSV file.
     *
     * @param sale the sale to save
     * @throws PersistenceException if writing fails
     */
    public void save(Sale sale) throws PersistenceException {
        persistSaleToCSV(sale);
    }

    @Override
    /**
     * Returns all sales for the given client from the CSV file, sorted by date descending.
     *
     * @param clientId the client ID to filter by
     * @return array of matching {@link business.Sale} records
     * @throws PersistenceException if the file cannot be read
     */
    public Sale[] findByClientId(int clientId) throws PersistenceException {
        List<Sale> result = new ArrayList<>();
        for (Sale s : retrieveSaleFromCSV()) {
            if (s.getClientId() == clientId) {
                result.add(s);
            }
        }
        result.sort(Comparator.comparing(Sale::getPurchaseDate).reversed());
        return result.toArray(new Sale[0]);
    }
}
