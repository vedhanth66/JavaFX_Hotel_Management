package com.zenith.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;

public class HotelDataStore {

    private static final HotelDataStore INSTANCE = new HotelDataStore();

    private final ObservableList<Room> rooms = FXCollections.observableArrayList();
    private final ObservableList<Guest> guests = FXCollections.observableArrayList();
    private final ObservableList<Booking> bookings = FXCollections.observableArrayList();

    // ─── Settings Data ───────────────────────────────────────────
    private String hotelName = "OOSD hotels";
    private String currency = "INR (₹)";
    private String contactEmail = "admin@zenithhotel.com";
    private String contactPhone = "+1-800-ZENITH";

    private boolean emailNotifications = true;
    private boolean soundAlerts = true;
    private boolean autoCheckoutReminders = true;
    private boolean darkMode = false; // changed default to light for floral theme
    private boolean uiAnimations = true;
    private boolean compactRoomView = false;

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public boolean isEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }
    public boolean isSoundAlerts() { return soundAlerts; }
    public void setSoundAlerts(boolean soundAlerts) { this.soundAlerts = soundAlerts; }
    public boolean isAutoCheckoutReminders() { return autoCheckoutReminders; }
    public void setAutoCheckoutReminders(boolean autoCheckoutReminders) { this.autoCheckoutReminders = autoCheckoutReminders; }
    public boolean isDarkMode() { return darkMode; }
    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }
    public boolean isUiAnimations() { return uiAnimations; }
    public void setUiAnimations(boolean uiAnimations) { this.uiAnimations = uiAnimations; }
    public boolean isCompactRoomView() { return compactRoomView; }
    public void setCompactRoomView(boolean compactRoomView) { this.compactRoomView = compactRoomView; }

    public String formatCurrency(double amount) {
        String sym = "$";
        if (currency != null) {
            if (currency.contains("€")) sym = "€";
            else if (currency.contains("£")) sym = "£";
            else if (currency.contains("¥")) sym = "¥";
            else if (currency.contains("د.إ")) sym = "AED ";
            else if (currency.contains("₹")) sym = "₹";
        }
        return String.format("%s%.2f", sym, amount);
    }

    private HotelDataStore() {
        initializeSampleData();
    }

    public static HotelDataStore getInstance() { return INSTANCE; }

    public ObservableList<Room> getRooms() { return rooms; }
    public ObservableList<Guest> getGuests() { return guests; }
    public ObservableList<Booking> getBookings() { return bookings; }

    // ─── Booking Operations ──────────────────────────────────────
    public Booking createBooking(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut, String notes) {
        Booking b = new Booking(guest, room, checkIn, checkOut);
        b.setNotes(notes);
        room.setStatus(Room.RoomStatus.RESERVED);
        room.setCurrentGuestName(guest.getFullName());
        bookings.add(b);
        ActivityLog.getInstance().log("Booking #" + b.getId() + " created for " + guest.getFullName() + " in Room " + room.getRoomNumber());
        return b;
    }

    public void checkIn(Booking b) {
        b.setStatus(Booking.BookingStatus.CHECKED_IN);
        b.getRoom().setStatus(Room.RoomStatus.OCCUPIED);
        b.getGuest().setTotalStays(b.getGuest().getTotalStays() + 1);
        ActivityLog.getInstance().log("Check-in: " + b.getGuestName() + " → Room " + b.getRoomNumber());
    }

    public void checkOut(Booking b) {
        b.setStatus(Booking.BookingStatus.CHECKED_OUT);
        b.getRoom().setStatus(Room.RoomStatus.AVAILABLE);
        b.getRoom().setCurrentGuestName("");
        b.getGuest().setTotalSpent(b.getGuest().getTotalSpent() + b.getTotalAmount());
        ActivityLog.getInstance().log("Check-out: " + b.getGuestName() + " from Room " + b.getRoomNumber() + " ($" + String.format("%.0f", b.getTotalAmount()) + ")");
    }

    public void cancelBooking(Booking b) {
        b.setStatus(Booking.BookingStatus.CANCELLED);
        b.getRoom().setStatus(Room.RoomStatus.AVAILABLE);
        b.getRoom().setCurrentGuestName("");
        ActivityLog.getInstance().log("Booking #" + b.getId() + " cancelled (" + b.getGuestName() + ")");
    }

    // ─── Guest Operations ────────────────────────────────────────
    public Guest addGuest(String name, String email, String phone, String nationality, boolean vip) {
        Guest g = new Guest(name, email, phone, nationality);
        g.setVip(vip);
        guests.add(g);
        ActivityLog.getInstance().log("Guest added: " + name + (vip ? " (VIP)" : ""));
        return g;
    }

    public void removeGuest(Guest g) {
        guests.remove(g);
        ActivityLog.getInstance().log("Guest removed: " + g.getFullName());
    }

    // ─── Room Operations ─────────────────────────────────────────
    public void setRoomMaintenance(Room room, boolean maintenance) {
        if (maintenance) {
            room.setStatus(Room.RoomStatus.MAINTENANCE);
            ActivityLog.getInstance().log("Room " + room.getRoomNumber() + " set to Maintenance");
        } else {
            room.setStatus(Room.RoomStatus.AVAILABLE);
            ActivityLog.getInstance().log("Room " + room.getRoomNumber() + " restored to Available");
        }
    }

    // ─── Statistics ──────────────────────────────────────────────
    public double getOccupancyRate() {
        if (rooms.isEmpty()) return 0;
        long occupied = rooms.stream()
            .filter(r -> r.getStatus() == Room.RoomStatus.OCCUPIED || r.getStatus() == Room.RoomStatus.RESERVED)
            .count();
        return (double) occupied / rooms.size();
    }

    public double getTotalRevenue() {
        return bookings.stream()
            .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
            .mapToDouble(Booking::getTotalAmount)
            .sum();
    }

    public long getActiveBookingsCount() {
        return bookings.stream()
            .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED || b.getStatus() == Booking.BookingStatus.CHECKED_IN)
            .count();
    }

    public long getTodayCheckIns() {
        LocalDate today = LocalDate.now();
        return bookings.stream()
            .filter(b -> b.getCheckInDate() != null && b.getCheckInDate().equals(today))
            .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED || b.getStatus() == Booking.BookingStatus.CHECKED_IN)
            .count();
    }

    public long getAvailableRoomCount() {
        return rooms.stream().filter(r -> r.getStatus() == Room.RoomStatus.AVAILABLE).count();
    }

    public long getOccupiedRoomCount() {
        return rooms.stream().filter(r -> r.getStatus() == Room.RoomStatus.OCCUPIED).count();
    }

    public long getReservedRoomCount() {
        return rooms.stream().filter(r -> r.getStatus() == Room.RoomStatus.RESERVED).count();
    }

    public long getMaintenanceRoomCount() {
        return rooms.stream().filter(r -> r.getStatus() == Room.RoomStatus.MAINTENANCE).count();
    }

    public double getRevenueByRoomType(Room.RoomType type) {
        return bookings.stream()
            .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
            .filter(b -> b.getRoom() != null && b.getRoom().getType() == type)
            .mapToDouble(Booking::getTotalAmount)
            .sum();
    }

    public long getBookingCountByRoomType(Room.RoomType type) {
        return bookings.stream()
            .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
            .filter(b -> b.getRoom() != null && b.getRoom().getType() == type)
            .count();
    }

    public double getAverageStayDuration() {
        return bookings.stream()
            .filter(b -> b.getStatus() != Booking.BookingStatus.CANCELLED)
            .mapToLong(Booking::getNights)
            .average()
            .orElse(0);
    }

    // ─── Reset ───────────────────────────────────────────────────
    public void resetAllData() {
        rooms.clear();
        guests.clear();
        bookings.clear();
        initializeSampleData();
        ActivityLog.getInstance().log("All data has been reset to defaults");
    }

    // ─── Sample Data ─────────────────────────────────────────────
    private void initializeSampleData() {
        // === ROOMS (36 rooms across 3 floors) ===
        // Floor 1: Standard & Deluxe
        for (int i = 101; i <= 112; i++) {
            Room.RoomType type = (i <= 108) ? Room.RoomType.STANDARD : Room.RoomType.DELUXE;
            double price = (type == Room.RoomType.STANDARD) ? 120 : 200;
            rooms.add(new Room(String.valueOf(i), type, 1, price));
        }
        // Floor 2: Deluxe & Suite
        for (int i = 201; i <= 212; i++) {
            Room.RoomType type = (i <= 206) ? Room.RoomType.DELUXE : Room.RoomType.SUITE;
            double price = (type == Room.RoomType.DELUXE) ? 200 : 350;
            rooms.add(new Room(String.valueOf(i), type, 2, price));
        }
        // Floor 3: Suite & Penthouse
        for (int i = 301; i <= 312; i++) {
            Room.RoomType type = (i <= 308) ? Room.RoomType.SUITE : Room.RoomType.PENTHOUSE;
            double price = (type == Room.RoomType.SUITE) ? 350 : 600;
            Room r = new Room(String.valueOf(i), type, 3, price);
            if (type == Room.RoomType.PENTHOUSE) r.setCapacity(4);
            rooms.add(r);
        }

        // Set some rooms to maintenance
        findRoom("111").setStatus(Room.RoomStatus.MAINTENANCE);
        findRoom("207").setStatus(Room.RoomStatus.MAINTENANCE);

        // === GUESTS (16 guests) ===
        guests.add(new Guest("Alice Johnson", "alice.j@email.com", "+1-555-0101", "USA", true, 5, 8400));
        guests.add(new Guest("Bob Smith", "bob.smith@email.com", "+1-555-0102", "USA", false, 2, 1200));
        guests.add(new Guest("Clara Davis", "clara.d@email.com", "+44-20-7946", "UK", true, 8, 22000));
        guests.add(new Guest("David Wilson", "d.wilson@email.com", "+1-555-0104", "Canada", false, 1, 600));
        guests.add(new Guest("Elena Frost", "elena.f@email.com", "+49-30-1234", "Germany", false, 3, 3150));
        guests.add(new Guest("Farid Al-Rashid", "farid@email.com", "+971-4-5678", "UAE", true, 12, 54000));
        guests.add(new Guest("Grace Kim", "grace.k@email.com", "+82-2-1234", "South Korea", false, 1, 350));
        guests.add(new Guest("Hugo Martinez", "hugo.m@email.com", "+34-91-5678", "Spain", false, 4, 4800));
        guests.add(new Guest("Isabella Rossi", "isabella@email.com", "+39-06-1234", "Italy", true, 6, 12600));
        guests.add(new Guest("James O'Brien", "james.ob@email.com", "+353-1-5678", "Ireland", false, 2, 1400));
        guests.add(new Guest("Keiko Tanaka", "keiko.t@email.com", "+81-3-1234", "Japan", true, 9, 31500));
        guests.add(new Guest("Lucas Petrov", "lucas.p@email.com", "+7-495-1234", "Russia", false, 1, 200));
        guests.add(new Guest("Maria Santos", "maria.s@email.com", "+55-11-5678", "Brazil", false, 3, 2250));
        guests.add(new Guest("Nathan Chen", "nathan.c@email.com", "+86-21-1234", "China", false, 2, 700));
        guests.add(new Guest("Olivia Thompson", "olivia.t@email.com", "+61-2-5678", "Australia", true, 7, 16800));
        guests.add(new Guest("Pierre Dubois", "pierre.d@email.com", "+33-1-1234", "France", false, 1, 350));

        // === BOOKINGS (active bookings) ===
        LocalDate today = LocalDate.now();

        // Currently checked-in guests
        Booking b1 = new Booking(guests.get(0), findRoom("102"), today.minusDays(2), today.plusDays(3));
        b1.setStatus(Booking.BookingStatus.CHECKED_IN);
        findRoom("102").setStatus(Room.RoomStatus.OCCUPIED);
        findRoom("102").setCurrentGuestName(guests.get(0).getFullName());
        bookings.add(b1);

        Booking b2 = new Booking(guests.get(2), findRoom("305"), today.minusDays(1), today.plusDays(4));
        b2.setStatus(Booking.BookingStatus.CHECKED_IN);
        findRoom("305").setStatus(Room.RoomStatus.OCCUPIED);
        findRoom("305").setCurrentGuestName(guests.get(2).getFullName());
        bookings.add(b2);

        Booking b3 = new Booking(guests.get(5), findRoom("310"), today.minusDays(3), today.plusDays(5));
        b3.setStatus(Booking.BookingStatus.CHECKED_IN);
        findRoom("310").setStatus(Room.RoomStatus.OCCUPIED);
        findRoom("310").setCurrentGuestName(guests.get(5).getFullName());
        bookings.add(b3);

        Booking b4 = new Booking(guests.get(8), findRoom("209"), today.minusDays(1), today.plusDays(2));
        b4.setStatus(Booking.BookingStatus.CHECKED_IN);
        findRoom("209").setStatus(Room.RoomStatus.OCCUPIED);
        findRoom("209").setCurrentGuestName(guests.get(8).getFullName());
        bookings.add(b4);

        Booking b5 = new Booking(guests.get(10), findRoom("312"), today.minusDays(4), today.plusDays(1));
        b5.setStatus(Booking.BookingStatus.CHECKED_IN);
        findRoom("312").setStatus(Room.RoomStatus.OCCUPIED);
        findRoom("312").setCurrentGuestName(guests.get(10).getFullName());
        bookings.add(b5);

        // Confirmed (future) bookings
        Booking b6 = new Booking(guests.get(1), findRoom("201"), today.plusDays(1), today.plusDays(4));
        b6.setStatus(Booking.BookingStatus.CONFIRMED);
        findRoom("201").setStatus(Room.RoomStatus.RESERVED);
        findRoom("201").setCurrentGuestName(guests.get(1).getFullName());
        bookings.add(b6);

        Booking b7 = new Booking(guests.get(3), findRoom("104"), today.plusDays(2), today.plusDays(5));
        b7.setStatus(Booking.BookingStatus.CONFIRMED);
        findRoom("104").setStatus(Room.RoomStatus.RESERVED);
        findRoom("104").setCurrentGuestName(guests.get(3).getFullName());
        bookings.add(b7);

        Booking b8 = new Booking(guests.get(7), findRoom("210"), today.plusDays(1), today.plusDays(6));
        b8.setStatus(Booking.BookingStatus.CONFIRMED);
        findRoom("210").setStatus(Room.RoomStatus.RESERVED);
        findRoom("210").setCurrentGuestName(guests.get(7).getFullName());
        bookings.add(b8);

        // Past (checked-out) bookings for revenue data
        Booking p1 = new Booking(guests.get(4), findRoom("103"), today.minusDays(7), today.minusDays(4));
        p1.setStatus(Booking.BookingStatus.CHECKED_OUT);
        bookings.add(p1);

        Booking p2 = new Booking(guests.get(6), findRoom("301"), today.minusDays(5), today.minusDays(3));
        p2.setStatus(Booking.BookingStatus.CHECKED_OUT);
        bookings.add(p2);

        Booking p3 = new Booking(guests.get(9), findRoom("205"), today.minusDays(10), today.minusDays(6));
        p3.setStatus(Booking.BookingStatus.CHECKED_OUT);
        bookings.add(p3);

        Booking p4 = new Booking(guests.get(11), findRoom("107"), today.minusDays(3), today.minusDays(1));
        p4.setStatus(Booking.BookingStatus.CHECKED_OUT);
        bookings.add(p4);

        Booking p5 = new Booking(guests.get(14), findRoom("309"), today.minusDays(8), today.minusDays(2));
        p5.setStatus(Booking.BookingStatus.CHECKED_OUT);
        bookings.add(p5);

        // One cancelled booking
        Booking c1 = new Booking(guests.get(13), findRoom("106"), today.plusDays(3), today.plusDays(6));
        c1.setStatus(Booking.BookingStatus.CANCELLED);
        bookings.add(c1);

        ActivityLog.getInstance().log("System initialized with " + rooms.size() + " rooms, " + guests.size() + " guests, " + bookings.size() + " bookings");
    }

    private Room findRoom(String number) {
        return rooms.stream()
            .filter(r -> r.getRoomNumber().equals(number))
            .findFirst()
            .orElse(null);
    }
}
