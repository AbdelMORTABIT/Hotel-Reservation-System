import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

// Room Types
enum RoomType {STANDARD, JUNIOR, SUITE,}

// Room entity
class Room {

    int roomNumber;
    RoomType roomType;
    int pricePerNight;

    Room(int roomNumber, RoomType roomType, int pricePerNight) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
    }
}

// User entity
class User {

    int id;
    int balance;

    User(int id, int balance) {
        this.id = id;
        this.balance = balance;
    }
}

// Booking entity
class Booking {

    Room roomSnapshot;
    User userSnapshot;
    Date checkIn;
    Date checkOut;
    int totalPrice;

    Booking(Room room, User user, Date checkIn, Date checkOut, int totalPrice) {
        this.roomSnapshot = new Room(
            room.roomNumber,
            room.roomType,
            room.pricePerNight
        );
        this.userSnapshot = new User(user.id, user.balance);
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalPrice = totalPrice;
    }
}

public class Service {

    ArrayList<Room> rooms = new ArrayList<>();
    ArrayList<User> users = new ArrayList<>();
    ArrayList<Booking> bookings = new ArrayList<>();

    public void setRoom(
        int roomNumber,
        RoomType roomType,
        int roomPricePerNight
    ) {
        Room existing = null;
        for (Room r : rooms) {
            if (r.roomNumber == roomNumber) {
                existing = r;
                break;
            }
        }
        if (existing != null) {
            existing.roomType = roomType;
            existing.pricePerNight = roomPricePerNight;
        } else {
            rooms.add(new Room(roomNumber, roomType, roomPricePerNight));
        }
    }

    public void setUser(int userId, int balance) {
        for (User u : users) {
            if (u.id == userId) return;
        }
        users.add(new User(userId, balance));
    }

    public void bookRoom(
        int userId,
        int roomNumber,
        Date checkIn,
        Date checkOut
    ) {
        try {
            if (!checkIn.before(checkOut) && !checkIn.equals(checkOut)) {
                throw new IllegalArgumentException(
                    "Check-in date must be before check-out date"
                );
            }
            User user = null;
            Room room = null;

            for (User u : users) {
                if (u.id == userId) user = u;
            }

            if (user == null) throw new IllegalArgumentException(
                "User not found"
            );

            for (Room r : rooms) {
                if (r.roomNumber == roomNumber) room = r;
            }
            if (room == null) throw new IllegalArgumentException(
                "Room not found"
            );

            // Check room availability
            for (Booking b : bookings) {
                if (b.roomSnapshot.roomNumber == roomNumber) {
                    if (
                        !(checkOut.before(b.checkIn) ||
                            checkIn.after(b.checkOut))
                    ) {
                        throw new IllegalArgumentException(
                            "Room already booked in this period"
                        );
                    }
                }
            }

            long diff = checkOut.getTime() - checkIn.getTime();
            int nights = (int) TimeUnit.DAYS.convert(
                diff,
                TimeUnit.MILLISECONDS
            );
            if (nights <= 0) throw new IllegalArgumentException(
                "Invalid number of nights"
            );

            int totalPrice = nights * room.pricePerNight;
            if (user.balance < totalPrice) throw new IllegalArgumentException(
                "Insufficient balance"
            );

            user.balance -= totalPrice;

            bookings.add(
                new Booking(room, user, checkIn, checkOut, totalPrice)
            );
            System.out.println(
                "Booking successful for user " +
                    userId +
                    " in room " +
                    roomNumber
            );
        } catch (Exception e) {
            System.out.println(
                "Booking failed for user " +
                    userId +
                    " in room " +
                    roomNumber +
                    ": " +
                    e.getMessage()
            );
        }
    }

    public void printAll() {
        System.out.println("Rooms:");
        ArrayList<Room> revRooms = new ArrayList<>(rooms);
        Collections.reverse(revRooms);
        for (Room r : revRooms) {
            System.out.println(
                "Room " +
                    r.roomNumber +
                    " - " +
                    r.roomType +
                    " - " +
                    r.pricePerNight
            );
        }

        System.out.println("\nBookings:");
        ArrayList<Booking> revBookings = new ArrayList<>(bookings);
        Collections.reverse(revBookings);
        for (Booking b : revBookings) {
            System.out.println(
                "User " +
                    b.userSnapshot.id +
                    " booked Room " +
                    b.roomSnapshot.roomNumber +
                    " (" +
                    b.roomSnapshot.roomType +
                    ") from " +
                    b.checkIn +
                    " to " +
                    b.checkOut +
                    " Total: " +
                    b.totalPrice
            );
        }
    }

    public void printAllUsers() {
        System.out.println("Users:");
        ArrayList<User> revUsers = new ArrayList<>(users);
        Collections.reverse(revUsers);
        for (User u : revUsers) {
            System.out.println("User " + u.id + " - Balance: " + u.balance);
        }
    }

    public static void main(String[] args) {
        Service service = new Service();

        service.setRoom(1, RoomType.STANDARD, 1000);
        service.setRoom(2, RoomType.JUNIOR, 2000);
        service.setRoom(3, RoomType.SUITE, 3000);

        service.setUser(1, 5000);
        service.setUser(2, 10000);

        try {
            Date d1 = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(
                "30/06/2026"
            );
            Date d2 = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(
                "07/07/2026"
            );
            Date d3 = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(
                "08/07/2026"
            );
            Date d4 = new java.text.SimpleDateFormat("dd/MM/yyyy").parse(
                "09/07/2026"
            );

            service.bookRoom(1, 2, d1, d2);
            service.bookRoom(1, 2, d2, d1);
            service.bookRoom(1, 1, d2, d3);
            service.bookRoom(2, 1, d2, d4);
            service.bookRoom(2, 3, d2, d3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        service.setRoom(1, RoomType.SUITE, 10000);
        System.out.println(
            "\n------------------- Print All -------------------"
        );
        service.printAll();
        System.out.println(
            "\n------------------- Print All Users -------------------"
        );
        service.printAllUsers();
    }
}
