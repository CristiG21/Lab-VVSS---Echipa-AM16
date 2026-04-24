package drinkshop.service.wbt;

import drinkshop.domain.*;
import drinkshop.repository.Repository;
import drinkshop.service.OrderService;
import drinkshop.service.StocService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderService - White Box Tests for addOrder")
@Tag("wbt")
class OrderServiceTest {

    // -------------------- In-memory repos --------------------

    private static class InMemoryOrderRepository implements Repository<Integer, Order> {
        private final List<Order> orders = new ArrayList<>();

        @Override
        public Order findOne(Integer id) {
            return orders.stream()
                    .filter(o -> o.getId() == id)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Order> findAll() {
            return orders;
        }

        @Override
        public Order save(Order entity) {
            orders.add(entity);
            return entity;
        }

        @Override
        public Order delete(Integer id) {
            Order found = findOne(id);
            if (found != null) {
                orders.remove(found);
            }
            return found;
        }

        @Override
        public Order update(Order entity) {
            Order existing = findOne(entity.getId());
            if (existing != null) {
                orders.remove(existing);
            }
            orders.add(entity);
            return entity;
        }
    }

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
            Product found = findOne(id);
            if (found != null) {
                products.remove(found);
            }
            return found;
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
            Reteta found = findOne(id);
            if (found != null) {
                retete.remove(found);
            }
            return found;
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

    private static class InMemoryStocRepository implements Repository<Integer, Stoc> {
        private final List<Stoc> stocuri = new ArrayList<>();
        int updateCalls = 0;

        @Override
        public Stoc findOne(Integer id) {
            return stocuri.stream()
                    .filter(s -> s.getId() == id)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public List<Stoc> findAll() {
            return stocuri;
        }

        @Override
        public Stoc save(Stoc entity) {
            stocuri.add(entity);
            return entity;
        }

        @Override
        public Stoc delete(Integer id) {
            Stoc found = findOne(id);
            if (found != null) {
                stocuri.remove(found);
            }
            return found;
        }

        @Override
        public Stoc update(Stoc entity) {
            updateCalls++;
            Stoc existing = findOne(entity.getId());
            if (existing != null && existing != entity) {
                stocuri.remove(existing);
                stocuri.add(entity);
            }
            return entity;
        }
    }

    // -------------------- Helpers --------------------

    private OrderService createService(InMemoryOrderRepository orderRepo,
                                       InMemoryProductRepository productRepo,
                                       InMemoryRetetaRepository retetaRepo) {
        return new OrderService(orderRepo, productRepo, retetaRepo);
    }

    private StocService createStocService(InMemoryStocRepository stocRepo) {
        return new StocService(stocRepo);
    }

    private Product product(int id, String name, double price) {
        return new Product(id, name, price, CategorieBautura.JUICE, TipBautura.BASIC);
    }

    private IngredientReteta ingredient(String name, double qty) {
        return new IngredientReteta(name, qty);
    }

    private Reteta reteta(int productId, IngredientReteta... ingrediente) {
        List<IngredientReteta> list = new ArrayList<>();
        for (IngredientReteta i : ingrediente) {
            list.add(i);
        }
        return new Reteta(productId, list);
    }

    private OrderItem item(Product p, int quantity) {
        return new OrderItem(p, quantity);
    }

    private Order order(int id, double totalPrice, OrderItem... items) {
        Order o = new Order(id);
        List<OrderItem> list = new ArrayList<>();
        for (OrderItem i : items) {
            list.add(i);
        }
        o.setItems(list);
        o.setTotalPrice(totalPrice);
        return o;
    }

    // -------------------- Tests --------------------

    @Test
    @DisplayName("F02_TC01 - order is null")
    void F02_TC01() {
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryProductRepository productRepo = new InMemoryProductRepository();
        InMemoryRetetaRepository retetaRepo = new InMemoryRetetaRepository();
        InMemoryStocRepository stocRepo = new InMemoryStocRepository();

        OrderService service = createService(orderRepo, productRepo, retetaRepo);
        StocService stocService = createStocService(stocRepo);

        Exception ex = assertThrows(Exception.class, () -> service.addOrder(null, stocService));
        assertEquals("Comanda nu poate fi null.", ex.getMessage());
        assertEquals(0, orderRepo.findAll().size());
    }

    @Test
    @DisplayName("F02_TC02 - order items are null")
    void F02_TC02() {
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryProductRepository productRepo = new InMemoryProductRepository();
        InMemoryRetetaRepository retetaRepo = new InMemoryRetetaRepository();
        InMemoryStocRepository stocRepo = new InMemoryStocRepository();

        OrderService service = createService(orderRepo, productRepo, retetaRepo);
        StocService stocService = createStocService(stocRepo);

        Order o = new Order(1);
        o.setItems(null);
        o.setTotalPrice(50);

        Exception ex = assertThrows(Exception.class, () -> service.addOrder(o, stocService));
        assertEquals("Comanda trebuie sa contina cel putin un produs.", ex.getMessage());
        assertEquals(0, orderRepo.findAll().size());
    }

    @Test
    @DisplayName("F02_TC03 - order items are empty")
    void F02_TC03() {
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryProductRepository productRepo = new InMemoryProductRepository();
        InMemoryRetetaRepository retetaRepo = new InMemoryRetetaRepository();
        InMemoryStocRepository stocRepo = new InMemoryStocRepository();

        OrderService service = createService(orderRepo, productRepo, retetaRepo);
        StocService stocService = createStocService(stocRepo);

        Order o = new Order(2);
        o.setItems(new ArrayList<>());
        o.setTotalPrice(0);

        Exception ex = assertThrows(Exception.class, () -> service.addOrder(o, stocService));
        assertEquals("Comanda trebuie sa contina cel putin un produs.", ex.getMessage());
        assertEquals(0, orderRepo.findAll().size());
    }

    @Test
    @DisplayName("F02_TC04 - quantity <= 0")
    void F02_TC04() {
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryProductRepository productRepo = new InMemoryProductRepository();
        InMemoryRetetaRepository retetaRepo = new InMemoryRetetaRepository();
        InMemoryStocRepository stocRepo = new InMemoryStocRepository();

        OrderService service = createService(orderRepo, productRepo, retetaRepo);
        StocService stocService = createStocService(stocRepo);

        Product p1 = product(1, "Pizza", 30);
        Order o = order(3, 30, item(p1, 0));

        Exception ex = assertThrows(Exception.class, () -> service.addOrder(o, stocService));
        assertEquals("Cantitatea trebuie sa fie mai mare decat 0.", ex.getMessage());
        assertEquals(0, orderRepo.findAll().size());
        assertEquals(0, stocRepo.updateCalls);
    }

    @Test
    @DisplayName("F02_TC05 - recipe does not exist")
    void F02_TC05() {
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryProductRepository productRepo = new InMemoryProductRepository();
        InMemoryRetetaRepository retetaRepo = new InMemoryRetetaRepository();
        InMemoryStocRepository stocRepo = new InMemoryStocRepository();

        OrderService service = createService(orderRepo, productRepo, retetaRepo);
        StocService stocService = createStocService(stocRepo);

        Product p3 = product(99, "ProdusFaraReteta", 20);
        Order o = order(4, 40, item(p3, 2));

        Exception ex = assertThrows(Exception.class, () -> service.addOrder(o, stocService));
        assertEquals("Nu exista reteta pentru produsul cu id 99", ex.getMessage());
        assertEquals(0, orderRepo.findAll().size());
        assertEquals(0, stocRepo.updateCalls);
    }

    @Test
    @DisplayName("F02_TC06 - valid order with one product")
    void F02_TC06() throws Exception {
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryProductRepository productRepo = new InMemoryProductRepository();
        InMemoryRetetaRepository retetaRepo = new InMemoryRetetaRepository();
        InMemoryStocRepository stocRepo = new InMemoryStocRepository();

        OrderService service = createService(orderRepo, productRepo, retetaRepo);
        StocService stocService = createStocService(stocRepo);

        Product p1 = product(1, "Pizza", 30);
        retetaRepo.save(reteta(1, ingredient("Faina", 100), ingredient("Branza", 50)));

        Stoc s1 = new Stoc(1, "Faina", 500, 10);
        Stoc s2 = new Stoc(2, "Branza", 500, 10);
        stocRepo.save(s1);
        stocRepo.save(s2);

        Order o = order(5, 60, item(p1, 2));

        assertDoesNotThrow(() -> service.addOrder(o, stocService));

        assertEquals(1, orderRepo.findAll().size());
        assertEquals(300.0, s1.getCantitate()); // 500 - (100 * 2)
        assertEquals(400.0, s2.getCantitate()); // 500 - (50 * 2)
        assertEquals(2, stocRepo.updateCalls);
    }

    @Test
    @DisplayName("F02_TC07 - valid order with two products")
    void F02_TC07() throws Exception {
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryProductRepository productRepo = new InMemoryProductRepository();
        InMemoryRetetaRepository retetaRepo = new InMemoryRetetaRepository();
        InMemoryStocRepository stocRepo = new InMemoryStocRepository();

        OrderService service = createService(orderRepo, productRepo, retetaRepo);
        StocService stocService = createStocService(stocRepo);

        Product p1 = product(1, "Pizza", 30);
        Product p2 = product(2, "Burger", 30);

        retetaRepo.save(reteta(1, ingredient("Faina", 100), ingredient("Branza", 50)));
        retetaRepo.save(reteta(2, ingredient("Carne", 80), ingredient("Branza", 20)));

        Stoc faina = new Stoc(1, "Faina", 500, 10);
        Stoc branza = new Stoc(2, "Branza", 500, 10);
        Stoc carne = new Stoc(3, "Carne", 500, 10);

        stocRepo.save(faina);
        stocRepo.save(branza);
        stocRepo.save(carne);

        Order o = order(6, 90, item(p1, 1), item(p2, 2));

        assertDoesNotThrow(() -> service.addOrder(o, stocService));

        assertEquals(1, orderRepo.findAll().size());
        assertEquals(400.0, faina.getCantitate());   // 500 - 100
        assertEquals(410.0, branza.getCantitate());  // 500 - (50 + 40)
        assertEquals(340.0, carne.getCantitate());   // 500 - 160
        assertEquals(3, stocRepo.updateCalls);
    }

    @Test
    @DisplayName("F02_TC08 - stop at first invalid item")
    void F02_TC08() {
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryProductRepository productRepo = new InMemoryProductRepository();
        InMemoryRetetaRepository retetaRepo = new InMemoryRetetaRepository();
        InMemoryStocRepository stocRepo = new InMemoryStocRepository();

        OrderService service = createService(orderRepo, productRepo, retetaRepo);
        StocService stocService = createStocService(stocRepo);

        Product p1 = product(1, "Pizza", 30);
        Product p2 = product(2, "Burger", 45);

        retetaRepo.save(reteta(1, ingredient("Faina", 100)));
        retetaRepo.save(reteta(2, ingredient("Carne", 80)));

        Stoc faina = new Stoc(1, "Faina", 500, 10);
        Stoc carne = new Stoc(2, "Carne", 500, 10);
        stocRepo.save(faina);
        stocRepo.save(carne);

        Order o = order(7, 120, item(p1, 0), item(p2, 2));

        Exception ex = assertThrows(Exception.class, () -> service.addOrder(o, stocService));
        assertEquals("Cantitatea trebuie sa fie mai mare decat 0.", ex.getMessage());

        // comanda nu se salveaza
        assertEquals(0, orderRepo.findAll().size());

        // stocul ramane neschimbat
        assertEquals(500.0, faina.getCantitate());
        assertEquals(500.0, carne.getCantitate());

        // nu se face niciun update de stoc
        assertEquals(0, stocRepo.updateCalls);
    }
}