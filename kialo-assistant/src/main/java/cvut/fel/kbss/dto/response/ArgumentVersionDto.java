package cvut.fel.kbss.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArgumentVersionDto {
    private Long id;
    private Long kialoId;
    private Integer version;
}
