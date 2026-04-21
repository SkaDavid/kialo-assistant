package cvut.fel.kbss.dto;

import cvut.fel.kbss.dto.response.ArgumentResponseDto;
import cvut.fel.kbss.dto.response.DebateResponseDto;
import cvut.fel.kbss.dto.response.UserResponseDto;
import cvut.fel.kbss.model.Argument;
import cvut.fel.kbss.model.Debate;
import cvut.fel.kbss.model.User;
import org.springframework.stereotype.Component;

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
        return response;
    }

    public DebateResponseDto toDto(Debate debate){
        DebateResponseDto response = new DebateResponseDto();
        response.setId(debate.getId());
        response.setTitle(debate.getTitle());
        response.setOwner(this.toDto(debate.getOwner()));
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
}
