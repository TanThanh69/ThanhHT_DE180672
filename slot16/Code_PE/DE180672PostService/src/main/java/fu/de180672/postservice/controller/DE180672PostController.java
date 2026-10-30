package fu.de180672.postservice.controller;

import fu.de180672.postservice.dto.ApiResponse;
import fu.de180672.postservice.dto.PostDTO;
import fu.de180672.postservice.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class DE180672PostController {

    private final PostService postService;

    public DE180672PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createPost(@Valid @RequestBody PostDTO dto) {
        ApiResponse response = postService.createPost(dto);
        return toResponse(response, HttpStatus.CREATED);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse> updatePost(@PathVariable("postId") Long postId,
                                                  @RequestBody PostDTO dto) {
        ApiResponse response = postService.updatePost(postId, dto);
        return toResponse(response, HttpStatus.OK);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse> getPostById(@PathVariable("postId") Long postId) {
        ApiResponse response = postService.getPostById(postId);
        return toResponse(response, HttpStatus.OK);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse> cancelPost(@PathVariable("postId") Long postId) {
        ApiResponse response = postService.cancelPost(postId);
        return toResponse(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getPostList(
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "advertiserName", required = false) String advertiserName,
            @RequestParam(value = "status", required = false) String status) {

        if (size == null || size > 100 || size < 1 || page == null || page < 0) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                    .body(ApiResponse.error(2, "Validation failed: page size must be between 1 and 100"));
        }

        ApiResponse response = postService.getPostList(advertiserName, status, PageRequest.of(page, size));
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
