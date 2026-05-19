package cvut.fel.kbss.dto.request;

import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AIArgumentRequestDto {
    private String type;
    private String text;
    private List<ArgumentResponseDto> debate;
}