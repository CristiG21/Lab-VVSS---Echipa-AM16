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

@DisplayName("ProductService - BVA Black Box Tests for addProduct")
@Tag("bbt")
@Tag("bva")
class ProductServiceBvaTest {

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
    @DisplayName("TC1_BVA - name length 0")
    void TC1_BVA() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe("", 12.5, 101);

        assertThrows(ValidationException.class, () -> service.addProduct(product));
    }

    @Test
    @DisplayName("TC3_BVA - name length 1")
    void TC3_BVA() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe(textOfLength(1), 12.5, 101);

        service.addProduct(product);

        assertEquals(1, productRepo.findAll().size());
    }

    @Test
    @DisplayName("TC4_BVA - name length 254")
    void TC4_BVA() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe(textOfLength(254), 12.5, 101);

        service.addProduct(product);

        assertEquals(1, productRepo.findAll().size());
    }

    @Test
    @DisplayName("TC5_BVA - name length 255")
    void TC5_BVA() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe(textOfLength(255), 12.5, 101);

        service.addProduct(product);

        assertEquals(1, productRepo.findAll().size());
    }

    @Test
    @DisplayName("TC6_BVA - name length 256")
    void TC6_BVA() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe(textOfLength(256), 12.5, 101);

        assertThrows(ValidationException.class, () -> service.addProduct(product));
    }

    @Test
    @DisplayName("TC7_BVA - price below lower boundary")
    void TC7_BVA() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe("Fresh Orange", -0.01, 101);

        assertThrows(ValidationException.class, () -> service.addProduct(product));
    }

    @Test
    @DisplayName("TC8_BVA - price at lower invalid boundary")
    void TC8_BVA() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe("Fresh Orange", 0.0, 101);

        assertThrows(ValidationException.class, () -> service.addProduct(product));
    }

    @Test
    @DisplayName("TC9_BVA - price at lower valid boundary")
    void TC9_BVA() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe("Fresh Orange", 0.01, 101);

        service.addProduct(product);

        assertEquals(1, productRepo.findAll().size());
    }

    @Test
    @DisplayName("TC10_BVA - price Double.MAX_VALUE - 1")
    void TC10_BVA() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe("Fresh Orange", Double.MAX_VALUE - 1, 101);

        service.addProduct(product);

        assertEquals(1, productRepo.findAll().size());
    }

    @Test
    @DisplayName("TC11_BVA - price Double.MAX_VALUE")
    void TC11_BVA() {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product product = productWithRecipe("Fresh Orange", Double.MAX_VALUE, 101);

        service.addProduct(product);

        assertEquals(1, productRepo.findAll().size());
    }
}