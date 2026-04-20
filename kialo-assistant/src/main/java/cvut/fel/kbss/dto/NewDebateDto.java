package cvut.fel.kbss.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewDebateDto {
    private String topic;
    private Long ownerId;
    private String thesis;

    public NewDebateDto(String topic, Long ownerId, String thesis) {
        this.topic = topic;
        this.ownerId = ownerId;
        this.thesis = thesis;
    }
}
