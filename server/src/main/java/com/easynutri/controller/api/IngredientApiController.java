package com.easynutri.controller.api;

import com.easynutri.dto.IngredientCandidateDto;
import com.easynutri.dto.IngredientMatchRequest;
import com.easynutri.dto.RecipeMatchResponseDto;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import com.easynutri.service.BarcodeLookupService;
import com.easynutri.service.IngredientService;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ingredients")
public class IngredientApiController {

  private final IngredientService ingredientService;
  private final BarcodeLookupService barcodeLookupService;
  private final UserRepository userRepository;

  public IngredientApiController(
      IngredientService ingredientService,
      BarcodeLookupService barcodeLookupService,
      UserRepository userRepository) {
    this.ingredientService = ingredientService;
    this.barcodeLookupService = barcodeLookupService;
    this.userRepository = userRepository;
  }

  @PostMapping("/resolve")
  public ResponseEntity<List<IngredientCandidateDto>> resolveIngredients(
      @RequestBody Map<String, String> payload) {
    String text = payload.getOrDefault("text", payload.getOrDefault("input", ""));
    List<IngredientCandidateDto> candidates = ingredientService.resolveIngredients(text);
    return ResponseEntity.ok(candidates);
  }

  @PostMapping("/voice")
  public ResponseEntity<List<IngredientCandidateDto>> resolveVoice(
      @RequestBody Map<String, String> payload) {
    String transcript = payload.getOrDefault("transcript", payload.getOrDefault("text", ""));
    List<IngredientCandidateDto> candidates = ingredientService.resolveIngredients(transcript);
    return ResponseEntity.ok(candidates);
  }

  @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<IngredientCandidateDto>> uploadPhoto(
      @RequestParam("file") MultipartFile file) {
    try {
      byte[] bytes = file.getBytes();
      String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
      List<IngredientCandidateDto> candidates = ingredientService.processImage(bytes, contentType);
      return ResponseEntity.ok(candidates);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/barcode")
  public ResponseEntity<?> lookupBarcode(@RequestBody Map<String, String> payload) {
    String barcode = payload.getOrDefault("barcode", "");
    return barcodeLookupService
        .lookupBarcode(barcode)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/ocr")
  public ResponseEntity<List<IngredientCandidateDto>> processOcr(
      @RequestBody Map<String, String> payload) {
    String text = payload.getOrDefault("text", payload.getOrDefault("ocrText", ""));
    List<IngredientCandidateDto> candidates = ingredientService.processOcr(text);
    return ResponseEntity.ok(candidates);
  }

  @PostMapping("/match")
  public ResponseEntity<List<RecipeMatchResponseDto>> matchRecipes(
      @RequestBody IngredientMatchRequest request, Principal principal) {
    Long userId = getUserId(principal);
    List<RecipeMatchResponseDto> matches = ingredientService.findMatchingRecipes(userId, request);
    return ResponseEntity.ok(matches);
  }

  private Long getUserId(Principal principal) {
    if (principal == null) return null;
    return userRepository.findByUsername(principal.getName()).map(UserEntity::getId).orElse(null);
  }
}
