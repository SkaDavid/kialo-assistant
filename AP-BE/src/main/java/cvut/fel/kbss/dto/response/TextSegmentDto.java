package cvut.fel.kbss.dto.response;

import cvut.fel.kbss.model.TextSegmentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TextSegmentDto {
    private TextSegmentType type;
    private String text;
    private String explanation;
}
