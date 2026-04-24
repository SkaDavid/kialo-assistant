package cvut.fel.kbss.dto.request;


import cvut.fel.kbss.model.DebateVisibility;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewDebateDto {
    private String topic;
    private String thesis;
    private DebateVisibility visibility;

    public NewDebateDto(String topic, String thesis, DebateVisibility visibility) {
        this.topic = topic;
        this.thesis = thesis;
        this.visibility = visibility;
    }
}
