package cvut.fel.kbss.controller;

import cvut.fel.kbss.dto.Mapper;
import cvut.fel.kbss.dto.response.UserResponseDto;
import cvut.fel.kbss.exception.UserNotFoundException;
import cvut.fel.kbss.model.User;
import cvut.fel.kbss.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<UserResponseDto> getUser(@RequestParam String username) throws UserNotFoundException {
        UserResponseDto user = this.userService.findUser(username);
        if(user != null){
            return ResponseEntity.status(HttpStatus.OK).body(user);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}