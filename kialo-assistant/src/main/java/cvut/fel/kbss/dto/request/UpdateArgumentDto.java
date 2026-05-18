package cvut.fel.kbss.dto.request;

import cvut.fel.kbss.model.ArgumentType;
import cvut.fel.kbss.model.DebateVisibility;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateArgumentDto {
    private String text;
    private ArgumentType type;
    private Integer kialoVersion;
}
