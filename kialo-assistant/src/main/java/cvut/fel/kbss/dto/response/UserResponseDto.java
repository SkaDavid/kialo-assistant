package cvut.fel.kbss.dto.response;

import lombok.Setter;

import java.util.List;

@Setter
public class UserResponseDto {
    private Long id;
    private String username;
    private List<Long> debates;
}
