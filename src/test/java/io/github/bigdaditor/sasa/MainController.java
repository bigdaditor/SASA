package io.github.bigdaditor.sasa;

import io.github.bigdaditor.sasa.dto.OrderDTO;
import io.github.bigdaditor.sasa.dto.ProductDTO;
import io.github.bigdaditor.sasa.dto.UserDTO;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class MainController {

    // ========== GET Endpoints (5) ==========

    @GetMapping("/string")
    public String getString() {
        return "Hello SASA";
    }

    @GetMapping("/users")
    public List<UserDTO> getUsers() {
        return Arrays.asList(
                new UserDTO(1L, "John Doe", "john@example.com", 30, LocalDate.of(1993, 5, 15)),
                new UserDTO(2L, "Jane Smith", "jane@example.com", 25, LocalDate.of(1998, 8, 20))
        );
    }

    @GetMapping("/user/{id}")
    public UserDTO getUser(@PathVariable Long id) {
        return new UserDTO(id, "John Doe", "john@example.com", 30, LocalDate.of(1993, 5, 15));
    }

    @GetMapping("/count")
    public Integer getCount() {
        return 42;
    }

    @GetMapping("/void")
    public void getVoid() {
        // Do nothing
    }

    // ========== POST Endpoints (5) ==========

    @PostMapping("/echo")
    public String postEcho(@RequestBody String message) {
        return "Echo: " + message;
    }

    @PostMapping("/users")
    public List<UserDTO> createUsers(@RequestBody List<UserDTO> users) {
        return users;
    }

    @PostMapping("/user")
    public UserDTO createUser(@RequestBody UserDTO user) {
        return user;
    }

    @PostMapping("/flag")
    public Boolean postFlag() {
        return true;
    }

    @PostMapping("/process")
    public void postProcess(@RequestBody Map<String, Object> data) {
        // Process data
    }

    // ========== PUT Endpoints (5) ==========

    @PutMapping("/update/{id}")
    public UserDTO updateUser(@PathVariable Long id, @RequestBody UserDTO user) {
        user.setId(id);
        return user;
    }

    @PutMapping("/items")
    public List<String> updateItems(@RequestBody List<String> items) {
        return items;
    }

    @PutMapping("/status")
    public String updateStatus(@RequestParam String status) {
        return "Status updated to: " + status;
    }

    @PutMapping("/product/{id}")
    public ProductDTO updateProduct(@PathVariable Long id, @RequestBody ProductDTO product) {
        product.setId(id);
        return product;
    }

    @PutMapping("/batch")
    public void batchUpdate(@RequestBody List<Map<String, Object>> batch) {
        // Batch update
    }

    // ========== DELETE Endpoints (5) ==========

    @DeleteMapping("/user/{id}")
    public Boolean deleteUser(@PathVariable Long id) {
        return true;
    }

    @DeleteMapping("/users")
    public List<Long> deleteUsers(@RequestParam List<Long> ids) {
        return ids;
    }

    @DeleteMapping("/order/{id}")
    public OrderDTO deleteOrder(@PathVariable Long id) {
        return new OrderDTO(
                id,
                1L,
                Arrays.asList(
                        new ProductDTO(1L, "Laptop", new BigDecimal("999.99"), "Electronics", true, Arrays.asList("tech", "computer"))
                ),
                new BigDecimal("999.99"),
                "CANCELLED",
                LocalDateTime.now()
        );
    }

    @DeleteMapping("/item/{id}")
    public String deleteItem(@PathVariable Long id) {
        return "Item " + id + " deleted";
    }

    @DeleteMapping("/clear")
    public void clearAll() {
        // Clear all data
    }

    // ========== PATCH Endpoints (5) ==========

    @PatchMapping("/user/{id}")
    public UserDTO patchUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        UserDTO user = new UserDTO(id, "John Doe", "john@example.com", 30, LocalDate.of(1993, 5, 15));
        if (updates.containsKey("name")) {
            user.setName((String) updates.get("name"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        return user;
    }

    @PatchMapping("/tags")
    public List<String> patchTags(@RequestBody List<String> tags) {
        return tags;
    }

    @PatchMapping("/status/{id}")
    public String patchStatus(@PathVariable Long id, @RequestParam String status) {
        return "Status " + id + " updated to: " + status;
    }

    @PatchMapping("/product/{id}")
    public ProductDTO patchProduct(@PathVariable Long id, @RequestBody ProductDTO product) {
        product.setId(id);
        return product;
    }

    @PatchMapping("/reset")
    public void resetData() {
        // Reset all data
    }
}
