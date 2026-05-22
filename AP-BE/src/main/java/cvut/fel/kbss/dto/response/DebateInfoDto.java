package cvut.fel.kbss.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DebateInfoDto {
    private boolean isPresent;
    private Long id;
    private List<ArgumentVersionDto> argumentVersions;
    private List<TermDefinitionDto> terms;
}
