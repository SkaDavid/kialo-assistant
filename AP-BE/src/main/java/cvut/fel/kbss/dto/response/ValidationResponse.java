package cvut.fel.kbss.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationResponse {
    private boolean isFallacy;
    private String explanation;
}
