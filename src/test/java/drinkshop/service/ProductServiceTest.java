package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.Reteta;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductService - Black Box Tests for addProduct")
@Tag("bbt")
class ProductServiceTest {

    private static class InMemoryProductRepository implements Repository<Integer, Product> {
        private final List<Product> products = new ArrayList<>();

        @Override
        public Product findOne(Integer id) {
            return products.stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Product> findAll() {
            return products;
        }

        @Override
        public Product save(Product entity) {
            products.add(entity);
            return entity;
        }

        @Override
        public Product delete(Integer id) {
            Product p = findOne(id);
            if (p != null) {
                products.remove(p);
            }
            return p;
        }

        @Override
        public Product update(Product entity) {
            Product existing = findOne(entity.getId());
            if (existing != null) {
                products.remove(existing);
            }
            products.add(entity);
            return entity;
        }
    }

    private ProductService createServiceWithRepo(InMemoryProductRepository repo) {
        return new ProductService(repo);
    }

    private String textOfLength(int length) {
        return "F".repeat(length);
    }

    private Product productWithoutRecipe(String name, double price) {
        return new Product(
                1,
                name,
                price,
                CategorieBautura.JUICE,
                TipBautura.BASIC
        );
    }

    private Product productWithRecipe(String name, double price, int recipeId) {
        Reteta reteta = new Reteta(recipeId, new ArrayList<>());
        return new Product(
                1,
                name,
                price,
                CategorieBautura.JUICE,
                TipBautura.BASIC,
                reteta
        );
    }

    @Nested
    @DisplayName("ECP tests")
    class EcpTests {

        @Test
        @DisplayName("TC1_EC - valid product")
        void TC1_EC() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe("Fresh Orange", 12.5, 101);

            service.addProduct(product);

            assertEquals(1, repo.findAll().size());
            assertEquals("Fresh Orange", repo.findAll().get(0).getNume());
        }

        @Test
        @DisplayName("TC3_EC - empty name")
        void TC3_EC() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe("", 12.5, 101);

            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }

        @Test
        @DisplayName("TC4_EC - name longer than 255 chars")
        void TC4_EC() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe(textOfLength(300), 12.5, 101);

            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }

        @Test
        @DisplayName("TC5_EC - invalid price format abc")
        void TC5_EC() {
            assertThrows(NumberFormatException.class, () -> Double.parseDouble("abc"));
        }

        @Test
        @DisplayName("TC6_EC - price equal to zero")
        void TC6_EC() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe("Fresh Orange", 0.0, 101);

            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }

        @Test
        @DisplayName("TC10_EC - invalid recipe")
        void TC10_EC() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe("Fresh Orange", 12.5, 301);

            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }

        @Test
        @DisplayName("TC11_EC - already used recipe")
        void TC11_EC() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            // precondition: exista deja un produs cu aceasta reteta
            Product existingProduct = productWithRecipe("Produs existent", 10.0, 201);
            service.addProduct(existingProduct);

            Product newProduct = productWithRecipe("Fresh Orange", 12.5, 201);

            assertThrows(ValidationException.class, () -> service.addProduct(newProduct));
        }
    }

    @Nested
    @DisplayName("BVA tests")
    class BvaTests {

        @Test
        @DisplayName("TC1_BVA - name length 0")
        void TC1_BVA() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe("", 12.5, 101);

            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }

        @Test
        @DisplayName("TC3_BVA - name length 1")
        void TC3_BVA() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe(textOfLength(1), 12.5, 101);

            service.addProduct(product);

            assertEquals(1, repo.findAll().size());
        }

        @Test
        @DisplayName("TC4_BVA - name length 254")
        void TC4_BVA() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe(textOfLength(254), 12.5, 101);

            service.addProduct(product);

            assertEquals(1, repo.findAll().size());
        }

        @Test
        @DisplayName("TC5_BVA - name length 255")
        void TC5_BVA() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe(textOfLength(255), 12.5, 101);

            service.addProduct(product);

            assertEquals(1, repo.findAll().size());
        }

        @Test
        @DisplayName("TC6_BVA - name length 256")
        void TC6_BVA() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe(textOfLength(256), 12.5, 101);

            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }

        @Test
        @DisplayName("TC7_BVA - price below lower boundary")
        void TC7_BVA() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe("Fresh Orange", -0.01, 101);

            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }

        @Test
        @DisplayName("TC8_BVA - price at lower invalid boundary")
        void TC8_BVA() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe("Fresh Orange", 0.0, 101);

            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }

        @Test
        @DisplayName("TC9_BVA - price at lower valid boundary")
        void TC9_BVA() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe("Fresh Orange", 0.01, 101);

            service.addProduct(product);

            assertEquals(1, repo.findAll().size());
        }

        @Test
        @DisplayName("TC10_BVA - price Double.MAX_VALUE - 1")
        void TC10_BVA() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe("Fresh Orange", Double.MAX_VALUE - 1, 101);

            service.addProduct(product);

            assertEquals(1, repo.findAll().size());
        }

        @Test
        @DisplayName("TC11_BVA - price Double.MAX_VALUE")
        void TC11_BVA() {
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);

            Product product = productWithRecipe("Fresh Orange", Double.MAX_VALUE, 101);

            service.addProduct(product);

            assertEquals(1, repo.findAll().size());
        }
    }
}