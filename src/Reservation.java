public class Reservation {
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
