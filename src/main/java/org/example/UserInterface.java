package org.example;
import org.example.model.*;
import org.example.repository.*;
import org.example.service.*;

import java.util.*;

public class UserInterface {
    private final Scanner scanner = new Scanner(System.in);
    private final VehicleRepository vehicleRepo;
    private final UserRepository userRepo;
    private final RentalRepository rentalRepo;
    private final AuthService authService;
    private final UserService userService;
    private final RentalService rentalService;

    private User currentUser;

    public UserInterface(VehicleRepository vehicleRepo,
                         UserRepository userRepo,
                         RentalRepository rentalRepo,
                         AuthService authService,
                         UserService userService,
                         RentalService rentalService) {
        this.vehicleRepo = vehicleRepo;
        this.userRepo = userRepo;
        this.rentalRepo = rentalRepo;
        this.authService = authService;
        this.userService = userService;
        this.rentalService = rentalService;
    }

    public void start() {
        if (authenticateUser()) {
            while (true) {
                try {
                    if (currentUser.isAdmin()) adminMenu();
                    else userMenu();
                } catch (Exception e) {
                    System.err.println("Błąd: " + e.getMessage());
                    scanner.nextLine(); // wyczyść bufor
                }
            }
        } else {
            System.out.println("Niepoprawne dane logowania!");
        }
    }

    private boolean authenticateUser() {
        System.out.print("Login: ");
        String login = scanner.nextLine();
        System.out.print("Hasło: ");
        String password = scanner.nextLine();
        User user = authService.login(login, password);
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    private void adminMenu() {
        System.out.println("\n===== MENU ADMINA =====");
        System.out.println("""
                1. Lista pojazdów
                2. Lista użytkowników
                3. Lista wypożyczeń użytkownika
                4. Wszystkie aktualne wypożyczenia
                5. Wypożycz pojazd dla użytkownika
                6. Dodaj pojazd
                7. Usuń pojazd
                8. Dodaj użytkownika
                9. Usuń użytkownika
                0. Wyjdź
                """);
        int choice = getValidChoice();
        switch (choice) {
            case 1 -> showAllVehicles();
            case 2 -> showUsers();
            case 3 -> showUserRentalsAdmin();
            case 4 -> showAllCurentRentals();
            case 5 -> rentVehicleForUser();
            case 6 -> addVehicle();
            case 7 -> removeVehicle();
            case 8 -> addUser();
            case 9  -> removeUser();
            case 0 -> System.exit(0);
        }
    }

    private void userMenu() {
        System.out.println("\n===== MENU UŻYTKOWNIKA =====");
        System.out.println("""
                1. Lista dostępnych pojazdów
                2. Wypożycz pojazd
                3. Moje aktualne wypożyczenia
                4. Moja historia wypożyczeń
                5. Zwróć pojazd
                0. Wyjdź
                """);
        int choice = getValidChoice();
        switch (choice) {
            case 1 -> showAvailableVehicles();
            case 2 -> rentVehicle();
            case 3 -> showUserCurrentRentals();
            case 4 -> showUserHistoryRetals();
            case 5 -> returnVehicle();
            case 0 -> System.exit(0);
        }
    }

    private int getValidChoice() {
        while (true) {
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Błąd: Podaj liczbę!");
            }
        }
    }
    private Long getValidId() {
        while (true) {
            String input = scanner.nextLine();
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Błąd: Podaj poprawne ID! (LICZBĘ!)\n");
                System.out.println("Spróbuj ponownie: ");
            }
        }
    }

    private void showAvailableVehicles() {
        List<Vehicle> availableVehicles = vehicleRepo.findByRentedFalse();

        if (availableVehicles.isEmpty()) {
            System.out.println("Brak dostępnych pojazdów.");
            return;
        }
        availableVehicles.sort(Comparator.comparingLong(Vehicle::getId));
        System.out.println("\n===== DOSTĘPNE POJAZDY =====");
        System.out.printf("%-5s %-10s %-15s %-20s %-10s %-15s %-20s %-20s %-5s \n",
                "ID", "Typ", "Marka", "Model", "Rok"," Rejestracja"," Kategoria Licencji"," Atrybuty", "Cena" );

        System.out.println("==============================================================================================================================");

        for (Vehicle v : availableVehicles) {
            String type = (v.getType().equals("Car")) ? "Samochód" :
                    (v.getType().equals("Motorcycle")) ? "Motocykl" : "Inne";

            String licenseCategory = "B";

            if (v instanceof Motorcycle) {
                Motorcycle motorcycle = (Motorcycle) v;
                licenseCategory = motorcycle.getLicenceCategory();
            }

            String attributes = (v.getAttributes() != null) ? v.getAttributes().toString() : "Brak atrybutów";

            System.out.printf("%-5s %-10s %-15s %-20s %-10s %-15s %-20s %-20s %-5s\n",
                    v.getId(),
                    type,
                    v.getBrand(),
                    v.getModel(),
                    v.getYear(),
                    v.getPlate(),
                    licenseCategory,
                    v.getAttributes(),
                    v.getPrice());
        }
    }

    private void showAllVehicles() {
    List<Vehicle> allVehicles  = vehicleRepo.findAll();
    if (allVehicles.isEmpty()){
        System.out.println("Brak pojazdów w bazie.");
        return;
        }
        allVehicles.sort(Comparator.comparingLong(Vehicle::getId));

        System.out.println("\n===== WSZYSTKIE POJAZDY =====");
        System.out.printf("%-5s %-10s %-15s %-20s %-10s %-15s %-20s %-20s %-5s \n",
                "ID", "Typ", "Marka", "Model", "Rok"," Rejestracja"," Kategoria Licencji"," Atrybuty", "Cena" );
        System.out.println("==============================================================================================================================");

        for (Vehicle v : allVehicles) {
            String type = (v.getType().equals("Car")) ? "Samochód" :
                    (v.getType().equals("Motorcycle")) ? "Motocykl" : "Inne";

            String licenseCategory = "B";

            if (v instanceof Motorcycle) {
                Motorcycle motorcycle = (Motorcycle) v;
                licenseCategory = motorcycle.getLicenceCategory();
            }

            String attributes = (v.getAttributes() != null) ? v.getAttributes().toString() : "Brak atrybutów";

            String rented = v.isRented() ? "Wypożyczony" : "Dostępny";

            System.out.printf("%-5s %-10s %-15s %-20s %-10s %-15s %-20s %-20s %-5s %-10s \n",
                    v.getId(),
                    type,
                    v.getBrand(),
                    v.getModel(),
                    v.getYear(),
                    v.getPlate(),
                    licenseCategory,
                    v.getAttributes(),
                    v.getPrice(),
                    rented);
        }
    }
    private void showUsers() {
        List<User> users = userRepo.findAll();
        if (users.isEmpty()) {
            System.out.println("Brak użytkowników w bazie.");
            return;
        }
        System.out.println("\n===== WSZYSCY UŻYTKOWNICY =====");
        System.out.printf("%-5s %-10s %-15s %-20s \n", "ID", "Login", "Hasło", "Rola");
        System.out.println("=========================================================");
        for (User user : users) {
            System.out.printf("%-5s %-10s %-15s %-20s \n",
                    user.getId(),
                    user.getLogin(),
                    user.getPassword(),
                    user.isAdmin() ? "Admin" : "Użytkownik");
        }
    }

    private void showUserRentalsAdmin() {
        System.out.print("Podaj login użytkownika: ");
        String login = scanner.nextLine();
        User user = userRepo.findByLogin(login);

        if (user == null) {
            System.out.println("Nie znaleziono użytkownika.");
            return;
        }

        List<Rental> userRentals = rentalRepo.findByUserId(user.getId());
        if (userRentals.isEmpty()) {
            System.out.println("Użytkownik nie posiada żadnych wypożyczeń.");
            return;
        }

        System.out.printf("====== WYPOŻYCZENIA UŻYTKOWNIKA: %s ======\n", user.getLogin());

        for (Rental rental : userRentals) {
            Vehicle vehicle = vehicleRepo.findById(rental.getVehicleId());
            if (vehicle == null) continue;

            String status = (rental.getReturnDate() == null) ? "AKTYWNE" : "ZWRÓCONE";
            String returnInfo = (rental.getReturnDate() != null)
                    ? rental.getReturnDate().toString()
                    : "NIE ZWRÓCONO";

            System.out.println("ID: " + vehicle.getId() +
                    ", Pojazd: " + vehicle.getBrand() + " " + vehicle.getModel() +
                    ", Data wypożyczenia: " + rental.getRentDate() +
                    ", Data zwrotu: " + returnInfo +
                    ", Status: " + status);
        }
    }


    private void showUserCurrentRentals() {
        System.out.printf("====== AKTUALNE WYPOŻYCZENIA: %s ======\n", currentUser.getLogin());

        List<Rental> currentRentals = rentalRepo.findByUserId(currentUser.getId()).stream()
                .filter(rental -> rental.getReturnDate() == null)
                .toList();

        if (currentRentals.isEmpty()) {
            System.out.println("Brak aktywnych wypożyczeń.");
            return;
        }

        currentRentals.forEach(rental -> {
            Vehicle vehicle = vehicleRepo.findById(rental.getVehicleId());
            if (vehicle != null) {
                System.out.println("ID: " + vehicle.getId() +
                        ", Pojazd: " + vehicle.getBrand() + " " + vehicle.getModel() +
                        ", Data wypożyczenia: " + rental.getRentDate());
            }
        });
    }
    private void showUserHistoryRetals() {
        System.out.printf("====== HISTORIA WYPOŻYCZEŃ: %s ======\n", currentUser.getLogin());
        rentalRepo.findByUserId(currentUser.getId()).stream()
                .filter(rental -> rental.getReturnDate() != null)
                .forEach(rental -> {
                    Vehicle vehicle = vehicleRepo.findById(rental.getVehicleId());
                    if (vehicle != null) {
                        System.out.println("ID: " + vehicle.getId() +
                                ", Pojazd: " + vehicle.getBrand() + " " + vehicle.getModel() +
                                ", Data wypożyczenia: " + rental.getRentDate() +
                                ", Data zwrotu: " + rental.getReturnDate());
                    }
                });
    }
    private void showUserRentals() { //pokazuje wszystko, nie uzywane
        rentalRepo.findAll().stream()
                .filter(r -> r.getUserId().equals(currentUser.getId()))
                .forEach(rental -> {
                    Vehicle vehicle = vehicleRepo.findById(rental.getVehicleId());
                    if (vehicle != null) {
                        System.out.println("ID: " + vehicle.getId() +
                                ", Pojazd: " + vehicle.getBrand() + " " + vehicle.getModel() +
                                ", Data wypożyczenia: " + rental.getRentDate() +
                                ", Data zwrotu: " + rental.getReturnDate());
                    }
                });
    }
    private void showAllCurentRentals() {
        List<Rental> allRentals = rentalRepo.findAll();

        List<Rental> activeRentals = allRentals.stream()
                .filter(rental -> rental.getReturnDate() == null)
                .toList();

        if (activeRentals.isEmpty()) {
            System.out.println("Brak aktywnych wypożyczeń.");
            return;
        }

        System.out.println("\n===== WSZYSTKIE AKTUALNE WYPOŻYCZENIA =====");
        System.out.printf("%-15s %-10s %-20s %-20s %-20s\n", "Login", "ID pojazdu", "Marka", "Model", "Data wypożyczenia");
        System.out.println("===============================================================================");

        for (Rental rental : activeRentals) {
            User user = userRepo.findById(rental.getUserId());
            Vehicle vehicle = vehicleRepo.findById(rental.getVehicleId());

            if (user != null && vehicle != null) {
                System.out.printf("%-15s %-10s %-20s %-20s %-20s\n",
                        user.getLogin(),
                        vehicle.getId(),
                        vehicle.getBrand(),
                        vehicle.getModel(),
                        rental.getRentDate().toString());
            }
        }
    }
    private void rentVehicle() {
        System.out.print("Podaj ID pojazdu: ");
        Long vehicleId = getValidId();
        Vehicle vehicle = vehicleRepo.findById(vehicleId);
        if (vehicle != null) {
            if (vehicle.isRented()) {
                System.out.println("Pojazd już wypożyczony.");
                return;
            }
            rentalService.rent(vehicle.getId(), currentUser.getId());
            System.out.println("Wypożyczono pojazd: " + vehicle.getBrand());
        } else {
            System.out.println("Nie znaleziono pojazdu.");
        }
    }

    private void rentVehicleForUser() {
        System.out.print("Login użytkownika: ");
        String login = scanner.nextLine();
        User user = userRepo.findByLogin(login);
        if (user == null) {
            System.out.println("Użytkownik nie istnieje.");
            return;
        }
        System.out.print("ID pojazdu: ");
        Long vehicleId = getValidId();
        Vehicle vehicle = vehicleRepo.findById(vehicleId);
        if (vehicle != null) {
            if (!vehicle.isRented()) {
                rentalService.rent(vehicle.getId(),user.getId());
                System.out.println("Wypożyczono pojazd dla użytkownika.");
            } else {
                System.out.println("Pojazd już wypożyczony.");
            }
        } else {
            System.out.println("Nie znaleziono pojazdu.");
        }
    }

    private void returnVehicle() {
        System.out.print("ID pojazdu do zwrotu: ");
        Long vehicleId = getValidId();
        rentalService.returnRental(vehicleId, currentUser.getId());
    }

    private void addUser() {
        System.out.print("Login: ");
        String login = scanner.nextLine();
        System.out.print("Hasło: ");
        String password = scanner.nextLine();
        System.out.print("Rola (admin/moderator/user): ");
        String roleInput = scanner.nextLine();

        Set<Role> roles = new HashSet<>();

        switch (roleInput.toLowerCase()) {
            case "admin":
                roles.add(new Role("admin"));
                break;
            case "moderator":
                roles.add(new Role("moderator"));
                break;
            case "user":
                roles.add(new Role("user"));
                break;
            default:
                System.out.println("Nieprawidłowa rola. Ustawiam domyślną rolę 'user'.");
                roles.add(new Role("user"));
                break;
        }

        User newUser = authService.register(login, password, roles);
        if (newUser != null) {
            System.out.println("Użytkownik został pomyślnie zarejestrowany!");
        } else {
            System.out.println("Błąd podczas rejestracji użytkownika.");
        }
    }

    private void removeUser() {
        System.out.print("Login do usunięcia: ");
        String login = scanner.nextLine();
        userService.deleteUserByLogin(login);
    }

    private void removeVehicle() {
        System.out.print("ID pojazdu do usunięcia: ");
        Long vehicleId = getValidId();
        vehicleRepo.deleteById(vehicleId);
    }

    private void addVehicle() {
        System.out.print("Typ pojazdu (Car/Motorcycle): ");
        String type = scanner.nextLine();

        System.out.print("Marka: ");
        String brand = scanner.nextLine();
        System.out.print("Model: ");
        String model = scanner.nextLine();
        System.out.print("Rok: ");
        int year = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Cena: ");
        int price = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Tablica: ");
        String plate = scanner.nextLine();

        Vehicle vehicle;
        if (type.equalsIgnoreCase("Motorcycle")) {
            System.out.print("Kategoria prawa jazdy: ");
            String cat = scanner.nextLine();
            vehicle = new Motorcycle(brand, model, year, price, cat, plate, new HashMap<>());
        } else if (type.equalsIgnoreCase("Car")) {
            vehicle = new Car(brand, model, year, price, plate, new HashMap<>());
        } else {
            System.out.println("Nieprawidłowy typ pojazdu.");
            return;
        }

        vehicleRepo.save(vehicle);
        System.out.println("Pojazd dodany do bazy.");
    }
}