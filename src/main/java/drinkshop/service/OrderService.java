package drinkshop.service;

import drinkshop.domain.*;
import drinkshop.repository.Repository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService {

    private final Repository<Integer, Order> orderRepo;
    private final Repository<Integer, Product> productRepo;
    private final Repository<Integer, Reteta> retetaRepo;

    public OrderService(Repository<Integer, Order> orderRepo,
                        Repository<Integer, Product> productRepo,
                        Repository<Integer, Reteta> retetaRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.retetaRepo = retetaRepo;
    }

    public void addOrder(Order o, StocService stocService) throws Exception {
        if (o == null) {
            throw new Exception("Comanda nu poate fi null.");
        }

        if (o.getItems() == null || o.getItems().isEmpty()) {
            throw new Exception("Comanda trebuie sa contina cel putin un produs.");
        }

        Map<String, Double> requiredIngredients = new HashMap<>();

        for (OrderItem item : o.getItems()) {
            if (item.getQuantity() <= 0) {
                throw new Exception("Cantitatea trebuie sa fie mai mare decat 0.");
            }

            Reteta r = findRecipeByProductId(item.getProduct().getId());

            if (r == null) {
                throw new Exception("Nu exista reteta pentru produsul cu id " + item.getProduct().getId());
            }

            addRequiredIngredients(requiredIngredients, r, item.getQuantity());
        }

        validateStock(requiredIngredients, stocService.getAll());
        updateStock(requiredIngredients, stocService.getAll(), stocService);

        orderRepo.save(o);
    }

    private Reteta findRecipeByProductId(int productId) {
        List<Reteta> retete = retetaRepo.findAll();

        for (Reteta reteta : retete) {
            if (reteta.getId() == productId) {
                return reteta;
            }
        }

        return null;
    }

    private void addRequiredIngredients(Map<String, Double> requiredIngredients, Reteta r, int quantity) {
        for (IngredientReteta ir : r.getIngrediente()) {
            String name = ir.getDenumire();
            double amount = ir.getCantitate() * quantity;

            if (requiredIngredients.containsKey(name)) {
                requiredIngredients.put(name, requiredIngredients.get(name) + amount);
            } else {
                requiredIngredients.put(name, amount);
            }
        }
    }

    private void validateStock(Map<String, Double> requiredIngredients, List<Stoc> allStocks) throws Exception {
        for (Map.Entry<String, Double> entry : requiredIngredients.entrySet()) {
            String numeIng = entry.getKey();
            double necesarTotal = entry.getValue();
            double disponibil = getAvailableQuantity(allStocks, numeIng);

            if (disponibil < necesarTotal) {
                throw new Exception("Stoc insuficient pentru: " + numeIng);
            }
        }
    }

    private double getAvailableQuantity(List<Stoc> allStocks, String ingredient) {
        double total = 0;

        for (Stoc s : allStocks) {
            if (s.getIngredient().equalsIgnoreCase(ingredient)) {
                total += s.getCantitate();
            }
        }

        return total;
    }

    private void updateStock(Map<String, Double> requiredIngredients, List<Stoc> allStocks, StocService stocService) throws Exception {
        for (Map.Entry<String, Double> entry : requiredIngredients.entrySet()) {
            String numeIng = entry.getKey();
            double ramasDeScazut = entry.getValue();

            for (Stoc s : allStocks) {
                if (ramasDeScazut <= 0) {
                    break;
                }

                if (s.getIngredient().equalsIgnoreCase(numeIng)) {
                    double deLuat = Math.min(s.getCantitate(), ramasDeScazut);
                    s.setCantitate((int) (s.getCantitate() - deLuat));
                    ramasDeScazut -= deLuat;
                    stocService.update(s);
                }
            }
        }
    }

    public void updateOrder(Order o) {
        orderRepo.update(o);
    }

    public void deleteOrder(int id) {
        orderRepo.delete(id);
    }

    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    public Order findById(int id) {
        return orderRepo.findOne(id);
    }

    public double computeTotal(Order o) {
        return o.getItems().stream()
                .mapToDouble(i -> productRepo.findOne(i.getProduct().getId()).getPret() * i.getQuantity())
                .sum(); // [cite: 36]
    }

    public void addItem(Order o, OrderItem item) {
        o.getItems().add(item);
        orderRepo.update(o);
    }

    public void removeItem(Order o, OrderItem item) {
        o.getItems().remove(item);
        orderRepo.update(o);
    }
}