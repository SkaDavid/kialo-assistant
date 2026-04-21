package cvut.fel.kbss.dto.response;

import lombok.Setter;

@Setter
public class ArgumentResponseDto {
    private Long id;
    private String text;
    private String type;
    private Long parent;
    private UserResponseDto owner;
    private Long debate;
}
