package cvut.fel.kbss.dto;

import cvut.fel.kbss.model.ArgumentType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewArgumentDto {
    private Long debateId;
    private String text;
    private ArgumentType type;
    private Long userId;
    private Long parentId;

    public NewArgumentDto(Long debateId, String text, ArgumentType type, Long userId, Long parentId) {
        this.debateId = debateId;
        this.text = text;
        this.type = type;
        this.userId = userId;
        this.parentId = parentId;
    }
}
