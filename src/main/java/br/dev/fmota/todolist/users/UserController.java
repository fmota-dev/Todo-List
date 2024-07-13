package br.dev.fmota.todolist.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.favre.lib.crypto.bcrypt.BCrypt;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody User user) {

        User userFound = this.userRepository.findByUsername(user.getUsername());

        if (userFound != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuário já cadastrado");
        }

        String passwordHashed = BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray());
        user.setPassword(passwordHashed);

        User userCreated = this.userRepository.save(user);
        String message = String.format(
                "Usuário %s (ID: %s) cadastrado com sucesso", userCreated.getUsername(), userCreated.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }
}
