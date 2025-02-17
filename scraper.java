import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class Product {
    private String title;
    private double price;

    public Product(String title, double price) {
        this.title = title;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }
}

public class WebScraper {
    private static final String URL = "https://example.com/products"; // Replace with actual website URL
    private static final double PRICE_THRESHOLD = 50.0;

    // Database Credentials
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/your_database";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";

    public static void main(String[] args) {
        try {
            List<Product> scrapedData = scrapeData();
            List<Product> filteredData = filterData(scrapedData, PRICE_THRESHOLD);
            insertIntoDatabase(filteredData);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Scrapes product data from a website.
     */
    public static List<Product> scrapeData() throws Exception {
        List<Product> products = new ArrayList<>();
        
        // Fetch HTML content
        Document doc = Jsoup.connect(URL).get();
        
        // Select elements (modify based on the website structure)
        Elements items = doc.select(".product");

        for (Element item : items) {
            String title = item.select(".title").text().trim();
            String priceText = item.select(".price").text().replace("$", "").trim();
            double price = Double.parseDouble(priceText);

            products.add(new Product(title, price));
        }

        System.out.println("Scraped " + products.size() + " products.");
        return products;
    }

    /**
     * Filters products below a price threshold.
     */
    public static List<Product> filterData(List<Product> products, double threshold) {
        List<Product> filtered = new ArrayList<>();
        
        for (Product p : products) {
            if (p.getPrice() <= threshold) {
                filtered.add(p);
            }
        }

        System.out.println("Filtered " + filtered.size() + " products.");
        return filtered;
    }

    /**
     * Inserts data into the PostgreSQL database.
     */
    public static void insertIntoDatabase(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("No data to insert.");
            return;
        }

        String query = "INSERT INTO products (title, price) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (Product product : products) {
                stmt.setString(1, product.getTitle());
                stmt.setDouble(2, product.getPrice());
                stmt.addBatch();
            }

            stmt.executeBatch();
            System.out.println("Inserted " + products.size() + " products into the database.");

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
