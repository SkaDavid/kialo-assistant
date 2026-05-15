package cvut.fel.kbss.dto;

import cvut.fel.kbss.dto.response.*;
import cvut.fel.kbss.model.Argument;
import cvut.fel.kbss.model.Debate;
import cvut.fel.kbss.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Mapper {

    public ArgumentResponseDto toDto(Argument argument){
        ArgumentResponseDto response = new ArgumentResponseDto();
        response.setId(argument.getId());
        response.setText(argument.getText());
        response.setType(argument.getType() != null ? argument.getType().toString() : null);
        response.setParent(argument.getParent() != null ? argument.getParent().getId() : null);
        response.setOwner(argument.getOwner() != null ? this.toDto(argument.getOwner()) : null);
        response.setDebate(argument.getDebate() != null ? argument.getDebate().getId() : null);
        if(argument.getSegments() != null){
            response.setStructuredText(argument.getSegments().stream()
                    .map(segment -> new TextSegmentDto(segment.getType(), segment.getContent(), segment.getExplanation()))
                    .collect(Collectors.toList())
            );
        }
        return response;
    }

    public DebateResponseDto toDto(Debate debate){
        DebateResponseDto response = new DebateResponseDto();
        response.setId(debate.getId());
        response.setTitle(debate.getTopic());
        response.setOwner(this.toDto(debate.getOwner()));
        response.setVisibility(debate.getVisibility());
        response.setArguments(debate.getArguments().stream()
                .map((argument) -> this.toDto(argument))
                .collect(Collectors.toList())
        );
        return response;
    }

    public UserResponseDto toDto(User user){
        UserResponseDto response = new UserResponseDto();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setDebates(user.getDebates() != null ?user.getDebates().stream()
                .map((debate) -> debate.getId())
                .collect(Collectors.toList()) : null
        );
        return response;
    }

    public DebateInfoDto toDebateInfoDto(Debate debate){
        DebateInfoDto dto = new DebateInfoDto();
        dto.setPresent(true);
        dto.setId(debate.getId());
        List<ArgumentVersionDto> versions = new ArrayList<>();
        for(Argument argument : debate.getArguments()){
            versions.add(this.toVersionDto(argument));
        }
        dto.setArgumentVersions(versions);
        return dto;
    }

    public ArgumentVersionDto toVersionDto(Argument argument){
        ArgumentVersionDto dto = new ArgumentVersionDto();
        dto.setVersion(argument.getKialoVersion());
        dto.setKialoId(argument.getKialoId());
        return dto;
    }
}
