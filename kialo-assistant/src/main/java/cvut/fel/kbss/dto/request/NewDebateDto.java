package cvut.fel.kbss.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewDebateDto {
    private String topic;
    private String thesis;

    public NewDebateDto(String topic, String thesis) {
        this.topic = topic;
        this.thesis = thesis;
    }
}
