package cvut.fel.kbss.dto.request;

import cvut.fel.kbss.model.DebateVisibility;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDebateDto {
    private String topic;
    private DebateVisibility visibility;
}
