package drinkshop.service.it;

import drinkshop.domain.Product;
import drinkshop.domain.Reteta;
import drinkshop.repository.file.FileProductRepository;
import drinkshop.repository.file.FileRetetaRepository;
import drinkshop.service.ProductService;
import drinkshop.service.validator.ProductValidator;
import drinkshop.service.validator.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration tests: ProductService + Validator + File Repos + real entities")
class ProductServiceIntTest {

    @TempDir
    Path tempDir;

    private FileProductRepository productRepo;
    private FileRetetaRepository retetaRepo;
    private ProductService service;

    @BeforeEach
    void setUp() throws Exception {
        Path products = tempDir.resolve("products.txt");
        Path retete = tempDir.resolve("retete.txt");
        if (!Files.exists(products)) Files.createFile(products);
        if (!Files.exists(retete)) Files.createFile(retete);

        productRepo = new FileProductRepository(products.toAbsolutePath().toString());
        retetaRepo = new FileRetetaRepository(retete.toAbsolutePath().toString());
        ProductValidator validator = new ProductValidator(productRepo, retetaRepo);
        service = new ProductService(productRepo, validator);
    }

    @Test
    @DisplayName("addProduct - valid product is saved (real entity + real repos + validator)")
    void addProduct_valid_saved() throws Exception {
        Reteta reteta = new Reteta(101, new ArrayList<>());
        retetaRepo.save(reteta);

        Product p = new Product(1, "Orange Juice", 5.5, null, null);
        // attach recipe
        p.setReteta(reteta);

        assertDoesNotThrow(() -> service.addProduct(p));

        Product stored = productRepo.findOne(1);
        assertNotNull(stored);
        assertEquals("Orange Juice", stored.getNume());
        assertEquals(5.5, stored.getPret());
        assertNotNull(stored.getReteta());
        assertEquals(101, stored.getReteta().getId());
    }

    @Test
    @DisplayName("addProduct - null product throws ValidationException")
    void addProduct_nullProduct() throws Exception {
        assertThrows(ValidationException.class, () -> service.addProduct(null));
    }

    @Test
    @DisplayName("addProduct - product with null name throws ValidationException")
    void addProduct_nullName() throws Exception {
        Reteta reteta = new Reteta(201, new ArrayList<>());
        retetaRepo.save(reteta);

        Product p = new Product(2, null, 3.0, null, null);
        p.setReteta(reteta);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.addProduct(p));
        assertTrue(ex.getMessage().toLowerCase().contains("numele"));
    }

    @Test
    @DisplayName("addProduct - product with name too long throws ValidationException")
    void addProduct_nameTooLong() throws Exception {
        Reteta reteta = new Reteta(202, new ArrayList<>());
        retetaRepo.save(reteta);

        String longName = "X".repeat(300);
        Product p = new Product(3, longName, 4.0, null, null);
        p.setReteta(reteta);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.addProduct(p));
        assertTrue(ex.getMessage().toLowerCase().contains("255"));
    }

    @Test
    @DisplayName("addProduct - product with zero price throws ValidationException")
    void addProduct_zeroPrice() throws Exception {
        Reteta reteta = new Reteta(203, new ArrayList<>());
        retetaRepo.save(reteta);

        Product p = new Product(4, "Name", 0.0, null, null);
        p.setReteta(reteta);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.addProduct(p));
        assertTrue(ex.getMessage().toLowerCase().contains("pret"));
    }

    @Test
    @DisplayName("addProduct - recipe already used by another product throws ValidationException")
    void addProduct_recipeAlreadyUsed() throws Exception {
        Reteta reteta = new Reteta(301, new ArrayList<>());
        retetaRepo.save(reteta);

        Product existing = new Product(10, "Existing", 2.0, null, null);
        existing.setReteta(reteta);
        productRepo.save(existing);

        Product p = new Product(11, "NewProd", 3.0, null, null);
        p.setReteta(reteta);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.addProduct(p));
        assertTrue(ex.getMessage().toLowerCase().contains("reteta"));

        // existing should still be present and new not saved
        assertEquals(1, productRepo.findAll().size());
        assertNotNull(productRepo.findOne(10));
        assertNull(productRepo.findOne(11));
    }
}
