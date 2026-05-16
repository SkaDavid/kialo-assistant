package cvut.fel.kbss.dto.response;

import cvut.fel.kbss.model.FallacyCheck;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ArgumentResponseDto {
    private Long id;
    private String text;
    private List<TextSegmentDto> structuredText;
    private String type;
    private Long parent;
    private UserResponseDto owner;
    private Long debate;
    private Integer version;
    private FallacyCheck fallacyCheck;
}
