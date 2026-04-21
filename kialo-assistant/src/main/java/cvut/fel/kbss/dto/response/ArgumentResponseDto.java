package cvut.fel.kbss.dto.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ArgumentResponseDto {
    private Long id;
    private String text;
    private String type;
    private Long parent;
    private UserResponseDto owner;
    private Long debate;
}
