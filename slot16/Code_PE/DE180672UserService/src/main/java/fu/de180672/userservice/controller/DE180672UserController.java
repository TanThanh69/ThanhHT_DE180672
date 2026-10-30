package fu.de180672.userservice.controller;

import fu.de180672.userservice.dto.ApiResponse;
import fu.de180672.userservice.dto.UserDTO;
import fu.de180672.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class DE180672UserController {

    private final UserService userService;

    public DE180672UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@Valid @RequestBody UserDTO dto) {
        ApiResponse response = userService.createUser(dto);
        return toResponse(response, HttpStatus.CREATED);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable("userId") Long userId,
                                                  @RequestBody UserDTO dto) {
        ApiResponse response = userService.updateUser(userId, dto);
        return toResponse(response, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable("userId") Long userId) {
        ApiResponse response = userService.getUserById(userId);
        return toResponse(response, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deactivateUser(@PathVariable("userId") Long userId) {
        ApiResponse response = userService.deactivateUser(userId);
        return toResponse(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getUserList(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status) {

        if (size == null || size > 100 || size < 1 || page == null || page < 0) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(ApiResponse.error(2, "Validation failed: page size must be between 1 and 100"));
        }

        ApiResponse response = userService.getUserList(role, status, PageRequest.of(page, size));
        return toResponse(response, HttpStatus.OK);
    }

    private ResponseEntity<ApiResponse> toResponse(ApiResponse res, HttpStatus successStatus) {
        HttpStatus status = switch (res.getStatus()) {
            case 1 -> successStatus;
            case 2 -> HttpStatus.NOT_ACCEPTABLE;      // 406
            case 3 -> HttpStatus.IM_USED;             // 226
            case 4 -> HttpStatus.NOT_FOUND;           // 404
            case 5 -> HttpStatus.BAD_REQUEST;         // 400
            default -> HttpStatus.INTERNAL_SERVER_ERROR; // 500
        };
        return ResponseEntity.status(status).body(res);
    }
}
