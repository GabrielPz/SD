import java.io.*;
import java.net.*;
import java.util.*;

public class RoomReservationSystem {
    private static final int MULTICAST_PORT = 8888;
    private static final String MULTICAST_ADDRESS = "230.0.0.0";
    private static final int BUFFER_SIZE = 1024;

    private DatagramSocket socket;
    private MulticastSocket multicastSocket;
    private InetAddress multicastAddress;
    private Map<String, List<Reservation>> roomReservations;

    public RoomReservationSystem() {
        try {
            socket = new DatagramSocket();
            multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);
            multicastSocket = new MulticastSocket(MULTICAST_PORT);
            multicastSocket.joinGroup(multicastAddress);
            roomReservations = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Room Reservation System is running...");
        System.out.println("Press Ctrl+C to exit.");

        // Create a separate thread to handle incoming multicast messages
        Thread receiverThread = new Thread(() -> {
            while (true) {
                try {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    multicastSocket.receive(packet);

                    String message = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Received multicast message: " + message);

                    String[] parts = message.split(",");
                    String roomName = parts[0];
                    String action = parts[1];
                    String studentName = parts[2];
                    int startTime = Integer.parseInt(parts[3]);
                    int endTime = Integer.parseInt(parts[4]);

                    if (action.equals("reserve")) {
                        reserveRoom(roomName, studentName, startTime, endTime);
                    } else if (action.equals("cancel")) {
                        cancelReservation(roomName, studentName, startTime, endTime);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        receiverThread.start();

        // Read user input and perform room reservations or cancellations
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter 'reserve' to make a reservation or 'cancel' to cancel a reservation:");
            String action = scanner.nextLine();

            if (action.equals("reserve")) {
                System.out.println("Enter room name:");
                String roomName = scanner.nextLine();

                System.out.println("Enter your name:");
                String studentName = scanner.nextLine();

                System.out.println("Enter start time (0-23):");
                int startTime = scanner.nextInt();

                System.out.println("Enter end time (0-23):");
                int endTime = scanner.nextInt();

                scanner.nextLine(); // Consume newline

                reserveRoom(roomName, studentName, startTime, endTime);
            } else if (action.equals("cancel")) {
                System.out.println("Enter room name:");
                String roomName = scanner.nextLine();

                System.out.println("Enter your name:");
                String studentName = scanner.nextLine();

                System.out.println("Enter start time (0-23):");
                int startTime = scanner.nextInt();

                System.out.println("Enter end time (0-23):");
                int endTime = scanner.nextInt();

                scanner.nextLine(); // Consume newline

                cancelReservation(roomName, studentName, startTime, endTime);
            }
        }
    }

    private synchronized void reserveRoom(String roomName, String studentName, int startTime, int endTime) {
        List<Reservation> reservations = roomReservations.getOrDefault(roomName, new ArrayList<>());
        for (Reservation reservation : reservations) {
            if (isTimeConflict(reservation, startTime, endTime)) {
                System.out.println("Time conflict: Room already reserved for that time slot.");
                return;
            }
        }

        Reservation newReservation = new Reservation(roomName, studentName, startTime, endTime);
        reservations.add(newReservation);
        roomReservations.put(roomName, reservations);

        String multicastMessage = roomName + ",reserve," + studentName + "," + startTime + "," + endTime;
        sendMulticastMessage(multicastMessage);
        System.out.println("Reservation successful: Room " + roomName + " reserved by " + studentName);
    }

    private synchronized void cancelReservation(String roomName, String studentName, int startTime, int endTime) {
        List<Reservation> reservations = roomReservations.getOrDefault(roomName, new ArrayList<>());
        Reservation canceledReservation = null;

        for (Reservation reservation : reservations) {
            if (reservation.getStudentName().equals(studentName) && reservation.getStartTime() == startTime
                    && reservation.getEndTime() == endTime) {
                canceledReservation = reservation;
                break;
            }
        }

        if (canceledReservation != null) {
            reservations.remove(canceledReservation);
            roomReservations.put(roomName, reservations);

            String multicastMessage = roomName + ",cancel," + studentName + "," + startTime + "," + endTime;
            sendMulticastMessage(multicastMessage);
            System.out.println("Reservation canceled: Room " + roomName + " released by " + studentName);
        } else {
            System.out.println("No matching reservation found.");
        }
    }

    private void sendMulticastMessage(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastAddress, MULTICAST_PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isTimeConflict(Reservation reservation, int startTime, int endTime) {
        return (startTime >= reservation.getStartTime() && startTime < reservation.getEndTime())
                || (endTime > reservation.getStartTime() && endTime <= reservation.getEndTime())
                || (startTime <= reservation.getStartTime() && endTime >= reservation.getEndTime());
    }

    private static class Reservation {
        private String roomName;
        private String studentName;
        private int startTime;
        private int endTime;

        public Reservation(String roomName, String studentName, int startTime, int endTime) {
            this.roomName = roomName;
            this.studentName = studentName;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getRoomName() {
            return roomName;
        }

        public String getStudentName() {
            return studentName;
        }

        public int getStartTime() {
            return startTime;
        }

        public int getEndTime() {
            return endTime;
        }
    }

    public static void main(String[] args) {
        RoomReservationSystem reservationSystem = new RoomReservationSystem();
        reservationSystem.start();
    }
}
