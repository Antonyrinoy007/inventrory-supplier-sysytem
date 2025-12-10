import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;

public class ApiTester {

    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) {
        try {
            System.out.println("--- Starting API Tests ---");

            // 1. Create Supplier
            System.out.println("\n1. Creating Supplier...");
            String supplierJson = "{\"name\":\"TechParts Inc.\", \"contactPerson\":\"Alice Smith\", \"phone\":\"555-0100\", \"email\":\"alice@techparts.com\"}";
            sendPost("http://localhost:8082/api/suppliers", supplierJson);

            // 2. Create Product
            System.out.println("\n2. Creating Product (linked to Supplier 1)...");
            String productJson = "{\"name\":\"Laptop Screen\", \"description\":\"15 inch display\", \"price\":120.50, \"quantityInStock\":50, \"supplierId\":1}";
            sendPost("http://localhost:8081/api/products", productJson);

            // 3. Get Product Details (Inter-service communication)
            System.out.println("\n3. Fetching Product Details (checking Supplier info)...");
            sendGet("http://localhost:8081/api/products/1/supplier");

            // 4. Decrease Stock
            System.out.println("\n4. Decreasing Stock (Sale of 5 items)...");
            String stockJson = "{\"amount\":5}";
            sendPost("http://localhost:8081/api/products/1/decreaseStock", stockJson);

            // 5. Verify Stock Level after Sale
            System.out.println("\n5. Verifying Stock Level after Sale...");
            sendGet("http://localhost:8081/api/products/1");

            // 6. Increase Stock (Restock)
            System.out.println("\n6. Increasing Stock (Restock of 20 items)...");
            String restockJson = "{\"amount\":20}";
            sendPost("http://localhost:8081/api/products/1/increaseStock", restockJson);

            // 7. Verify Final Stock Level
            System.out.println("\n7. Verifying Final Stock Level after Restock...");
            sendGet("http://localhost:8081/api/products/1");

            System.out.println("\n--- Tests Completed ---");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendPost(String url, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }

    private static void sendGet(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + response.statusCode());
        System.out.println("Response: " + response.body());
    }
}
