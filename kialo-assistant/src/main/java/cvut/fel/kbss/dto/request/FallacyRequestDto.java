package cvut.fel.kbss.dto.request;

import cvut.fel.kbss.dto.response.FallacyResponseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FallacyRequestDto {
    private String text;

    public FallacyRequestDto(String text){
        this.text = text;
    }
}
