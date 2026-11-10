package com.easynutri.controller.api;

import com.easynutri.dto.CommunityCommentDto;
import com.easynutri.dto.CommunityRecipeDto;
import com.easynutri.dto.CreateCommunityRecipeRequest;
import com.easynutri.dto.CreatorProfileDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/community")
@Tag(
    name = "Community Recipes",
    description = "Endpoints for community recipe sharing, engagement, and profiles")
public class CommunityApiController {

  private final CommunityService communityService;
  private final UserRepository userRepository;

  public CommunityApiController(CommunityService communityService, UserRepository userRepository) {
    this.communityService = communityService;
    this.userRepository = userRepository;
  }

  @GetMapping("/feed")
  @Operation(summary = "Get community recipe feed filtered by latest, popular, or following")
  public ResponseEntity<List<CommunityRecipeDto>> getFeed(
      @RequestParam(value = "feed", defaultValue = "latest") String feed,
      @RequestParam(value = "q", required = false) String query,
      Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(communityService.getFeed(userId, feed, query));
  }

  @GetMapping("/recipes/{id}")
  @Operation(summary = "Get recipe detail by ID")
  public ResponseEntity<CommunityRecipeDto> getRecipe(
      @PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    return communityService
        .getRecipeById(id, userId)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/recipes")
  @Operation(summary = "Publish a community recipe with verified ingredient nutrition")
  public ResponseEntity<CommunityRecipeDto> publishRecipe(
      @RequestBody CreateCommunityRecipeRequest request, Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(communityService.publishRecipe(userId, request));
  }

  @PostMapping("/recipes/{id}/like")
  @Operation(summary = "Toggle like on a community recipe")
  public ResponseEntity<Map<String, Object>> toggleLike(
      @PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    boolean liked = communityService.toggleLike(userId, id);
    return ResponseEntity.ok(Map.of("recipeId", id, "liked", liked));
  }

  @PostMapping("/recipes/{id}/comments")
  @Operation(summary = "Add a comment to a recipe with automated clinical screening")
  public ResponseEntity<CommunityCommentDto> addComment(
      @PathVariable("id") Long id, @RequestBody Map<String, String> body, Principal principal) {
    Long userId = getUserId(principal);
    String content = body.getOrDefault("content", "");
    return ResponseEntity.ok(communityService.addComment(userId, id, content));
  }

  @DeleteMapping("/comments/{id}")
  @Operation(summary = "Delete own comment (or admin delete)")
  public ResponseEntity<Map<String, Object>> deleteComment(
      @PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    communityService.deleteComment(userId, id);
    return ResponseEntity.ok(Map.of("commentId", id, "deleted", true));
  }

  @PostMapping("/users/{id}/follow")
  @Operation(summary = "Toggle follow on a recipe creator")
  public ResponseEntity<Map<String, Object>> toggleFollow(
      @PathVariable("id") Long id, Principal principal) {
    Long userId = getUserId(principal);
    boolean following = communityService.toggleFollow(userId, id);
    return ResponseEntity.ok(Map.of("creatorId", id, "following", following));
  }

  @GetMapping("/users/{username}")
  @Operation(summary = "Get creator profile and published recipes")
  public ResponseEntity<CreatorProfileDto> getProfile(
      @PathVariable("username") String username, Principal principal) {
    Long userId = getUserId(principal);
    return ResponseEntity.ok(communityService.getCreatorProfile(username, userId));
  }

  private Long getUserId(Principal principal) {
    String username = principal != null ? principal.getName() : "demo";
    return userRepository.findByUsername(username).map(UserEntity::getId).orElse(1L);
  }
}
