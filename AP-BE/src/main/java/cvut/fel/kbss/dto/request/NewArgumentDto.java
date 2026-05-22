package cvut.fel.kbss.dto.request;

import cvut.fel.kbss.model.ArgumentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewArgumentDto {
    private Long debateId;
    private String text;
    private ArgumentType type;
    private Long parentId;
    private Long kialoId;
    private Integer version;
}
