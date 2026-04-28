package cvut.fel.kbss.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FallacyResponseDto {
    @JsonProperty("fallacy")
    private String label;

    @JsonProperty("confidence")
    private float score;

    public FallacyResponseDto(String text, float score){
        this.label = text;
        this.score = score;
    }
}
