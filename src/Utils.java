public class Utils {
    public static boolean isTimeConflict(Reservation reservation, int startTime, int endTime) {
        return (startTime >= reservation.getStartTime() && startTime < reservation.getEndTime())
                || (endTime > reservation.getStartTime() && endTime <= reservation.getEndTime())
                || (startTime <= reservation.getStartTime() && endTime >= reservation.getEndTime());
    }
}
