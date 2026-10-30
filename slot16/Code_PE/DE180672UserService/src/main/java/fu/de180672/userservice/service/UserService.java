package fu.de180672.userservice.service;

import fu.de180672.userservice.dto.ApiResponse;
import fu.de180672.userservice.dto.UserDTO;
import org.springframework.data.domain.Pageable;

public interface UserService {
    ApiResponse createUser(UserDTO dto);
    ApiResponse updateUser(Long userId, UserDTO dto);
    ApiResponse getUserById(Long userId);
    ApiResponse deactivateUser(Long userId);
    ApiResponse getUserList(String role, String status, Pageable pageable);
}
