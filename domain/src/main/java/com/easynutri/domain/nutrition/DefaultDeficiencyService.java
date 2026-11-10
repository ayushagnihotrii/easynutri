package com.easynutri.domain.nutrition;

import com.easynutri.domain.model.FoodCandidate;
import com.easynutri.domain.model.ProfileContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Pure Java implementation of DeficiencyService aligned with ICMR-NIN 2024 Dietary Guidelines for
 * Indians.
 */
public final class DefaultDeficiencyService implements DeficiencyService {

  private static final Map<String, NutrientInfo> NUTRIENTS = new LinkedHashMap<>();

  static {
    NUTRIENTS.put(
        "iron",
        new NutrientInfo(
            "iron",
            "Iron",
            "आयरन (लोहा)",
            "mg",
            19.0,
            29.0,
            27.0,
            "Vital for hemoglobin synthesis, oxygen transport, and cognitive energy.",
            List.of(
                "Pair plant iron (spinach, dal, beans) with Vitamin C (lemon, amla, tomato) to enhance absorption up to 300%.",
                "Avoid tea, coffee, and milk within 2 hours of iron-rich meals (tannins and polyphenols inhibit iron uptake).",
                "Cook in cast-iron cookware to naturally boost dietary iron content."),
            List.of("Fatigue", "Pale conjunctiva", "Cold extremities", "Reduced stamina"),
            null));

    NUTRIENTS.put(
        "vitamin_b12",
        new NutrientInfo(
            "vitamin_b12",
            "Vitamin B12",
            "विटामिन बी12",
            "mcg",
            2.2,
            2.2,
            2.5,
            "Critical for red blood cell formation, neurological integrity, and DNA synthesis.",
            List.of(
                "Vegetarians should consume regular portions of curd, paneer, and milk.",
                "Vegans must prioritize fortified foods or clinically supervised methylcobalamin.",
                "Gastric acidity is necessary for intrinsic-factor B12 absorption."),
            List.of("Nerve tingling", "Memory fog", "Pernicious anemia", "Fatigue"),
            null));

    NUTRIENTS.put(
        "vitamin_d",
        new NutrientInfo(
            "vitamin_d",
            "Vitamin D",
            "विटामिन डी",
            "mcg",
            15.0,
            15.0,
            15.0,
            "Facilitates calcium homeostasis, bone mineralization, and immune modulation.",
            List.of(
                "Fat-soluble: consume with dietary healthy fats (ghee, nuts, seeds) for maximal absorption.",
                "15-20 minutes of mid-day sun exposure (arms and face) triggers endogenous synthesis.",
                "Fortified milk and mushrooms provide supplementary dietary quantities."),
            List.of("Bone tenderness", "Muscle weakness", "Frequent infections", "Low mood"),
            null));

    NUTRIENTS.put(
        "calcium",
        new NutrientInfo(
            "calcium",
            "Calcium",
            "कैल्शियम",
            "mg",
            1000.0,
            1000.0,
            1200.0,
            "Essential for skeletal strength, cardiac rhythm regulation, and neuromuscular signaling.",
            List.of(
                "Vitamin D sufficiency is mandatory for active intestinal calcium transport.",
                "Divide calcium intake across meals rather than consuming single oversized portions.",
                "Sesame seeds (til), ragi, paneer, and curd are premier Indian bioavailable sources."),
            List.of("Muscle cramps", "Brittle nails", "Osteopenia risk", "Tooth sensitivity"),
            null));

    NUTRIENTS.put(
        "protein",
        new NutrientInfo(
            "protein",
            "Protein",
            "प्रोटीन",
            "g",
            54.0,
            46.0,
            65.0,
            "Structural backbone for lean muscle maintenance, tissue repair, and immune antibodies.",
            List.of(
                "Combine grains with pulses (e.g. dal-chawal, khichdi) at 3:1 ratio for complete PDCAAS amino acid profiles.",
                "Distribute 20-30g of protein across breakfast, lunch, and dinner.",
                "Paneer, tofu, sattu, sprouted moong, and curd are high-density Indian staples."),
            List.of("Muscle loss", "Slow recovery", "Thinning hair", "Persistent hunger"),
            null));

    NUTRIENTS.put(
        "fibre",
        new NutrientInfo(
            "fibre",
            "Dietary Fibre",
            "आहार फाइबर",
            "g",
            35.0,
            30.0,
            30.0,
            "Supports gut microbiome, stabilizes postprandial glucose, and lowers LDL cholesterol.",
            List.of(
                "Increase daily fibre gradually while drinking at least 2.5-3.0 litres of water to prevent digestive discomfort.",
                "Consume whole fruits instead of strained juices, and opt for whole grains like oats, brown rice, and millets.",
                "Add psyllium husk (isabgol) or flaxseed powder if dietary intake remains low."),
            List.of(
                "Constipation",
                "Blood sugar spikes",
                "Irregular bowel movements",
                "High cholesterol"),
            null));

    NUTRIENTS.put(
        "folate",
        new NutrientInfo(
            "folate",
            "Folate (Vitamin B9)",
            "फोलेट (विटामिन बी9)",
            "mcg",
            300.0,
            300.0,
            500.0,
            "Crucial for cellular division, nucleotide synthesis, and maternal fetal neural development.",
            List.of(
                "Folate is heat-sensitive; steam greens lightly or consume fresh salads to avoid losing up to 50% through boiling.",
                "Rich sources: spinach, methi, chana, rajma, oranges, and fortified whole grains.",
                "Critical periconceptional nutrient for preventing neural tube defects."),
            List.of("Megaloblastic anemia", "Mouth sores", "Lethargy", "Elevated homocysteine"),
            null));

    NUTRIENTS.put(
        "zinc",
        new NutrientInfo(
            "zinc",
            "Zinc",
            "जिंक (जस्ता)",
            "mg",
            13.0,
            11.0,
            14.0,
            "Required for >300 cellular enzymes, immune defenses, wound healing, and taste sensation.",
            List.of(
                "Soaking, sprouting, or fermenting pulses and grains breaks down phytates, drastically boosting zinc bioavailability.",
                "Pumpkin seeds, sesame seeds, lentils, and dairy provide rich bioavailable zinc.",
                "Do not combine high-dose zinc with iron supplements simultaneously."),
            List.of(
                "Impaired immunity", "Delayed wound healing", "Loss of taste/smell", "Hair loss"),
            null));

    NUTRIENTS.put(
        "vitamin_c",
        new NutrientInfo(
            "vitamin_c",
            "Vitamin C",
            "विटामिन सी",
            "mg",
            80.0,
            65.0,
            90.0,
            "Potent antioxidant, essential for collagen synthesis, endothelial health, and iron absorption.",
            List.of(
                "Highly vulnerable to heat: squeeze lemon juice or sprinkle raw coriander after cooking, not in boiling pots.",
                "Amla (Indian gooseberry), guava, oranges, tomatoes, and bell peppers are superior sources.",
                "Daily intake is necessary because Vitamin C is water-soluble and cannot be stored."),
            List.of("Bleeding gums", "Slow healing", "Easy bruising", "Joint discomfort"),
            null));

    NUTRIENTS.put(
        "magnesium",
        new NutrientInfo(
            "magnesium",
            "Magnesium",
            "मैग्नीशियम",
            "mg",
            385.0,
            325.0,
            350.0,
            "Regulates muscle contractions, nerve transmission, blood glucose, and restorative sleep.",
            List.of(
                "Consume leafy greens, nuts (almonds, walnuts), seeds, and whole millets daily.",
                "Refined grains and excess refined sugar intake increase urinary magnesium loss.",
                "Supports evening muscle relaxation and healthy sleep architecture."),
            List.of("Muscle spasms", "Tremors", "Headaches", "Restless legs", "Insomnia"),
            null));
  }

  @Override
  public Set<String> detectDeficiencies(Map<String, Double> dailyIntake, ProfileContext profile) {
    Objects.requireNonNull(dailyIntake, "dailyIntake cannot be null");
    Objects.requireNonNull(profile, "profile cannot be null");

    Set<String> deficient = new HashSet<>();

    for (Map.Entry<String, NutrientInfo> entry : NUTRIENTS.entrySet()) {
      String key = entry.getKey();
      NutrientInfo info = entry.getValue();
      double target = getRda(info, profile);

      // Check daily intake for key or variant aliases
      double actual = getIntakeValue(dailyIntake, key);
      if (actual < (target * 0.70)) { // Deficient if below 70% of ICMR RDA
        deficient.add(key);
      }
    }

    return Collections.unmodifiableSet(deficient);
  }

  @Override
  public List<FoodCandidate> findFoodsRichIn(
      String targetNutrient,
      List<FoodCandidate> candidateFoods,
      ProfileContext profile,
      int limit) {

    Objects.requireNonNull(targetNutrient, "targetNutrient cannot be null");
    Objects.requireNonNull(candidateFoods, "candidateFoods cannot be null");

    String cleanNutrient = targetNutrient.trim().toLowerCase(Locale.ROOT);
    int safeLimit = Math.max(1, limit);

    List<FoodCandidate> safeCandidates = new ArrayList<>();
    for (FoodCandidate food : candidateFoods) {
      if (isDietAndAllergySafe(food, profile)) {
        double val = getFoodNutrientValue(food, cleanNutrient);
        if (val > 0.0) {
          safeCandidates.add(food);
        }
      }
    }

    safeCandidates.sort(
        (f1, f2) -> {
          double v1 = getFoodNutrientValue(f1, cleanNutrient);
          double v2 = getFoodNutrientValue(f2, cleanNutrient);
          return Double.compare(v2, v1); // Descending density
        });

    if (safeCandidates.size() > safeLimit) {
      return safeCandidates.subList(0, safeLimit);
    }
    return safeCandidates;
  }

  public List<NutrientInfo> getAllSupportedNutrients() {
    return List.copyOf(NUTRIENTS.values());
  }

  public NutrientInfo getNutrient(String key) {
    if (key == null) return null;
    return NUTRIENTS.get(key.trim().toLowerCase(Locale.ROOT));
  }

  public double getRdaForProfile(String nutrientKey, ProfileContext profile) {
    NutrientInfo info = getNutrient(nutrientKey);
    if (info == null) return 0.0;
    return getRda(info, profile);
  }

  private double getRda(NutrientInfo info, ProfileContext profile) {
    boolean isFemale = "FEMALE".equalsIgnoreCase(profile.gender());
    boolean isPregnant =
        profile.healthConditions().stream()
            .anyMatch(h -> h.toUpperCase(Locale.ROOT).contains("PREGNAN"));

    if (isPregnant) {
      return info.rdaPregnant();
    }
    return isFemale ? info.rdaFemale() : info.rdaMale();
  }

  private double getIntakeValue(Map<String, Double> intake, String key) {
    if (intake.containsKey(key)) return intake.get(key);
    // Aliases
    if (key.equals("fibre") && intake.containsKey("fiber")) return intake.get("fiber");
    if (key.equals("vitamin_b12") && intake.containsKey("b12")) return intake.get("b12");
    if (key.equals("vitamin_d") && intake.containsKey("d")) return intake.get("d");
    if (key.equals("vitamin_c") && intake.containsKey("c")) return intake.get("c");
    return 0.0;
  }

  private double getFoodNutrientValue(FoodCandidate food, String key) {
    Map<String, Double> n = food.nutrientsPer100g();
    if (n.containsKey(key)) return n.get(key);
    return switch (key) {
      case "iron" -> n.getOrDefault("iron_mg", 0.0);
      case "calcium" -> n.getOrDefault("calcium_mg", 0.0);
      case "protein" -> n.getOrDefault("protein_g", 0.0);
      case "fibre" -> n.getOrDefault("fibre_g", n.getOrDefault("fiber_g", 0.0));
      case "vitamin_c" -> n.getOrDefault("vitamin_c_mg", 0.0);
      case "zinc" -> n.getOrDefault("zinc_mg", 0.0);
      case "magnesium" -> n.getOrDefault("magnesium_mg", 0.0);
      case "vitamin_b12" -> n.getOrDefault("vitamin_b12_mcg", 0.0);
      case "vitamin_d" -> n.getOrDefault("vitamin_d_mcg", 0.0);
      case "folate" -> n.getOrDefault("folate_mcg", 0.0);
      default -> 0.0;
    };
  }

  private boolean isDietAndAllergySafe(FoodCandidate food, ProfileContext profile) {
    if (profile == null) return true;

    // Allergy check
    for (String allergy : profile.allergies()) {
      String cleanAllergy = allergy.trim().toLowerCase(Locale.ROOT);
      for (String tag : food.allergenTags()) {
        if (tag.toLowerCase(Locale.ROOT).contains(cleanAllergy)) {
          return false;
        }
      }
    }

    // Diet check (e.g. VEGETARIAN, VEGAN)
    String diet = profile.dietType() != null ? profile.dietType().toUpperCase(Locale.ROOT) : "";
    if ("VEGETARIAN".equals(diet) || "VEGAN".equals(diet)) {
      for (String flag : food.dietaryFlags()) {
        if ("NON_VEG".equalsIgnoreCase(flag)
            || "MEAT".equalsIgnoreCase(flag)
            || "FISH".equalsIgnoreCase(flag)) {
          return false;
        }
      }
    }
    if ("VEGAN".equals(diet)) {
      for (String flag : food.dietaryFlags()) {
        if ("DAIRY".equalsIgnoreCase(flag)
            || "EGG".equalsIgnoreCase(flag)
            || "HONEY".equalsIgnoreCase(flag)) {
          return false;
        }
      }
    }

    return true;
  }
}
