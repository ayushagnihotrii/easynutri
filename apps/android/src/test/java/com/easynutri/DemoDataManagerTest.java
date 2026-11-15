package com.easynutri;

import static org.junit.Assert.*;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.easynutri.data.demo.DemoDataManager;
import com.easynutri.data.model.Models.PantryItemDto;
import com.easynutri.data.model.Models.RecipeDetailDto;
import com.easynutri.data.model.Models.RecipeMatchItemDto;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class DemoDataManagerTest {

  private DemoDataManager demoDataManager;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    demoDataManager = DemoDataManager.getInstance(context);
    demoDataManager.resetToBaseline();
  }

  @Test
  public void testPreloadedPantryContainsFourteenIngredients() {
    List<PantryItemDto> pantry = demoDataManager.getPantryItems();
    assertNotNull(pantry);
    assertEquals(14, pantry.size());

    boolean hasRice = pantry.stream().anyMatch(i -> i.foodId.contains("rice"));
    boolean hasMoong = pantry.stream().anyMatch(i -> i.foodId.contains("moong"));
    boolean hasSpinach = pantry.stream().anyMatch(i -> i.foodId.contains("spinach"));
    boolean hasPaneer = pantry.stream().anyMatch(i -> i.foodId.contains("paneer"));
    boolean hasOats = pantry.stream().anyMatch(i -> i.foodId.contains("oats"));

    assertTrue(hasRice);
    assertTrue(hasMoong);
    assertTrue(hasSpinach);
    assertTrue(hasPaneer);
    assertTrue(hasOats);
  }

  @Test
  public void testPreloadedRecipesContainAllDemonstrationItems() {
    List<RecipeDetailDto> recipes = demoDataManager.getAllRecipes();
    assertNotNull(recipes);
    assertTrue(recipes.size() >= 8);

    boolean hasKhichdi = recipes.stream().anyMatch(r -> r.titleEn.contains("Khichdi"));
    boolean hasPalakDal = recipes.stream().anyMatch(r -> r.titleEn.contains("Palak"));
    boolean hasPulao = recipes.stream().anyMatch(r -> r.titleEn.contains("Pulao"));
    boolean hasPoha = recipes.stream().anyMatch(r -> r.titleEn.contains("Poha"));
    boolean hasOats = recipes.stream().anyMatch(r -> r.titleEn.contains("Oats"));

    assertTrue(hasKhichdi);
    assertTrue(hasPalakDal);
    assertTrue(hasPulao);
    assertTrue(hasPoha);
    assertTrue(hasOats);
  }

  @Test
  public void testServingScalingRecalculatesIngredients() {
    RecipeDetailDto recipe2 = demoDataManager.getRecipeById("recipe_moong_khichdi", 2);
    assertNotNull(recipe2);
    double rice2 =
        recipe2.ingredients.stream()
            .filter(i -> i.foodId.contains("rice"))
            .findFirst()
            .get()
            .amountG;

    RecipeDetailDto recipe4 = demoDataManager.getRecipeById("recipe_moong_khichdi", 4);
    assertNotNull(recipe4);
    double rice4 =
        recipe4.ingredients.stream()
            .filter(i -> i.foodId.contains("rice"))
            .findFirst()
            .get()
            .amountG;

    assertEquals(2 * rice2, rice4, 0.1);
    assertEquals(4, recipe4.defaultServings);
  }

  @Test
  public void testSampleIngredientMatchingFindsMoongKhichdiAndSpinachKhichdi() {
    List<String> sampleIngredients = demoDataManager.getSampleIngredientIds();
    assertEquals(5, sampleIngredients.size());

    List<RecipeMatchItemDto> matches =
        demoDataManager.matchRecipes(sampleIngredients, 2, "LUNCH", "", "VEGETARIAN");
    assertNotNull(matches);
    assertFalse(matches.isEmpty());

    RecipeMatchItemDto topMatch = matches.get(0);
    assertTrue(topMatch.matchPercentage >= 75);
    assertNotNull(topMatch.recipe);
  }

  @Test
  public void testResetDemoRestoresPristineState() {
    demoDataManager.logWater(1000);
    demoDataManager.logMealToDiary("recipe_moong_khichdi", 2, "Dinner");

    demoDataManager.resetToBaseline();

    List<PantryItemDto> pantry = demoDataManager.getPantryItems();
    assertEquals(14, pantry.size());
    assertEquals(1, demoDataManager.getDayDiary().slots.get(0).items.size()); // Original breakfast
  }
}
