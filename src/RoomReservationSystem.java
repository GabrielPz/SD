import java.util.*;

public class RoomReservationSystem {
    static final int MULTICAST_PORT = 8888;
    static final String MULTICAST_ADDRESS = "230.0.0.0";

    private Map<String, List<Reservation>> roomReservations;

    public RoomReservationSystem() {
        roomReservations = new HashMap<>();
    }

    public void start() {
        System.out.println("Room Reservation System is running...");
        System.out.println("Press Ctrl+C to exit.");

        MulticastReceiver receiver = new MulticastReceiver(roomReservations);
        Thread receiverThread = new Thread(receiver);
        receiverThread.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter 'reserve' to make a reservation or 'cancel' to cancel a reservation:");
            String action = scanner.nextLine();

            if (action.equals("reserve")) {
                handleReservation(scanner);
            } else if (action.equals("cancel")) {
                handleCancellation(scanner);
            }
        }
    }

    private void handleReservation(Scanner scanner) {
        System.out.println("Enter room name:");
        String roomName = scanner.nextLine();

        System.out.println("Enter your name:");
        String studentName = scanner.nextLine();

        System.out.println("Enter start time (0-23):");
        int startTime = scanner.nextInt();

        System.out.println("Enter end time (0-23):");
        int endTime = scanner.nextInt();

        scanner.nextLine(); // Consume newline

        Reservation newReservation = new Reservation(roomName, studentName, startTime, endTime);

        if (isTimeConflict(newReservation)) {
            System.out.println("Time conflict: Room already reserved for that time slot.");
        } else {
            List<Reservation> reservations = roomReservations.getOrDefault(roomName, new ArrayList<>());
            reservations.add(newReservation);
            roomReservations.put(roomName, reservations);

            String multicastMessage = roomName + ",reserve," + studentName + "," + startTime + "," + endTime;
            RoomReservationClient client = new RoomReservationClient();
            client.sendMulticastMessage(multicastMessage);

            System.out.println("Reservation successful: Room " + roomName + " reserved by " + studentName);
        }
    }

    private void handleCancellation(Scanner scanner) {
        System.out.println("Enter room name:");
        String roomName = scanner.nextLine();

        System.out.println("Enter your name:");
        String studentName = scanner.nextLine();

        System.out.println("Enter start time (0-23):");
        int startTime = scanner.nextInt();

        System.out.println("Enter end time (0-23):");
        int endTime = scanner.nextInt();

        scanner.nextLine(); // Consume newline

        Reservation reservationToRemove = null;
        List<Reservation> reservations = roomReservations.getOrDefault(roomName, new ArrayList<>());

        for (Reservation reservation : reservations) {
            if (reservation.getRoomName().equals(roomName)
                    && reservation.getStudentName().equals(studentName)
                    && reservation.getStartTime() == startTime
                    && reservation.getEndTime() == endTime) {
                reservationToRemove = reservation;
                break;
            }
        }

        if (reservationToRemove != null) {
            reservations.remove(reservationToRemove);
            roomReservations.put(roomName, reservations);

            String multicastMessage = roomName + ",cancel," + studentName + "," + startTime + "," + endTime;
            RoomReservationClient client = new RoomReservationClient();
            client.sendMulticastMessage(multicastMessage);

            System.out.println("Reservation canceled: Room " + roomName + " released by " + studentName);
        } else {
            System.out.println("No matching reservation found.");
        }
    }

    private boolean isTimeConflict(Reservation newReservation) {
        List<Reservation> reservations = roomReservations.getOrDefault(newReservation.getRoomName(), new ArrayList<>());

        for (Reservation reservation : reservations) {
            if (Utils.isTimeConflict(reservation, newReservation.getStartTime(), newReservation.getEndTime())) {
                return true;
            }
        }

        return false;
    }

    public static void main(String[] args) {
        RoomReservationSystem reservationSystem = new RoomReservationSystem();
        reservationSystem.start();
    }
}
