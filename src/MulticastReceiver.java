import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.List;
import java.util.Map;

public class MulticastReceiver implements Runnable {
    private static final int BUFFER_SIZE = 1024;

    private MulticastSocket multicastSocket;
    private Map<String, List<Reservation>> roomReservations;

    public MulticastReceiver(Map<String, List<Reservation>> roomReservations) {
        this.roomReservations = roomReservations;
        try {
            multicastSocket = new MulticastSocket(RoomReservationSystem.MULTICAST_PORT);
            multicastSocket.joinGroup(InetAddress.getByName(RoomReservationSystem.MULTICAST_ADDRESS));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
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

                // Process the received multicast message if required
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
