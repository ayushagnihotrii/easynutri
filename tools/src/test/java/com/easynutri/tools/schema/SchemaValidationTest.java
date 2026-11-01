package com.easynutri.tools.schema;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SchemaValidationTest {

  private static ObjectMapper objectMapper;
  private static JsonSchemaFactory schemaFactory;
  private static Path dataDir;

  @BeforeAll
  static void setUp() {
    objectMapper = new ObjectMapper();
    schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    String rootProp = System.getProperty("project.root.dir");
    if (rootProp != null && !rootProp.isBlank()) {
      dataDir = Path.of(rootProp, "data");
    } else {
      dataDir = Path.of("..", "data");
    }
  }

  private Set<ValidationMessage> validateJson(Path schemaPath, Path dataPath) throws IOException {
    JsonSchema schema = schemaFactory.getSchema(schemaPath.toUri());
    JsonNode jsonNode = objectMapper.readTree(dataPath.toFile());
    return schema.validate(jsonNode);
  }

  private Set<ValidationMessage> validateJsonString(Path schemaPath, String jsonContent)
      throws IOException {
    JsonSchema schema = schemaFactory.getSchema(schemaPath.toUri());
    JsonNode jsonNode = objectMapper.readTree(jsonContent);
    return schema.validate(jsonNode);
  }

  @Test
  @DisplayName("Validate sample food JSON against food.schema.json")
  void testSampleFoodValid() throws IOException {
    Path schema = dataDir.resolve("schemas/food.schema.json");
    Path sample = dataDir.resolve("foods/sample_food.json");
    Set<ValidationMessage> errors = validateJson(schema, sample);
    assertTrue(errors.isEmpty(), "Sample food must pass schema validation: " + errors);
  }

  @Test
  @DisplayName("Validate sample recipe JSON against recipe.schema.json")
  void testSampleRecipeValid() throws IOException {
    Path schema = dataDir.resolve("schemas/recipe.schema.json");
    Path sample = dataDir.resolve("recipes/sample_recipe.json");
    Set<ValidationMessage> errors = validateJson(schema, sample);
    assertTrue(errors.isEmpty(), "Sample recipe must pass schema validation: " + errors);
  }

  @Test
  @DisplayName("Validate sample rule JSON against rule.schema.json")
  void testSampleRuleValid() throws IOException {
    Path schema = dataDir.resolve("schemas/rule.schema.json");
    Path sample = dataDir.resolve("rules/sample_rule.json");
    Set<ValidationMessage> errors = validateJson(schema, sample);
    assertTrue(errors.isEmpty(), "Sample rule must pass schema validation: " + errors);
  }

  @Test
  @DisplayName("Validate sample substitution JSON against substitution.schema.json")
  void testSampleSubstitutionValid() throws IOException {
    Path schema = dataDir.resolve("schemas/substitution.schema.json");
    Path sample = dataDir.resolve("substitutions/sample_substitution.json");
    Set<ValidationMessage> errors = validateJson(schema, sample);
    assertTrue(errors.isEmpty(), "Sample substitution must pass schema validation: " + errors);
  }

  @Test
  @DisplayName("Assert that corrupted sample rule without required 'source' field fails validation")
  void testBrokenRuleFailsValidation() throws IOException {
    Path schema = dataDir.resolve("schemas/rule.schema.json");
    String brokenRuleJson =
        """
            {
              "id": "rule_broken",
              "condition": "diabetes",
              "status": "DRAFT"
            }
            """;
    Set<ValidationMessage> errors = validateJsonString(schema, brokenRuleJson);
    assertFalse(errors.isEmpty(), "Corrupted rule must fail schema validation");
    assertTrue(
        errors.stream().anyMatch(msg -> msg.getMessage().contains("source")),
        "Expected failure for missing 'source' property");
  }

  @Test
  @DisplayName("Assert that corrupted food without required 'verified' field fails validation")
  void testBrokenFoodFailsValidation() throws IOException {
    Path schema = dataDir.resolve("schemas/food.schema.json");
    String brokenFoodJson =
        """
            {
              "id": "food_broken",
              "name_en": "Corrupted Food",
              "name_hi": "दूषित भोजन",
              "category": "SNACKS",
              "energy_kcal": 100.0,
              "protein_g": 2.0,
              "carbohydrate_g": 10.0,
              "fat_g": 5.0,
              "source": "SAMPLE"
            }
            """;
    Set<ValidationMessage> errors = validateJsonString(schema, brokenFoodJson);
    assertFalse(errors.isEmpty(), "Corrupted food must fail schema validation");
    assertTrue(
        errors.stream().anyMatch(msg -> msg.getMessage().contains("verified")),
        "Expected failure for missing 'verified' property");
  }
}
