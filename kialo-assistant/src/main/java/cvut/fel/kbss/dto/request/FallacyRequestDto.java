package cvut.fel.kbss.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class FallacyRequestDto {
    private String text;
    private Long argumentId;
}
