import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.HashMap;

class Car {
    private String carId;
    private String brand;
    private String model;
    private double basePricePerDay;
    private boolean isAvailable;

    public Car(String carId, String brand, String model, double basePricePerDay) {
        this.carId = carId;
        this.brand = brand;
        this.model = model;
        this.basePricePerDay = basePricePerDay;
        this.isAvailable = true;
    }
    public String getCarId() {
        return carId;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public double getBasePricePerDay() {
        return basePricePerDay;
    }

    public double calculatePrice(int rentalDays) {
        return basePricePerDay * rentalDays;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void rent() {
        isAvailable = false;
    }

    public void returnCar() {
        isAvailable = true;
    }

    @Override
    public String toString() {
        return carId + " - " + brand + " " + model + (isAvailable ? " (Available)" : " (Rented)") +
                String.format(" | $%.2f/day", basePricePerDay);
    }
}

// Polymorphism: specialized car types override pricing behavior
class EconomyCar extends Car {
    public EconomyCar(String carId, String brand, String model, double basePricePerDay) {
        super(carId, brand, model, basePricePerDay);
    }

    @Override
    public double calculatePrice(int rentalDays) {
        double price = getBasePricePerDay() * rentalDays;
        // Economy discount for long rentals
        if (rentalDays >= 7) {
            price *= 0.9; // 10% off
        }
        return price;
    }
}

class SuvCar extends Car {
    public SuvCar(String carId, String brand, String model, double basePricePerDay) {
        super(carId, brand, model, basePricePerDay);
    }

    @Override
    public double calculatePrice(int rentalDays) {
        // Slight SUV multiplier
        return getBasePricePerDay() * 1.15 * rentalDays;
    }
}

class LuxuryCar extends Car {
    public LuxuryCar(String carId, String brand, String model, double basePricePerDay) {
        super(carId, brand, model, basePricePerDay);
    }

    @Override
    public double calculatePrice(int rentalDays) {
        // Luxury multiplier with optional long-rental perk
        double price = getBasePricePerDay() * 1.6 * rentalDays;
        if (rentalDays >= 5) {
            price -= 50.0; // flat perk
        }
        return Math.max(price, 0.0);
    }
}

class Customer {
    private String customerId;
    private String name;

    public Customer(String customerId, String name) {
        this.customerId = customerId;
        this.name = name;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }
}

class Rental {
    private String rentalId;
    private Car car;
    private Customer customer;
    private int days;
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalCost;
    private Payment payment;
    private boolean active;
    private String discountCode; // optional
    private double subtotalBeforeDiscount; // base price before discount and tax
    private double discountAmount; // absolute amount off
    private double taxAmount; // absolute tax amount

    public Rental(String rentalId, Car car, Customer customer, int days, LocalDate startDate, double totalCost) {
        this.rentalId = rentalId;
        this.car = car;
        this.customer = customer;
        this.days = days;
        this.startDate = startDate;
        this.endDate = startDate.plusDays(days);
        this.totalCost = totalCost;
        this.active = true;
    }

    public String getRentalId() { return rentalId; }
    public Car getCar() {
        return car;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getDays() {
        return days;
    }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getTotalCost() { return totalCost; }
    public Payment getPayment() { return payment; }
    public boolean isActive() { return active; }
    public String getDiscountCode() { return discountCode; }
    public double getSubtotalBeforeDiscount() { return subtotalBeforeDiscount; }
    public double getDiscountAmount() { return discountAmount; }
    public double getTaxAmount() { return taxAmount; }

    public void attachPayment(Payment payment) {
        this.payment = payment;
    }

    public void markReturned() {
        this.active = false;
    }

    public void applyExtension(int extraDays) {
        this.days += extraDays;
        this.endDate = this.startDate.plusDays(this.days);
    }

    public void updateTotalCost(double newTotalCost) {
        this.totalCost = newTotalCost;
    }

    public void setPricingBreakdown(String discountCode, double subtotalBeforeDiscount, double discountAmount, double taxAmount, double total) {
        this.discountCode = discountCode;
        this.subtotalBeforeDiscount = subtotalBeforeDiscount;
        this.discountAmount = discountAmount;
        this.taxAmount = taxAmount;
        this.totalCost = total;
    }
}

enum PaymentMethod { CASH, CARD }

class Payment {
    private String paymentId;
    private double amount;
    private PaymentMethod method;
    private boolean successful;
    private LocalDate paidOn;

    public Payment(String paymentId, double amount, PaymentMethod method) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.method = method;
        this.successful = false;
    }

    public boolean process() {
        // Simulated processing
        this.successful = true;
        this.paidOn = LocalDate.now();
        return successful;
    }

    public String getPaymentId() { return paymentId; }
    public double getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public boolean isSuccessful() { return successful; }
    public LocalDate getPaidOn() { return paidOn; }

    @Override
    public String toString() {
        return "Payment{" + paymentId + ", " + method + ", $" + String.format("%.2f", amount) +
                ", " + (successful ? "SUCCESS" : "PENDING") + "}";
    }
}

class CarRentalSystem {
    private List<Car> cars;
    private List<Customer> customers;
    private List<Rental> rentals;
    private double taxRate = 0.08; // 8% tax
    private String pendingDiscountCode = ""; // transient during booking flow
    private Map<String, Double> discountCodeToPercent = new HashMap<>();
    private double seasonalMultiplier = 0.10; // +10% in peak months
    private double weekendMultiplier = 0.05;  // +5% if any weekend day
    private int seasonalStartMonth = 6; // June
    private int seasonalEndMonth = 8;   // August

    public CarRentalSystem() {
        cars = new ArrayList<>();
        customers = new ArrayList<>();
        rentals = new ArrayList<>();
        // Seed default discount codes
        discountCodeToPercent.put("SAVE10", 0.10);
        discountCodeToPercent.put("SAVE15", 0.15);
        discountCodeToPercent.put("VIP20", 0.20);
    }

    public void addCar(Car car) {
        cars.add(car);
    }

    public void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public Customer findCustomerById(String customerId) {
        for (Customer c : customers) {
            if (c.getCustomerId().equalsIgnoreCase(customerId)) return c;
        }
        return null;
    }

    public List<Customer> findCustomersByName(String name) {
        List<Customer> result = new ArrayList<>();
        for (Customer c : customers) {
            if (c.getName().equalsIgnoreCase(name)) result.add(c);
        }
        return result;
    }

    public void listAllCars() {
        System.out.println("\n== All Cars ==");
        for (Car car : cars) {
            System.out.println(car.toString());
        }
    }

    public void listAvailableCars() {
        System.out.println("\n== Available Cars ==");
        for (Car car : cars) {
            if (car.isAvailable()) {
                System.out.println(car.toString());
            }
        }
    }

    public Car findCarById(String carId) {
        for (Car car : cars) {
            if (car.getCarId().equalsIgnoreCase(carId)) {
                return car;
            }
        }
        return null;
    }

    public boolean isCarAvailable(String carId) {
        Car car = findCarById(carId);
        return car != null && car.isAvailable();
    }

    public Rental findActiveRentalByCar(Car car) {
        for (Rental rental : rentals) {
            if (rental.getCar() == car && rental.isActive()) {
                return rental;
            }
        }
        return null;
    }

    public Rental findActiveRentalById(String rentalId) {
        for (Rental rental : rentals) {
            if (rental.isActive() && rental.getRentalId().equalsIgnoreCase(rentalId)) {
                return rental;
            }
        }
        return null;
    }

    public Rental rentCar(Car car, Customer customer, int days, PaymentMethod method) {
        if (!car.isAvailable()) {
            System.out.println("Car is not available for rent.");
            return null;
        }

        LocalDate startDate = LocalDate.now();
        double base = computePolicyAdjustedBase(car, startDate, days);
        String discountCode = pendingDiscountCode; // set just before call
        double discountPct = getDiscountPercent(discountCode);
        double discountAmt = round2(base * discountPct);
        double taxable = base - discountAmt;
        double taxAmt = round2(taxable * taxRate);
        double total = round2(taxable + taxAmt);
        String rentalId = "RNT" + (rentals.size() + 1);
        Rental rental = new Rental(rentalId, car, customer, days, startDate, total);
        rental.setPricingBreakdown(discountCode, round2(base), discountAmt, taxAmt, total);

        String paymentId = "PAY" + (rentals.size() + 1);
        Payment payment = new Payment(paymentId, total, method);
        boolean ok = payment.process();
        rental.attachPayment(payment);

        if (ok) {
            car.rent();
            rentals.add(rental);
            System.out.println("Payment successful. Rental confirmed.\n" + invoiceFor(rental));
            return rental;
        } else {
            System.out.println("Payment failed. Rental not created.");
            return null;
        }
    }

    public void finalizeReturn(Car car, int actualDaysUsed, PaymentMethod method) {
        Rental rental = findActiveRentalByCar(car);
        if (rental == null) {
            System.out.println("Car was not rented.");
            return;
        }
        if (actualDaysUsed < rental.getDays()) {
            // No refunds for early return in this simple model; just close rental at booked days
            actualDaysUsed = rental.getDays();
        }
        double base = computePolicyAdjustedBase(car, rental.getStartDate(), actualDaysUsed);
        String discountCode = rental.getDiscountCode();
        double discountPct = getDiscountPercent(discountCode);
        double discountAmt = round2(base * discountPct);
        double taxable = base - discountAmt;
        double taxAmt = round2(taxable * taxRate);
        double newTotal = round2(taxable + taxAmt);
        double delta = newTotal - rental.getTotalCost();
        if (delta > 0) {
            String paymentId = "PAY" + (rentals.size() + 1) + "R";
            Payment payment = new Payment(paymentId, delta, method);
            boolean ok = payment.process();
            if (!ok) {
                System.out.println("Additional payment failed. Return aborted.");
                return;
            }
            rental.attachPayment(payment);
        }
        // Update rental record
        int extraDays = actualDaysUsed - rental.getDays();
        if (extraDays > 0) {
            rental.applyExtension(extraDays);
        }
        rental.setPricingBreakdown(discountCode, round2(base), discountAmt, taxAmt, newTotal);
        rental.markReturned();
        car.returnCar();
        System.out.println("Car returned. Final invoice:\n" + invoiceFor(rental));
    }

    public void listActiveRentals() {
        System.out.println("\n== Active Rentals ==");
        if (rentals.isEmpty()) {
            System.out.println("No rentals yet.");
            return;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Rental r : rentals) {
            if (r.isActive()) {
                System.out.println(r.getRentalId() + " | " + r.getCustomer().getName() + " | " +
                        r.getCar().getCarId() + " - " + r.getCar().getBrand() + " " + r.getCar().getModel() +
                        " | " + r.getStartDate().format(fmt) + " to " + r.getEndDate().format(fmt) +
                        String.format(" | $%.2f", r.getTotalCost()));
            }
        }
    }

    public void extendRental(String rentalId, int extraDays, PaymentMethod method) {
        if (extraDays <= 0) {
            System.out.println("Extra days must be positive.");
            return;
        }
        Rental rental = findActiveRentalById(rentalId);
        if (rental == null) {
            System.out.println("Active rental not found.");
            return;
        }
        Car car = rental.getCar();
        int newTotalDays = rental.getDays() + extraDays;
        double base = computePolicyAdjustedBase(car, rental.getStartDate(), newTotalDays);
        String discountCode = rental.getDiscountCode();
        double discountPct = getDiscountPercent(discountCode);
        double discountAmt = round2(base * discountPct);
        double taxable = base - discountAmt;
        double taxAmt = round2(taxable * taxRate);
        double newTotalCost = round2(taxable + taxAmt);
        double additionalAmount = newTotalCost - rental.getTotalCost();
        if (additionalAmount <= 0) {
            // no charge scenario (e.g., discounts)
            rental.applyExtension(extraDays);
            rental.setPricingBreakdown(discountCode, round2(base), discountAmt, taxAmt, newTotalCost);
            System.out.println("Extension applied with no additional charge.\n" + invoiceFor(rental));
            return;
        }
        String paymentId = "PAY" + (rentals.size() + 1) + "E";
        Payment payment = new Payment(paymentId, additionalAmount, method);
        boolean ok = payment.process();
        if (!ok) {
            System.out.println("Payment failed. Extension not applied.");
            return;
        }
        rental.attachPayment(payment);
        rental.applyExtension(extraDays);
        rental.setPricingBreakdown(discountCode, round2(base), discountAmt, taxAmt, newTotalCost);
        System.out.println("Extension applied successfully. Updated invoice:\n" + invoiceFor(rental));
    }

    private String invoiceFor(Rental rental) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder();
        sb.append("--- Invoice ").append(rental.getRentalId()).append(" ---\n");
        sb.append("Customer: ").append(rental.getCustomer().getName()).append("\n");
        sb.append("Car: ").append(rental.getCar().getBrand()).append(" ")
                .append(rental.getCar().getModel()).append("\n");
        sb.append("Period: ").append(rental.getStartDate().format(fmt))
                .append(" to ").append(rental.getEndDate().format(fmt))
                .append(" (" ).append(rental.getDays()).append(" days)\n");
        if (rental.getSubtotalBeforeDiscount() > 0) {
            sb.append(String.format("Subtotal: $%.2f\n", rental.getSubtotalBeforeDiscount()));
        }
        if (rental.getDiscountAmount() > 0) {
            String code = rental.getDiscountCode() == null ? "" : rental.getDiscountCode();
            sb.append(String.format("Discount %s: -$%.2f\n", code.isEmpty() ? "" : ("[" + code + "]"), rental.getDiscountAmount()));
        }
        if (rental.getTaxAmount() > 0) {
            sb.append(String.format("Tax (%.0f%%): $%.2f\n", taxRate * 100.0, rental.getTaxAmount()));
        }
        sb.append(String.format("Total: $%.2f\n", rental.getTotalCost()));
        if (rental.getPayment() != null) {
            sb.append("Payment: ").append(rental.getPayment().toString()).append("\n");
        }
        sb.append("--------------------------\n");
        return sb.toString();
    }

    // Discount logic
    private double getDiscountPercent(String code) {
        if (code == null) return 0.0;
        String c = code.trim().toUpperCase();
        if (c.isEmpty()) return 0.0;
        Double pct = discountCodeToPercent.get(c);
        return pct == null ? 0.0 : pct;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // Input helpers
    private double readDoubleInRange(Scanner scanner, String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                double val = Double.parseDouble(line.trim());
                if (val < min || val > max) {
                    System.out.println("Enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return val;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }
    private int readIntInRange(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                int val = Integer.parseInt(line.trim());
                if (val < min || val > max) {
                    System.out.println("Enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return val;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                int val = Integer.parseInt(line.trim());
                if (val <= 0) {
                    System.out.println("Enter a positive number.");
                    continue;
                }
                return val;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private String readNonEmptyLine(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (line != null && !line.trim().isEmpty()) {
                return line;
            }
            System.out.println("Input cannot be empty. Try again.");
        }
    }

    // Persistence helpers
    public void saveData(String dirPath) {
        try {
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            // cars.csv: id,type,brand,model,price,available
            try (BufferedWriter bw = Files.newBufferedWriter(dir.resolve("cars.csv"))) {
                for (Car c : cars) {
                    String type = (c instanceof LuxuryCar) ? "LuxuryCar" : (c instanceof SuvCar) ? "SuvCar" : (c instanceof EconomyCar) ? "EconomyCar" : "Car";
                    bw.write(String.join(",",
                            escape(c.getCarId()),
                            type,
                            escape(c.getBrand()),
                            escape(c.getModel()),
                            String.valueOf(c.getBasePricePerDay()),
                            String.valueOf(c.isAvailable())));
                    bw.newLine();
                }
            }

            // customers.csv: id,name
            try (BufferedWriter bw = Files.newBufferedWriter(dir.resolve("customers.csv"))) {
                for (Customer cu : customers) {
                    bw.write(String.join(",", escape(cu.getCustomerId()), escape(cu.getName())));
                    bw.newLine();
                }
            }

            DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
            // rentals.csv: rentalId,carId,customerId,days,startDate,totalCost,active,paymentId,paymentMethod,paymentAmount,paidOn,discountCode,subtotal,discountAmount,taxAmount
            try (BufferedWriter bw = Files.newBufferedWriter(dir.resolve("rentals.csv"))) {
                for (Rental r : rentals) {
                    String paymentId = (r.getPayment() != null) ? r.getPayment().getPaymentId() : "";
                    String paymentMethod = (r.getPayment() != null) ? r.getPayment().getMethod().name() : "";
                    String paymentAmount = (r.getPayment() != null) ? String.valueOf(r.getPayment().getAmount()) : "";
                    String paidOn = (r.getPayment() != null && r.getPayment().isSuccessful()) ? r.getPayment().getPaidOn().format(fmt) : "";
                    bw.write(String.join(",",
                            escape(r.getRentalId()),
                            escape(r.getCar().getCarId()),
                            escape(r.getCustomer().getCustomerId()),
                            String.valueOf(r.getDays()),
                            r.getStartDate().format(fmt),
                            String.valueOf(r.getTotalCost()),
                            String.valueOf(r.isActive()),
                            escape(paymentId),
                            paymentMethod,
                            paymentAmount,
                            paidOn,
                            escape(r.getDiscountCode() == null ? "" : r.getDiscountCode()),
                            String.valueOf(r.getSubtotalBeforeDiscount()),
                            String.valueOf(r.getDiscountAmount()),
                            String.valueOf(r.getTaxAmount())));
                    bw.newLine();
                }
            }

            // settings.csv: taxRate
            try (BufferedWriter bw = Files.newBufferedWriter(dir.resolve("settings.csv"))) {
                bw.write("taxRate," + taxRate);
                bw.newLine();
                bw.write("seasonalMultiplier," + seasonalMultiplier);
                bw.newLine();
                bw.write("weekendMultiplier," + weekendMultiplier);
                bw.newLine();
                bw.write("seasonalStartMonth," + seasonalStartMonth);
                bw.newLine();
                bw.write("seasonalEndMonth," + seasonalEndMonth);
                bw.newLine();
            }

            // coupons.csv: code,percent
            try (BufferedWriter bw = Files.newBufferedWriter(dir.resolve("coupons.csv"))) {
                for (Map.Entry<String, Double> e : discountCodeToPercent.entrySet()) {
                    bw.write(escape(e.getKey()) + "," + e.getValue());
                    bw.newLine();
                }
            }

            System.out.println("Data saved to: " + dir.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to save data: " + e.getMessage());
        }
    }

    public void loadData(String dirPath) {
        try {
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir)) {
                System.out.println("Directory does not exist: " + dir.toAbsolutePath());
                return;
            }

            List<Car> loadedCars = new ArrayList<>();
            List<Customer> loadedCustomers = new ArrayList<>();
            List<Rental> loadedRentals = new ArrayList<>();

            // Load cars
            Path carsFile = dir.resolve("cars.csv");
            if (Files.exists(carsFile)) {
                try (BufferedReader br = Files.newBufferedReader(carsFile)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] p = splitCsv(line);
                        if (p.length < 6) continue;
                        String id = unescape(p[0]);
                        String type = p[1];
                        String brand = unescape(p[2]);
                        String model = unescape(p[3]);
                        double price = Double.parseDouble(p[4]);
                        boolean available = Boolean.parseBoolean(p[5]);
                        Car car = createCarFromRecord(id, type, brand, model, price);
                        if (!available) car.rent();
                        loadedCars.add(car);
                    }
                }
            }

            // Load customers
            Path customersFile = dir.resolve("customers.csv");
            if (Files.exists(customersFile)) {
                try (BufferedReader br = Files.newBufferedReader(customersFile)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] p = splitCsv(line);
                        if (p.length < 2) continue;
                        loadedCustomers.add(new Customer(unescape(p[0]), unescape(p[1])));
                    }
                }
            }

            // Build quick lookup maps
            // Simple linear searches suffice given small lists

            // Load rentals
            Path rentalsFile = dir.resolve("rentals.csv");
            if (Files.exists(rentalsFile)) {
                try (BufferedReader br = Files.newBufferedReader(rentalsFile)) {
                    String line;
                    DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
                    while ((line = br.readLine()) != null) {
                        String[] p = splitCsv(line);
                        if (p.length < 15) continue;
                        String rentalId = unescape(p[0]);
                        String carId = unescape(p[1]);
                        String customerId = unescape(p[2]);
                        int days = Integer.parseInt(p[3]);
                        LocalDate start = LocalDate.parse(p[4], fmt);
                        double total = Double.parseDouble(p[5]);
                        boolean active = Boolean.parseBoolean(p[6]);
                        String paymentId = unescape(p[7]);
                        String methodStr = p[8];
                        String amountStr = p[9];
                        String paidOnStr = p[10];
                        String discountCode = unescape(p[11]);
                        double subtotalBefore = Double.parseDouble(p[12]);
                        double discountAmt = Double.parseDouble(p[13]);
                        double taxAmt = Double.parseDouble(p[14]);

                        Car car = null;
                        for (Car c : loadedCars) { if (c.getCarId().equalsIgnoreCase(carId)) { car = c; break; } }
                        Customer cust = null;
                        for (Customer cu : loadedCustomers) { if (cu.getCustomerId().equalsIgnoreCase(customerId)) { cust = cu; break; } }
                        if (car == null || cust == null) continue;

                        Rental r = new Rental(rentalId, car, cust, days, start, total);
                        r.setPricingBreakdown(discountCode.isEmpty() ? null : discountCode, subtotalBefore, discountAmt, taxAmt, total);
                        if (paymentId != null && !paymentId.isEmpty() && amountStr != null && !amountStr.isEmpty() && methodStr != null && !methodStr.isEmpty()) {
                            PaymentMethod pm = PaymentMethod.valueOf(methodStr);
                            double amt = Double.parseDouble(amountStr);
                            Payment pay = new Payment(paymentId, amt, pm);
                            if (paidOnStr != null && !paidOnStr.isEmpty()) {
                                // Mark processed
                                pay.process();
                            }
                            r.attachPayment(pay);
                        }
                        if (!active) {
                            r.markReturned();
                        }
                        loadedRentals.add(r);
                    }
                }
            }

            this.cars = loadedCars;
            this.customers = loadedCustomers;
            this.rentals = loadedRentals;

            // Load settings
            Path settingsFile = dir.resolve("settings.csv");
            if (Files.exists(settingsFile)) {
                try (BufferedReader br = Files.newBufferedReader(settingsFile)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] p = splitCsv(line);
                        if (p.length >= 2 && "taxRate".equalsIgnoreCase(p[0])) {
                            try { this.taxRate = Double.parseDouble(p[1]); } catch (NumberFormatException ignore) {}
                        } else if (p.length >= 2 && "seasonalMultiplier".equalsIgnoreCase(p[0])) {
                            try { this.seasonalMultiplier = Double.parseDouble(p[1]); } catch (NumberFormatException ignore) {}
                        } else if (p.length >= 2 && "weekendMultiplier".equalsIgnoreCase(p[0])) {
                            try { this.weekendMultiplier = Double.parseDouble(p[1]); } catch (NumberFormatException ignore) {}
                        } else if (p.length >= 2 && "seasonalStartMonth".equalsIgnoreCase(p[0])) {
                            try { this.seasonalStartMonth = Integer.parseInt(p[1]); } catch (NumberFormatException ignore) {}
                        } else if (p.length >= 2 && "seasonalEndMonth".equalsIgnoreCase(p[0])) {
                            try { this.seasonalEndMonth = Integer.parseInt(p[1]); } catch (NumberFormatException ignore) {}
                        }
                    }
                }
            }

            // Load coupons
            Path couponsFile = dir.resolve("coupons.csv");
            if (Files.exists(couponsFile)) {
                Map<String, Double> loaded = new HashMap<>();
                try (BufferedReader br = Files.newBufferedReader(couponsFile)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] p = splitCsv(line);
                        if (p.length < 2) continue;
                        String code = unescape(p[0]).trim().toUpperCase();
                        try {
                            double pct = Double.parseDouble(p[1]);
                            if (pct > 0 && pct < 1) loaded.put(code, pct);
                        } catch (NumberFormatException ignore) {}
                    }
                }
                if (!loaded.isEmpty()) {
                    discountCodeToPercent = loaded;
                }
            }

            System.out.println("Data loaded from: " + dir.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to load data: " + e.getMessage());
        }
    }

    private void showTaxRate() {
        System.out.printf("Current tax rate: %.2f%%%n", taxRate * 100.0);
    }

    private void listDiscountCodes() {
        System.out.println("\n== Available Discount Codes ==");
        if (discountCodeToPercent.isEmpty()) {
            System.out.println("(none)");
            return;
        }
        for (Map.Entry<String, Double> e : discountCodeToPercent.entrySet()) {
            System.out.printf("%s - %.0f%%%n", e.getKey(), e.getValue() * 100.0);
        }
    }

    private void changeTaxRate(Scanner scanner) {
        double newRate = readDoubleInRange(scanner, "Enter new tax rate percentage (e.g., 8 for 8%): ", 0.0, 50.0);
        this.taxRate = round2(newRate / 100.0);
        showTaxRate();
    }

    private void manageDiscountCodes(Scanner scanner) {
        while (true) {
            System.out.println("\n== Manage Discount Codes ==");
            System.out.println("1. List Codes");
            System.out.println("2. Add/Update Code");
            System.out.println("3. Remove Code");
            System.out.println("4. Back");
            int c = readIntInRange(scanner, "Choose: ", 1, 4);
            if (c == 1) {
                listDiscountCodes();
            } else if (c == 2) {
                String code = readNonEmptyLine(scanner, "Enter code (e.g., SAVE10): ").trim().toUpperCase();
                double pct = readDoubleInRange(scanner, "Enter percent (e.g., 10 for 10%): ", 0.0, 90.0);
                discountCodeToPercent.put(code, round2(pct / 100.0));
                System.out.println("Saved.");
            } else if (c == 3) {
                String code = readNonEmptyLine(scanner, "Enter code to remove: ").trim().toUpperCase();
                if (discountCodeToPercent.remove(code) != null) {
                    System.out.println("Removed.");
                } else {
                    System.out.println("Code not found.");
                }
            } else {
                break;
            }
        }
    }

    private double computePolicyAdjustedBase(Car car, LocalDate start, int days) {
        double base = car.calculatePrice(days);
        double multiplier = 1.0;
        // Seasonal
        int m = start.getMonthValue();
        boolean inSeason;
        if (seasonalStartMonth <= seasonalEndMonth) {
            inSeason = (m >= seasonalStartMonth && m <= seasonalEndMonth);
        } else {
            // wrap around year
            inSeason = (m >= seasonalStartMonth || m <= seasonalEndMonth);
        }
        if (inSeason) multiplier += seasonalMultiplier;
        // Weekend if any day overlaps Sat/Sun
        boolean hasWeekend = false;
        for (int i = 0; i < days; i++) {
            DayOfWeek dow = start.plusDays(i).getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) { hasWeekend = true; break; }
        }
        if (hasWeekend) multiplier += weekendMultiplier;
        return round2(base * multiplier);
    }

    private Car createCarFromRecord(String id, String type, String brand, String model, double price) {
        if ("LuxuryCar".equalsIgnoreCase(type)) return new LuxuryCar(id, brand, model, price);
        if ("SuvCar".equalsIgnoreCase(type)) return new SuvCar(id, brand, model, price);
        if ("EconomyCar".equalsIgnoreCase(type)) return new EconomyCar(id, brand, model, price);
        return new Car(id, brand, model, price);
    }

    // Simple CSV escaping to handle commas
    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }

    private String[] splitCsv(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    parts.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(ch);
                }
            }
        }
        parts.add(cur.toString());
        return parts.toArray(new String[0]);
    }

    private String unescape(String s) { return s; }

    public void listCustomerHistoryById(String customerId) {
        Customer c = findCustomerById(customerId);
        if (c == null) {
            System.out.println("Customer not found.");
            return;
        }
        listCustomerHistory(c);
    }

    public void listCustomerHistoryByName(String name) {
        List<Customer> matches = findCustomersByName(name);
        if (matches.isEmpty()) {
            System.out.println("No customer with that name.");
            return;
        }
        if (matches.size() > 1) {
            System.out.println("Multiple customers found; specify ID instead:");
            for (Customer c : matches) {
                System.out.println(c.getCustomerId() + " - " + c.getName());
            }
            return;
        }
        listCustomerHistory(matches.get(0));
    }

    private void listCustomerHistory(Customer customer) {
        System.out.println("\n== Rental History for " + customer.getName() + " (" + customer.getCustomerId() + ") ==");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        boolean any = false;
        for (Rental r : rentals) {
            if (r.getCustomer().getCustomerId().equalsIgnoreCase(customer.getCustomerId())) {
                any = true;
                String status = r.isActive() ? "ACTIVE" : "CLOSED";
                System.out.println(r.getRentalId() + " | " + r.getCar().getCarId() + " - " + r.getCar().getBrand() + " " + r.getCar().getModel()
                        + " | " + r.getStartDate().format(fmt) + " to " + r.getEndDate().format(fmt)
                        + String.format(" | $%.2f | %s", r.getTotalCost(), status));
            }
        }
        if (!any) {
            System.out.println("No rentals found for this customer.");
        }
    }

    private void showReports() {
        System.out.println("\n== Reports ==");
        int totalCars = cars.size();
        int availableCars = 0;
        for (Car c : cars) if (c.isAvailable()) availableCars++;
        int rentedCars = totalCars - availableCars;
        int activeRentals = 0;
        double totalRevenue = 0.0;
        for (Rental r : rentals) {
            if (r.isActive()) activeRentals++;
            if (!r.isActive()) totalRevenue += r.getTotalCost();
        }
        System.out.println("Total cars: " + totalCars);
        System.out.println("Available cars: " + availableCars);
        System.out.println("Rented cars: " + rentedCars);
        System.out.println("Active rentals: " + activeRentals);
        System.out.printf("Revenue (closed rentals): $%.2f%n", totalRevenue);
    }

    private void exportInvoice(String rentalId, String dirPath) {
        Rental target = null;
        for (Rental r : rentals) {
            if (r.getRentalId().equalsIgnoreCase(rentalId)) { target = r; break; }
        }
        if (target == null) {
            System.out.println("Rental not found.");
            return;
        }
        try {
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path file = dir.resolve(rentalId + "_invoice.txt");
            try (BufferedWriter bw = Files.newBufferedWriter(file)) {
                bw.write(invoiceFor(target));
            }
            System.out.println("Invoice written to: " + file.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to export invoice: " + e.getMessage());
        }
    }

    public void menu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("===== Car Rental System =====");
            System.out.println("1. List All Cars");
            System.out.println("2. List Available Cars");
            System.out.println("3. Check Availability");
            System.out.println("4. Book a Car");
            System.out.println("5. Return a Car");
            System.out.println("6. View Active Rentals");
            System.out.println("7. Extend a Rental");
            System.out.println("8. View Customer History");
            System.out.println("9. Exit");
            System.out.println("10. Save Data");
            System.out.println("11. Load Data");
            System.out.println("12. View Discount Codes");
            System.out.println("13. Change Tax Rate");
            System.out.println("14. Reports");
            System.out.println("15. Export Invoice");
            System.out.println("16. Manage Discount Codes");
            int choice = readIntInRange(scanner, "Enter your choice: ", 1, 16);

            if (choice == 1) {
                listAllCars();
            } else if (choice == 2) {
                listAvailableCars();
            } else if (choice == 3) {
                String carIdCheck = readNonEmptyLine(scanner, "Enter car ID to check: ");
                System.out.println(isCarAvailable(carIdCheck) ? "Available" : "Not available");
            } else if (choice == 4) {
                System.out.println("\n== Book a Car ==\n");
                String customerName = readNonEmptyLine(scanner, "Enter your name: ");

                listAvailableCars();
                String carId = readNonEmptyLine(scanner, "\nEnter the car ID you want to rent: ");
                int rentalDays = readPositiveInt(scanner, "Enter the number of days for rental: ");
                String discount = readNonEmptyLine(scanner, "Enter discount code (or press Enter to skip): ");
                if (discount.equalsIgnoreCase("skip")) discount = "";
                pendingDiscountCode = discount; // store temporarily for rentCar

                Car selectedCar = findCarById(carId);
                if (selectedCar == null || !selectedCar.isAvailable()) {
                    System.out.println("Invalid car selection or not available.");
                    continue;
                }

                Customer newCustomer = new Customer("CUS" + (customers.size() + 1), customerName);
                addCustomer(newCustomer);

                int pm = readIntInRange(scanner, "Payment method (1-Cash, 2-Card): ", 1, 2);
                PaymentMethod method = (pm == 2) ? PaymentMethod.CARD : PaymentMethod.CASH;

                double base = selectedCar.calculatePrice(rentalDays);
                double discPct = getDiscountPercent(discount);
                double discAmt = round2(base * discPct);
                double taxable = base - discAmt;
                double taxAmt = round2(taxable * taxRate);
                double totalPreview = round2(taxable + taxAmt);
                System.out.printf("Subtotal: $%.2f%n", base);
                if (discAmt > 0) System.out.printf("Discount: -$%.2f (%s)%n", discAmt, discount.toUpperCase());
                System.out.printf("Tax (%.0f%%): $%.2f%n", taxRate * 100.0, taxAmt);
                System.out.printf("Total: $%.2f%n", totalPreview);
                String confirm = readNonEmptyLine(scanner, "Confirm rental (Y/N): ");
                if (confirm.equalsIgnoreCase("Y")) {
                    rentCar(selectedCar, newCustomer, rentalDays, method);
                } else {
                    System.out.println("Rental canceled.");
                }
            } else if (choice == 5) {
                System.out.println("\n== Return a Car ==\n");
                String carId = readNonEmptyLine(scanner, "Enter the car ID you want to return: ");
                Car carToReturn = findCarById(carId);
                if (carToReturn != null && !carToReturn.isAvailable()) {
                    int actualDays = readPositiveInt(scanner, "Enter actual total days used (>= booked): ");
                    int pm3 = readIntInRange(scanner, "Payment method for any extra charges (1-Cash, 2-Card): ", 1, 2);
                    PaymentMethod method3 = (pm3 == 2) ? PaymentMethod.CARD : PaymentMethod.CASH;
                    finalizeReturn(carToReturn, actualDays, method3);
                } else {
                    System.out.println("Invalid car ID or car is not currently rented.");
                }
            } else if (choice == 6) {
                listActiveRentals();
            } else if (choice == 7) {
                System.out.println("\n== Extend a Rental ==\n");
                listActiveRentals();
                String rid = readNonEmptyLine(scanner, "Enter Rental ID to extend: ");
                int extra = readPositiveInt(scanner, "Enter extra days: ");
                int pm2 = readIntInRange(scanner, "Payment method (1-Cash, 2-Card): ", 1, 2);
                PaymentMethod method2 = (pm2 == 2) ? PaymentMethod.CARD : PaymentMethod.CASH;
                extendRental(rid, extra, method2);
            } else if (choice == 8) {
                System.out.println("\n== View Customer History ==\n");
                int sel = readIntInRange(scanner, "Search by (1) ID or (2) Name: ", 1, 2);
                if (sel == 1) {
                    String cid = readNonEmptyLine(scanner, "Enter Customer ID: ");
                    listCustomerHistoryById(cid);
                } else {
                    String cname = readNonEmptyLine(scanner, "Enter Customer Name: ");
                    listCustomerHistoryByName(cname);
                }
            } else if (choice == 9) {
                break;
            } else if (choice == 10) {
                String dir = readNonEmptyLine(scanner, "Enter directory to save (e.g., data): ");
                saveData(dir);
            } else if (choice == 11) {
                String dir = readNonEmptyLine(scanner, "Enter directory to load (e.g., data): ");
                loadData(dir);
            } else if (choice == 12) {
                listDiscountCodes();
                showTaxRate();
            } else if (choice == 13) {
                changeTaxRate(scanner);
            } else if (choice == 14) {
                showReports();
            } else if (choice == 15) {
                String rid = readNonEmptyLine(scanner, "Enter Rental ID to export invoice: ");
                String dir = readNonEmptyLine(scanner, "Enter directory to save invoice (e.g., invoices): ");
                exportInvoice(rid, dir);
            } else if (choice == 16) {
                manageDiscountCodes(scanner);
            } else {
                System.out.println("Invalid choice. Please enter a valid option.");
            }
        }

        scanner.close();
        System.out.println("\nThank you for using the Car Rental System!");
    }

}
public class Main{
    public static void main(String[] args) {
        CarRentalSystem rentalSystem = new CarRentalSystem();

        Car car1 = new EconomyCar("C001", "Toyota", "Camry", 60.0);
        Car car2 = new SuvCar("C002", "Honda", "Accord", 70.0);
        Car car3 = new LuxuryCar("C003", "Mahindra", "Thar", 150.0);
        rentalSystem.addCar(car1);
        rentalSystem.addCar(car2);
        rentalSystem.addCar(car3);

        rentalSystem.menu();
    }
}
