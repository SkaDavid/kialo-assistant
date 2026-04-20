package cvut.fel.kbss.controller;

import cvut.fel.kbss.model.User;
import cvut.fel.kbss.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<User> getUser(@RequestParam String username){
        return ResponseEntity.ok(this.userService.findUser(username));
    }
}