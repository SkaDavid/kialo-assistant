package cvut.fel.kbss.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AIDebateResponse {
    private String topic;
    private String thesis;
    private List<ArgumentResponseDto> arguments = new ArrayList<>();
    private UserResponseDto owner;
    private Long debateId;
}
