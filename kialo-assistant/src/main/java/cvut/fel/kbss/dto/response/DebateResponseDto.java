package cvut.fel.kbss.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DebateResponseDto {
    private Long id;
    private String title;
    private UserResponseDto owner;
    private List<ArgumentResponseDto> arguments;
}
