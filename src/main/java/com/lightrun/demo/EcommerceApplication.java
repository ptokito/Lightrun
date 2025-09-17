package com.lightrun.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@SpringBootApplication
public class EcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}

// Product Model
class Product {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer inventory;
    private String category;

    public Product(Long id, String name, BigDecimal price, Integer inventory, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.inventory = inventory;
        this.category = category;
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public Integer getInventory() { return inventory; }
    public void setInventory(Integer inventory) { this.inventory = inventory; }
    public String getCategory() { return category; }
}

// Order Models
class Order {
    private Long id;
    private String customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private String discountCode;

    public Order(Long id, String customerId) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }
}

class OrderItem {
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;

    public OrderItem(Long productId, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}

// Service Layer with INTENTIONAL BUGS
@Service
class OrderService {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private PricingService pricingService;
    
    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong orderIdGenerator = new AtomicLong(1);

    public Order createOrder(String customerId, List<OrderRequest> orderRequests) {
        Order order = new Order(orderIdGenerator.getAndIncrement(), customerId);
        
        for (OrderRequest request : orderRequests) {
            Product product = productService.getProduct(request.getProductId());
            if (product == null) {
                throw new RuntimeException("Product not found: " + request.getProductId());
            }
            
            // BUG 1: Race condition in inventory check
            if (product.getInventory() < request.getQuantity()) {
                throw new RuntimeException("Insufficient inventory for product: " + product.getName());
            }
            
            // BUG 2: Inventory update happens AFTER the check, creating race condition
            productService.updateInventory(product.getId(), product.getInventory() - request.getQuantity());
            
            OrderItem item = new OrderItem(product.getId(), request.getQuantity(), product.getPrice());
            order.getItems().add(item);
        }
        
        // BUG 3: Price calculation has subtle error with discounts
        BigDecimal total = pricingService.calculateTotal(order);
        order.setTotalAmount(total);
        
        orders.put(order.getId(), order);
        return order;
    }
    
    public Order getOrder(Long orderId) {
        return orders.get(orderId);
    }
    
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }
}

@Service
class ProductService {
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    
    public ProductService() {
        // Initialize with sample products
        products.put(1L, new Product(1L, "Laptop", new BigDecimal("999.99"), 10, "Electronics"));
        products.put(2L, new Product(2L, "Mouse", new BigDecimal("29.99"), 50, "Electronics"));
        products.put(3L, new Product(3L, "Keyboard", new BigDecimal("79.99"), 25, "Electronics"));
        products.put(4L, new Product(4L, "Monitor", new BigDecimal("299.99"), 15, "Electronics"));
    }
    
    public Product getProduct(Long id) {
        return products.get(id);
    }
    
    public void updateInventory(Long productId, Integer newInventory) {
        Product product = products.get(productId);
        if (product != null) {
            // BUG 4: No synchronization on inventory updates
            try {
                Thread.sleep(10); // Simulate database delay - makes race condition more likely
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            product.setInventory(newInventory);
        }
    }
    
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }
}

@Service
class PricingService {
    
    public BigDecimal calculateTotal(Order order) {
        BigDecimal subtotal = BigDecimal.ZERO;
        
        for (OrderItem item : order.getItems()) {
            BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);
        }
        
        // BUG 5: Discount calculation has precision error
        if (order.getDiscountCode() != null) {
            BigDecimal discount = applyDiscount(subtotal, order.getDiscountCode());
            subtotal = subtotal.subtract(discount);
        }
        
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal applyDiscount(BigDecimal amount, String discountCode) {
        // BUG 6: Float arithmetic causes precision errors
        if ("SAVE10".equals(discountCode)) {
            // Using float instead of BigDecimal for calculation
            float discountAmount = amount.floatValue() * 0.1f;
            return new BigDecimal(discountAmount);
        } else if ("SAVE20".equals(discountCode)) {
            float discountAmount = amount.floatValue() * 0.2f;
            return new BigDecimal(discountAmount);
        }
        return BigDecimal.ZERO;
    }
}

// DTOs
class OrderRequest {
    private Long productId;
    private Integer quantity;
    
    public OrderRequest() {}
    
    public OrderRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}

class CreateOrderRequest {
    private String customerId;
    private List<OrderRequest> items;
    private String discountCode;
    
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public List<OrderRequest> getItems() { return items; }
    public void setItems(List<OrderRequest> items) { this.items = items; }
    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }
}

// REST Controller with CORS enabled
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")  // <-- THIS IS WHERE THE CORS ANNOTATION GOES
class EcommerceController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ProductService productService;
    
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }
    
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(request.getCustomerId(), request.getItems());
            
            // Apply discount code if provided
            if (request.getDiscountCode() != null && !request.getDiscountCode().isEmpty()) {
                order.setDiscountCode(request.getDiscountCode());
                // BUG 7: Total is not recalculated after setting discount code
            }
            
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/orders/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        Order order = orderService.getOrder(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }
}
