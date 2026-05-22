package cvut.fel.kbss.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FallacyRequestDto {
    private String text;
    private Long argumentId;

    public FallacyRequestDto(String text){
        this.text = text;
    }
}
