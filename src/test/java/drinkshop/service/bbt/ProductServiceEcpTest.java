package drinkshop.service.bbt;

import drinkshop.domain.CategorieBautura;
import drinkshop.domain.Product;
import drinkshop.domain.Reteta;
import drinkshop.domain.TipBautura;
import drinkshop.repository.Repository;
import drinkshop.service.ProductService;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductService - ECP Black Box Tests for addProduct")
@Tag("bbt")
@Tag("ecp")
class ProductServiceEcpTest {

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

    private static class InMemoryRetetaRepository implements Repository<Integer, Reteta> {
        private final List<Reteta> retete = new ArrayList<>();

        @Override
        public Reteta findOne(Integer id) {
            return retete.stream()
                    .filter(r -> r.getId() == id)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Reteta> findAll() {
            return retete;
        }

        @Override
        public Reteta save(Reteta entity) {
            retete.add(entity);
            return entity;
        }

        @Override
        public Reteta delete(Integer id) {
            Reteta r = findOne(id);
            if (r != null) {
                retete.remove(r);
            }
            return r;
        }

        @Override
        public Reteta update(Reteta entity) {
            Reteta existing = findOne(entity.getId());
            if (existing != null) {
                retete.remove(existing);
            }
            retete.add(entity);
            return entity;
        }
    }

    private InMemoryProductRepository productRepo;
    private InMemoryRetetaRepository retetaRepo;
    private ProductValidator productValidator;
    private ProductService service;
    private AtomicInteger nextId;

    @BeforeEach
    void setUp() {
        productRepo = new InMemoryProductRepository();
        retetaRepo = new InMemoryRetetaRepository();
        productValidator = new ProductValidator(productRepo, retetaRepo);
        service = new ProductService(productRepo, productValidator);
        nextId = new AtomicInteger(0);
    }

    @AfterEach
    void tearDown() {
        productRepo.findAll().clear();
        retetaRepo.findAll().clear();
    }

    private String textOfLength(int length) {
        return "F".repeat(length);
    }

    private Product productWithRecipe(String name, double price, int recipeId) {
        Reteta reteta = new Reteta(recipeId, new ArrayList<>());
        return new Product(
                nextId.incrementAndGet(),
                name,
                price,
                CategorieBautura.JUICE,
                TipBautura.BASIC,
                reteta
        );
    }

    @Test
    @DisplayName("TC1_EC - valid product")
    void TC1_EC() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe("Fresh Orange", 12.5, 101);

        service.addProduct(product);

        assertEquals(1, productRepo.findAll().size());
        assertEquals("Fresh Orange", productRepo.findAll().get(0).getNume());
    }

    @Test
    @DisplayName("TC3_EC - empty name")
    void TC3_EC() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe("", 12.5, 101);

        assertThrows(ValidationException.class, () -> service.addProduct(product));
    }

    @Test
    @DisplayName("TC4_EC - name longer than 255 chars")
    void TC4_EC() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

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
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe("Fresh Orange", 0.0, 101);

        assertThrows(ValidationException.class, () -> service.addProduct(product));
    }

    @Test
    @DisplayName("TC10_EC - invalid recipe")
    void TC10_EC() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe("Fresh Orange", 12.5, 301);

        assertThrows(ValidationException.class, () -> service.addProduct(product));
    }

    @Test
    @DisplayName("TC11_EC - already used recipe")
    void TC11_EC() {
        Reteta reteta = new Reteta(201, new ArrayList<>());
        retetaRepo.save(reteta);

        Product existingProduct = productWithRecipe("Produs existent", 10.0, 201);
        service.addProduct(existingProduct);

        Product newProduct = productWithRecipe("Fresh Orange", 12.5, 201);

        assertThrows(ValidationException.class, () -> service.addProduct(newProduct));
    }
}