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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Integration: Service + Validator <-> Repo (file-backed)")
class ProductServiceLevel1RepoIntTest {

    private FileProductRepository productRepo;
    private FileRetetaRepository retetaRepo;
    private ProductValidator productValidator;
    private ProductService productService;
    private Product product;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        Path productsPath = tempDir.resolve("products.txt");
        Path retetePath = tempDir.resolve("retete.txt");
        if (!Files.exists(productsPath)) Files.createFile(productsPath);
        if (!Files.exists(retetePath)) Files.createFile(retetePath);

        productRepo = new FileProductRepository(productsPath.toAbsolutePath().toString());
        retetaRepo = new FileRetetaRepository(retetePath.toAbsolutePath().toString());
        productValidator = spy(new ProductValidator(productRepo, retetaRepo));
        productService = new ProductService(productRepo, productValidator);
        product = mock(Product.class);
    }

    @Test
    @DisplayName("addProduct - valid product is saved and validator called")
    void addProduct_valid_saved() {
        Reteta reteta = mock(Reteta.class);
        when(product.getNume()).thenReturn("Good");
        when(product.getPret()).thenReturn(2.5);
        when(product.getReteta()).thenReturn(reteta);
        when(product.getId()).thenReturn(10);
        when(reteta.getId()).thenReturn(101);

        // persist the recipe in the real reteta repo
        retetaRepo.save(reteta);

        assertDoesNotThrow(() -> productService.addProduct(product));

        // repo should now contain the product
        assertEquals(1, productRepo.findAll().size());
        assertSame(product, productRepo.findAll().get(0));

        // validator was invoked
        verify(productValidator, times(1)).validate(product);
    }

    @Test
    @DisplayName("addProduct - null product should throw ValidationException and not save")
    void addProduct_nullProduct() {
        // act & assert
        ValidationException ex = assertThrows(ValidationException.class, () -> productService.addProduct(null));
        assertTrue(ex.getMessage().toLowerCase().contains("null"));

        // validator should have been invoked with null and repo remains empty
        verify(productValidator, times(1)).validate(null);
        assertEquals(0, productRepo.findAll().size());
    }

    @Test
    @DisplayName("addProduct - null name should throw ValidationException and not save")
    void addProduct_nullName() {
        Reteta reteta = mock(Reteta.class);
        when(product.getNume()).thenReturn(null);
        when(product.getPret()).thenReturn(1.0);
        when(product.getReteta()).thenReturn(reteta);
        when(product.getId()).thenReturn(20);
        when(reteta.getId()).thenReturn(201);

        // even if recipe saved, validation should fail earlier
        retetaRepo.save(reteta);

        ValidationException ex = assertThrows(ValidationException.class, () -> productService.addProduct(product));
        assertTrue(ex.getMessage().toLowerCase().contains("numele"));

        verify(productValidator, times(1)).validate(product);
        assertEquals(0, productRepo.findAll().size());
    }

    @Test
    @DisplayName("addProduct - name too long should throw ValidationException and not save")
    void addProduct_nameTooLong() {
        Reteta reteta = mock(Reteta.class);
        String longName = "X".repeat(300);
        when(product.getNume()).thenReturn(longName);
        when(product.getPret()).thenReturn(1.0);
        when(product.getReteta()).thenReturn(reteta);
        when(product.getId()).thenReturn(21);
        when(reteta.getId()).thenReturn(202);

        retetaRepo.save(reteta);

        ValidationException ex = assertThrows(ValidationException.class, () -> productService.addProduct(product));
        assertTrue(ex.getMessage().toLowerCase().contains("255"));

        verify(productValidator, times(1)).validate(product);
        assertEquals(0, productRepo.findAll().size());
    }

    @Test
    @DisplayName("addProduct - zero price should throw ValidationException and not save")
    void addProduct_zeroPrice() {
        Reteta reteta = mock(Reteta.class);
        when(product.getNume()).thenReturn("Name");
        when(product.getPret()).thenReturn(0.0);
        when(product.getReteta()).thenReturn(reteta);
        when(product.getId()).thenReturn(22);
        when(reteta.getId()).thenReturn(301);

        retetaRepo.save(reteta);

        ValidationException ex = assertThrows(ValidationException.class, () -> productService.addProduct(product));
        assertTrue(ex.getMessage().toLowerCase().contains("pretul"));

        verify(productValidator, times(1)).validate(product);
        assertEquals(0, productRepo.findAll().size());
    }

     @Test
     @DisplayName("addProduct - recipe already used by another product -> ValidationException and no save")
     void addProduct_recipeAlreadyUsed() {
         Reteta reteta = mock(Reteta.class);
         Product existing = mock(Product.class);

         // existing product uses recipe id 555 and has id 1
         when(existing.getReteta()).thenReturn(reteta);
         when(existing.getId()).thenReturn(1);
         when(reteta.getId()).thenReturn(555);

         // save existing product in repo (real repo behavior)
         productRepo.save(existing);

         // new product uses same recipe but different id
         when(product.getNume()).thenReturn("Name");
         when(product.getPret()).thenReturn(1.2);
         when(product.getReteta()).thenReturn(reteta);
         when(product.getId()).thenReturn(2);

         // recipe must be present in reteta repo
         retetaRepo.save(reteta);

         ValidationException ex = assertThrows(ValidationException.class, () -> productService.addProduct(product));
         assertTrue(ex.getMessage().toLowerCase().contains("reteta"));

         // validator called but save must not have added the new product
         verify(productValidator, times(1)).validate(product);
         assertEquals(1, productRepo.findAll().size(), "Only the existing product should remain in repo");
     }
 }
