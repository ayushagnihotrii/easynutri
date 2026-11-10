package com.easynutri.service;

import com.easynutri.domain.moderation.ContentScreener;
import com.easynutri.domain.moderation.ScreeningResult;
import com.easynutri.domain.moderation.ScreeningVerdict;
import com.easynutri.domain.recipe.DietaryTagClassifier;
import com.easynutri.dto.CommunityCommentDto;
import com.easynutri.dto.CommunityRecipeDto;
import com.easynutri.dto.CreateCommunityRecipeRequest;
import com.easynutri.dto.CreatorProfileDto;
import com.easynutri.dto.RecipeIngredientInputDto;
import com.easynutri.entity.*;
import com.easynutri.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommunityService {

  private final CommunityRecipeRepository recipeRepository;
  private final CommunityRecipeIngredientRepository ingredientRepository;
  private final CommunityLikeRepository likeRepository;
  private final CommunityCommentRepository commentRepository;
  private final CommunityFollowRepository followRepository;
  private final FoodRepository foodRepository;
  private final UserRepository userRepository;
  private final SavedRecipeRepository savedRecipeRepository;

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault());

  public CommunityService(
      CommunityRecipeRepository recipeRepository,
      CommunityRecipeIngredientRepository ingredientRepository,
      CommunityLikeRepository likeRepository,
      CommunityCommentRepository commentRepository,
      CommunityFollowRepository followRepository,
      FoodRepository foodRepository,
      UserRepository userRepository,
      SavedRecipeRepository savedRecipeRepository) {
    this.recipeRepository = recipeRepository;
    this.ingredientRepository = ingredientRepository;
    this.likeRepository = likeRepository;
    this.commentRepository = commentRepository;
    this.followRepository = followRepository;
    this.foodRepository = foodRepository;
    this.userRepository = userRepository;
    this.savedRecipeRepository = savedRecipeRepository;
  }

  @Transactional(readOnly = true)
  public List<CommunityRecipeDto> getFeed(Long currentUserId, String filter, String query) {
    String cleanFilter = filter != null ? filter.toUpperCase(Locale.ROOT) : "LATEST";
    List<CommunityRecipeEntity> entities;

    if (query != null && !query.trim().isEmpty()) {
      entities = recipeRepository.searchApprovedRecipes("APPROVED", query.trim());
    } else {
      entities =
          switch (cleanFilter) {
            case "POPULAR" ->
                recipeRepository.findByStatusOrderByLikeCountDescCreatedAtDesc("APPROVED");
            case "FOLLOWING" ->
                currentUserId != null
                    ? recipeRepository.findFollowingFeed(currentUserId)
                    : recipeRepository.findByStatusOrderByCreatedAtDesc("APPROVED");
            default -> recipeRepository.findByStatusOrderByCreatedAtDesc("APPROVED");
          };
    }

    return entities.stream().map(e -> mapToDto(e, currentUserId)).toList();
  }

  @Transactional(readOnly = true)
  public Optional<CommunityRecipeDto> getRecipeById(Long recipeId, Long currentUserId) {
    return recipeRepository.findById(recipeId).map(e -> mapToDto(e, currentUserId));
  }

  public CommunityRecipeDto publishRecipe(Long authorId, CreateCommunityRecipeRequest request) {
    UserEntity author =
        userRepository
            .findById(authorId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + authorId));

    // Automated clinical content screening
    ScreeningResult screenResult =
        ContentScreener.screen(
            request.title(), request.description() + " " + request.instructionsText());

    if (screenResult.verdict() == ScreeningVerdict.BLOCK) {
      throw new IllegalArgumentException(
          "Recipe blocked by safety screening: " + String.join("; ", screenResult.reasons()));
    }

    String initialStatus =
        screenResult.verdict() == ScreeningVerdict.REVIEW_REQUIRED ? "PENDING_REVIEW" : "APPROVED";

    int servings = Math.max(1, request.baseServings());
    double totalCal = 0.0;
    double totalPro = 0.0;
    double totalCarb = 0.0;
    double totalFat = 0.0;
    double totalFib = 0.0;
    double totalSod = 0.0;
    double totalIron = 0.0;
    double totalCalc = 0.0;

    List<DietaryTagClassifier.IngredientDetail> classifierDetails = new ArrayList<>();
    List<CommunityRecipeIngredientEntity> ingredients = new ArrayList<>();

    for (RecipeIngredientInputDto in : request.ingredients()) {
      FoodEntity food = null;
      if (in.foodId() != null && !in.foodId().isBlank()) {
        food = foodRepository.findById(in.foodId()).orElse(null);
      }
      if (food == null && in.ingredientName() != null && !in.ingredientName().isBlank()) {
        List<FoodEntity> matches = foodRepository.searchFoods(in.ingredientName().trim());
        if (!matches.isEmpty()) {
          food = matches.get(0);
        }
      }

      double amount = Math.max(1.0, in.amountG());
      boolean verified = food != null;
      String ingName = food != null ? food.getNameEn() : in.ingredientName();

      if (food != null) {
        totalCal += (food.getEnergyKcal() * amount) / 100.0;
        totalPro += (food.getProteinG() * amount) / 100.0;
        totalCarb += (food.getCarbohydrateG() * amount) / 100.0;
        totalFat += (food.getFatG() * amount) / 100.0;
        totalFib += (food.getFibreG() * amount) / 100.0;
        totalSod += (food.getSodiumMg() * amount) / 100.0;
        totalIron += (food.getIronMg() * amount) / 100.0;
        totalCalc += (food.getCalciumMg() * amount) / 100.0;

        List<String> allergens =
            food.getAllergenTags() != null
                ? Arrays.stream(food.getAllergenTags().split(",")).map(String::trim).toList()
                : List.of();
        List<String> diets =
            food.getDietFlags() != null
                ? Arrays.stream(food.getDietFlags().split(",")).map(String::trim).toList()
                : List.of();

        classifierDetails.add(
            new DietaryTagClassifier.IngredientDetail(
                food.getId(), food.getNameEn(), food.getCategory(), allergens, diets));
      } else {
        classifierDetails.add(
            new DietaryTagClassifier.IngredientDetail(
                in.ingredientName(), in.ingredientName(), "Uncategorized", List.of(), List.of()));
      }

      CommunityRecipeIngredientEntity ingEntity =
          new CommunityRecipeIngredientEntity(
              null, food, ingName, amount, in.unit() != null ? in.unit() : "g", verified);
      ingredients.add(ingEntity);
    }

    double calPerServing = round(totalCal / servings);
    double proPerServing = round(totalPro / servings);
    double carbPerServing = round(totalCarb / servings);
    double fatPerServing = round(totalFat / servings);
    double fibPerServing = round(totalFib / servings);
    double sodPerServing = round(totalSod / servings);
    double ironPerServing = round(totalIron / servings);
    double calcPerServing = round(totalCalc / servings);

    DietaryTagClassifier.RecipeNutritionSnapshot snapshot =
        new DietaryTagClassifier.RecipeNutritionSnapshot(
            calPerServing,
            proPerServing,
            carbPerServing,
            fatPerServing,
            fibPerServing,
            sodPerServing,
            ironPerServing,
            calcPerServing);

    List<String> tags = DietaryTagClassifier.classifyTags(classifierDetails, snapshot);
    String tagsStr = String.join(",", tags);

    CommunityRecipeEntity recipe =
        new CommunityRecipeEntity(
            author,
            request.title(),
            request.description(),
            request.coverImageUrl(),
            servings,
            request.prepMinutes() > 0 ? request.prepMinutes() : 15,
            request.cookMinutes() > 0 ? request.cookMinutes() : 20,
            request.mealSlot() != null ? request.mealSlot() : "LUNCH",
            tagsStr,
            request.instructionsText(),
            calPerServing,
            proPerServing,
            carbPerServing,
            fatPerServing,
            fibPerServing,
            sodPerServing,
            ironPerServing,
            calcPerServing,
            initialStatus);

    for (CommunityRecipeIngredientEntity ing : ingredients) {
      recipe.addIngredient(ing);
    }

    CommunityRecipeEntity saved = recipeRepository.save(recipe);
    return mapToDto(saved, authorId);
  }

  public boolean toggleLike(Long userId, Long recipeId) {
    UserEntity user = userRepository.findById(userId).orElseThrow();
    CommunityRecipeEntity recipe = recipeRepository.findById(recipeId).orElseThrow();

    Optional<CommunityLikeEntity> existing =
        likeRepository.findByUserIdAndCommunityRecipeId(userId, recipeId);

    if (existing.isPresent()) {
      likeRepository.delete(existing.get());
      recipe.setLikeCount(Math.max(0, recipe.getLikeCount() - 1));
      recipeRepository.save(recipe);
      return false;
    } else {
      likeRepository.save(new CommunityLikeEntity(user, recipe));
      recipe.setLikeCount(recipe.getLikeCount() + 1);
      recipeRepository.save(recipe);
      return true;
    }
  }

  public CommunityCommentDto addComment(Long authorId, Long recipeId, String content) {
    if (content == null || content.trim().isEmpty()) {
      throw new IllegalArgumentException("Comment content cannot be empty");
    }

    UserEntity author = userRepository.findById(authorId).orElseThrow();
    CommunityRecipeEntity recipe = recipeRepository.findById(recipeId).orElseThrow();

    ScreeningResult screenResult = ContentScreener.screen("Comment", content);
    if (screenResult.verdict() == ScreeningVerdict.BLOCK) {
      throw new IllegalArgumentException(
          "Comment blocked by safety screen: " + String.join("; ", screenResult.reasons()));
    }

    String status =
        screenResult.verdict() == ScreeningVerdict.REVIEW_REQUIRED ? "PENDING_REVIEW" : "APPROVED";

    CommunityCommentEntity comment =
        new CommunityCommentEntity(recipe, author, content.trim(), status);
    CommunityCommentEntity saved = commentRepository.save(comment);

    if ("APPROVED".equals(status)) {
      recipe.setCommentCount(recipe.getCommentCount() + 1);
      recipeRepository.save(recipe);
    }

    return mapCommentToDto(saved, authorId);
  }

  public void deleteComment(Long userId, Long commentId) {
    CommunityCommentEntity comment = commentRepository.findById(commentId).orElseThrow();
    UserEntity user = userRepository.findById(userId).orElseThrow();

    if (!comment.getAuthor().getId().equals(userId) && !"ROLE_ADMIN".equals(user.getRole())) {
      throw new SecurityException("Unauthorized to delete this comment");
    }

    CommunityRecipeEntity recipe = comment.getCommunityRecipe();
    commentRepository.delete(comment);
    recipe.setCommentCount(Math.max(0, recipe.getCommentCount() - 1));
    recipeRepository.save(recipe);
  }

  @Transactional(readOnly = true)
  public List<CommunityCommentDto> getCommentsForRecipe(Long recipeId, Long currentUserId) {
    return commentRepository
        .findByCommunityRecipeIdAndStatusOrderByCreatedAtAsc(recipeId, "APPROVED")
        .stream()
        .map(c -> mapCommentToDto(c, currentUserId))
        .toList();
  }

  public boolean toggleFollow(Long followerId, Long followingId) {
    if (followerId.equals(followingId)) {
      throw new IllegalArgumentException("Cannot follow yourself");
    }

    UserEntity follower = userRepository.findById(followerId).orElseThrow();
    UserEntity following = userRepository.findById(followingId).orElseThrow();

    Optional<CommunityFollowEntity> existing =
        followRepository.findByFollowerIdAndFollowingId(followerId, followingId);

    if (existing.isPresent()) {
      followRepository.delete(existing.get());
      return false;
    } else {
      followRepository.save(new CommunityFollowEntity(follower, following));
      return true;
    }
  }

  @Transactional(readOnly = true)
  public CreatorProfileDto getCreatorProfile(String username, Long currentUserId) {
    UserEntity creator =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Creator not found: " + username));

    long approvedCount = recipeRepository.countByAuthorIdAndStatus(creator.getId(), "APPROVED");
    long followersCount = followRepository.countByFollowingId(creator.getId());
    long followingCount = followRepository.countByFollowerId(creator.getId());

    boolean isFollowedByMe =
        currentUserId != null
            && followRepository.existsByFollowerIdAndFollowingId(currentUserId, creator.getId());
    boolean isSelf = currentUserId != null && currentUserId.equals(creator.getId());

    List<CommunityRecipeDto> recipes =
        recipeRepository
            .findByAuthorIdAndStatusOrderByCreatedAtDesc(creator.getId(), "APPROVED")
            .stream()
            .map(r -> mapToDto(r, currentUserId))
            .toList();

    return new CreatorProfileDto(
        creator.getId(),
        creator.getUsername(),
        creator.getFullName(),
        creator.getBio(),
        creator.getAvatarUrl(),
        creator.isVerifiedPro(),
        creator.getProCategory(),
        approvedCount,
        followersCount,
        followingCount,
        isFollowedByMe,
        isSelf,
        recipes);
  }

  private CommunityRecipeDto mapToDto(CommunityRecipeEntity e, Long currentUserId) {
    boolean isLiked =
        currentUserId != null
            && likeRepository.existsByUserIdAndCommunityRecipeId(currentUserId, e.getId());
    boolean isBookmarked = false; // can connect to SavedRecipeRepository if needed

    List<String> tags =
        e.getDietaryTags() != null && !e.getDietaryTags().isBlank()
            ? Arrays.asList(e.getDietaryTags().split(","))
            : List.of();

    List<CommunityRecipeDto.CommunityIngredientDto> ingDtos =
        e.getIngredients().stream()
            .map(
                i ->
                    new CommunityRecipeDto.CommunityIngredientDto(
                        i.getId(),
                        i.getFood() != null ? i.getFood().getId() : null,
                        i.getIngredientName(),
                        i.getAmountG(),
                        i.getUnit(),
                        i.isVerifiedFood()))
            .toList();

    return new CommunityRecipeDto(
        e.getId(),
        e.getAuthor().getId(),
        e.getAuthor().getUsername(),
        e.getAuthor().getFullName(),
        e.getAuthor().getAvatarUrl(),
        e.getAuthor().isVerifiedPro(),
        e.getAuthor().getProCategory(),
        e.getTitle(),
        e.getDescription(),
        e.getCoverImageUrl(),
        e.getBaseServings(),
        e.getPrepMinutes(),
        e.getCookMinutes(),
        e.getMealSlot(),
        tags,
        e.getInstructionsText(),
        e.getCaloriesPerServing(),
        e.getProteinG(),
        e.getCarbsG(),
        e.getFatG(),
        e.getFiberG(),
        e.getSodiumMg(),
        e.getIronMg(),
        e.getCalciumMg(),
        e.getStatus(),
        e.getLikeCount(),
        e.getCommentCount(),
        e.getBookmarkCount(),
        isLiked,
        isBookmarked,
        ingDtos,
        e.getCreatedAt(),
        DATE_FORMATTER.format(e.getCreatedAt()));
  }

  private CommunityCommentDto mapCommentToDto(CommunityCommentEntity c, Long currentUserId) {
    boolean isOwn = currentUserId != null && currentUserId.equals(c.getAuthor().getId());
    return new CommunityCommentDto(
        c.getId(),
        c.getCommunityRecipe().getId(),
        c.getAuthor().getId(),
        c.getAuthor().getUsername(),
        c.getAuthor().getAvatarUrl(),
        c.getAuthor().isVerifiedPro(),
        c.getAuthor().getProCategory(),
        c.getContent(),
        c.getStatus(),
        isOwn,
        c.getCreatedAt(),
        DATE_FORMATTER.format(c.getCreatedAt()));
  }

  private double round(double val) {
    return BigDecimal.valueOf(val).setScale(1, RoundingMode.HALF_UP).doubleValue();
  }
}
