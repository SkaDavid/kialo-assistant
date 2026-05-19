package cvut.fel.kbss.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FallacyResponseDto {
    private String label;
    private float score;
    private String explanation;
    private boolean isFallacy;
}
