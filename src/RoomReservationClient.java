import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class RoomReservationClient {
    private DatagramSocket socket;
    private InetAddress multicastAddress;

    public RoomReservationClient() {
        try {
            socket = new DatagramSocket();
            multicastAddress = InetAddress.getByName(RoomReservationSystem.MULTICAST_ADDRESS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMulticastMessage(String message) {
        try {
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastAddress,
                    RoomReservationSystem.MULTICAST_PORT);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
