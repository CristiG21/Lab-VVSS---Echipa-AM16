package drinkshop.service;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

    private Product validProduct(String name, double price) {
        return new Product(
                1,
                name,
                price,
                CategorieBautura.JUICE,
                TipBautura.BASIC
        );
    }

    static Stream<String> invalidNames() {
        return Stream.of(
                "",
                "a".repeat(256)
        );
    }

    static Stream<String> validBoundaryNames() {
        return Stream.of(
                "F",
                "a".repeat(254),
                "a".repeat(255)
        );
    }

    static Stream<Double> invalidPrices() {
        return Stream.of(
                -0.01,
                0.0
        );
    }

    static Stream<Double> validBoundaryPrices() {
        return Stream.of(
                0.01,
                12.5,
                999.99
        );
    }

    @Nested
    @DisplayName("ECP tests")
    class EcpTests {

        @Test
        @DisplayName("ECP valid - should save product when name and price are valid")
        void addProductValid_ECP() {
            // Arrange
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);
            Product product = validProduct("Fresh Orange", 12.5);

            // Act
            service.addProduct(product);

            // Assert
            assertEquals(1, repo.findAll().size());
            assertEquals("Fresh Orange", repo.findAll().get(0).getNume());
        }

        @ParameterizedTest
        @MethodSource("drinkshop.service.ProductServiceTest#invalidNames")
        @DisplayName("ECP invalid - should reject invalid names")
        void addProductInvalidName_ECP(String invalidName) {
            // Arrange
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);
            Product product = validProduct(invalidName, 12.5);

            // Act + Assert
            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }

        @ParameterizedTest
        @MethodSource("drinkshop.service.ProductServiceTest#invalidPrices")
        @DisplayName("ECP invalid - should reject non-positive price")
        void addProductInvalidPrice_ECP(Double invalidPrice) {
            // Arrange
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);
            Product product = validProduct("Fresh Orange", invalidPrice);

            // Act + Assert
            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }
    }

    @Nested
    @DisplayName("BVA tests")
    class BvaTests {

        @ParameterizedTest
        @MethodSource("drinkshop.service.ProductServiceTest#validBoundaryNames")
        @DisplayName("BVA valid - should accept names on valid boundaries")
        void addProductValidNameBoundaries_BVA(String validName) {
            // Arrange
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);
            Product product = validProduct(validName, 12.5);

            // Act
            service.addProduct(product);

            // Assert
            assertEquals(1, repo.findAll().size());
        }

        @Test
        @DisplayName("BVA invalid - should reject empty name")
        void addProductNameBelowMin_BVA() {
            // Arrange
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);
            Product product = validProduct("", 12.5);

            // Act + Assert
            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }

        @Test
        @DisplayName("BVA invalid - should reject name over max length")
        void addProductNameAboveMax_BVA() {
            // Arrange
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);
            Product product = validProduct("a".repeat(256), 12.5);

            // Act + Assert
            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }

        @ParameterizedTest
        @MethodSource("drinkshop.service.ProductServiceTest#validBoundaryPrices")
        @DisplayName("BVA valid - should accept positive prices")
        void addProductValidPriceBoundaries_BVA(Double validPrice) {
            // Arrange
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);
            Product product = validProduct("Fresh Orange", validPrice);

            // Act
            service.addProduct(product);

            // Assert
            assertEquals(1, repo.findAll().size());
        }

        @Test
        @DisplayName("BVA invalid - should reject price below zero")
        void addProductPriceBelowBoundary_BVA() {
            // Arrange
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);
            Product product = validProduct("Fresh Orange", -0.01);

            // Act + Assert
            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }

        @Test
        @DisplayName("BVA invalid - should reject price equal to zero")
        void addProductPriceAtBoundaryZero_BVA() {
            // Arrange
            InMemoryProductRepository repo = new InMemoryProductRepository();
            ProductService service = createServiceWithRepo(repo);
            Product product = validProduct("Fresh Orange", 0.0);

            // Act + Assert
            assertThrows(ValidationException.class, () -> service.addProduct(product));
        }
    }
}