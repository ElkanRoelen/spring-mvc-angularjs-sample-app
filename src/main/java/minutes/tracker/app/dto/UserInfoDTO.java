package minutes.tracker.app.dto;

/**
 *
 * JSON-serializable DTO containing user data
 *
 */
public class UserInfoDTO {

    private String userName;
    private Long maxMinutesPerDay;
    private Long todaysMinutes;

    public UserInfoDTO(String userName, Long maxMinutesPerDay, Long todaysMinutes) {
        this.userName = userName;
        this.maxMinutesPerDay = maxMinutesPerDay;
        this.todaysMinutes = todaysMinutes;
    }

    public Long getMaxMinutesPerDay() {
        return maxMinutesPerDay;
    }

    public void setMaxMinutesPerDay(Long maxMinutesPerDay) {
        this.maxMinutesPerDay = maxMinutesPerDay;
    }

    public Long getTodaysMinutes() {
        return todaysMinutes;
    }

    public void setTodaysMinutes(Long todaysMinutes) {
        this.todaysMinutes = todaysMinutes;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
