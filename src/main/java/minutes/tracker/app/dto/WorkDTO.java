package minutes.tracker.app.dto;


import minutes.tracker.app.dto.serialization.CustomTimeDeserializer;
import minutes.tracker.app.dto.serialization.CustomTimeSerializer;
import minutes.tracker.app.model.Work;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * JSON serializable DTO containing Work data
 *
 */
public class WorkDTO {

    private Long id;

    @JsonFormat(pattern = "yyyy/MM/dd", timezone = "CET")
    private Date date;

    @JsonSerialize(using = CustomTimeSerializer.class)
    @JsonDeserialize(using = CustomTimeDeserializer.class)
    private Time time;

    private String description;
    private Long minutes;

    public WorkDTO() {
    }

    public WorkDTO(Long id, Date date, Time time, String description, Long minutes) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.description = description;
        this.minutes = minutes;
    }

    public static WorkDTO mapFromWorkEntity(Work work) {
        return new WorkDTO(work.getId(), work.getDate(), work.getTime(),
                work.getDescription(), work.getMinutes());
    }

    public static List<WorkDTO> mapFromWorksEntities(List<Work> works) {
        return works.stream().map((work) -> mapFromWorkEntity(work)).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getMinutes() {
        return minutes;
    }

    public void setMinutes(Long minutes) {
        this.minutes = minutes;
    }

}
