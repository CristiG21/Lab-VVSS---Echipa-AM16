package drinkshop.export;

import drinkshop.domain.Order;
import drinkshop.domain.OrderItem;
import drinkshop.domain.Product;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvExporter {
    public static void exportOrders(List<Product> products, List<Order> orders, String path) {
        try (FileWriter w = new FileWriter(path)) {
            // Header-ul CSV-ului cu câmpuri text protejate
            w.write("\"OrderId\",\"Product\",\"Quantity\",\"Price\"\n");

            double sum = 0.0;
            for (Order o : orders) {
                for (OrderItem i : o.getItems()) {
                    Product p = products.stream().filter(p1 -> i.getProduct().getId() == p1.getId()).toList().get(0);

                    // Corecție C07: Încadrarea textului (nume produs) între ghilimele
                    w.write(o.getId() + ",\"" + p.getNume() + "\"," + i.getQuantity() + "," + i.getTotal() + "\n");
                }

                // Formatare consecventă pentru rândurile de sumar
                w.write("\"total order: " + o.getTotal() + " RON\"\n");
                w.write("\"-------------------------------\"\n");
                sum += o.getTotal();
            }

            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            // Protejăm și mesajul final de sumar
            w.write("\"TOTAL OF " + date + " is: " + sum + " RON\"\n");

        } catch (IOException e) {
            throw new RuntimeException("Eroare la procesarea ieșirii (C07): " + e.getMessage(), e);
        }
    }
}