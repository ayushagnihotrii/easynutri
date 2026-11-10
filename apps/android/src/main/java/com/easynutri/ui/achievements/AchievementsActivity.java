package com.easynutri.ui.achievements;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.R;
import com.easynutri.data.model.Models.BadgeDto;
import com.easynutri.data.model.Models.ChallengeDto;
import com.easynutri.data.model.Models.UserStreakDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.ActivityAchievementsBinding;
import com.google.android.material.card.MaterialCardView;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.List;
import javax.inject.Inject;

@AndroidEntryPoint
public class AchievementsActivity extends AppCompatActivity {

  @Inject AppRepository repository;

  private ActivityAchievementsBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityAchievementsBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setupToolbar();
    loadGamificationData();
  }

  private void setupToolbar() {
    binding.toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void loadGamificationData() {
    binding.progressBar.setVisibility(View.VISIBLE);

    repository.getStreaks(
        new AppRepository.DataCallback<>() {
          @Override
          public void onSuccess(List<UserStreakDto> streaks) {
            if (streaks != null && !streaks.isEmpty()) {
              binding.tvStreakHero.setText(streaks.get(0).currentStreakDays + " Day Streak!");
            }
          }

          @Override
          public void onError(String message) {}
        });

    repository.getBadges(
        new AppRepository.DataCallback<List<BadgeDto>>() {
          @Override
          public void onSuccess(List<BadgeDto> badges) {
            binding.progressBar.setVisibility(View.GONE);
            renderBadges(badges);
          }

          @Override
          public void onError(String message) {
            binding.progressBar.setVisibility(View.GONE);
            renderDefaultBadges();
          }
        });

    repository.getChallenges(
        new AppRepository.DataCallback<List<ChallengeDto>>() {
          @Override
          public void onSuccess(List<ChallengeDto> challenges) {
            renderChallenges(challenges);
          }

          @Override
          public void onError(String message) {
            renderDefaultChallenges();
          }
        });
  }

  private void renderBadges(List<BadgeDto> badges) {
    binding.layoutBadges.removeAllViews();
    if (badges == null || badges.isEmpty()) {
      renderDefaultBadges();
      return;
    }
    for (BadgeDto badge : badges) {
      addBadgeCard(badge.title, badge.description, badge.unlocked);
    }
  }

  private void renderDefaultBadges() {
    binding.layoutBadges.removeAllViews();
    addBadgeCard("Hydration Pioneer", "Met daily 2,500 ml target for 3 consecutive days", true);
    addBadgeCard(
        "Balanced Plate Master", "Completed a full week within clinical macronutrient range", true);
    addBadgeCard("Community Creator", "Published your first safe, approved recipe", false);
    addBadgeCard("Century Walker", "Logged 10,000 steps in a single day", true);
  }

  private void addBadgeCard(String titleText, String descText, boolean unlocked) {
    MaterialCardView card = new MaterialCardView(this);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 8, 0, 8);
    card.setLayoutParams(params);
    card.setCardBackgroundColor(getColor(R.color.brand_surface));
    card.setStrokeColor(getColor(unlocked ? R.color.brand_primary : R.color.outline_subtle));
    card.setStrokeWidth(2);
    card.setRadius(16);

    LinearLayout container = new LinearLayout(this);
    container.setOrientation(LinearLayout.HORIZONTAL);
    container.setPadding(24, 20, 24, 20);
    container.setGravity(android.view.Gravity.CENTER_VERTICAL);

    TextView icon = new TextView(this);
    icon.setText(unlocked ? "🏆" : "🔒");
    icon.setTextSize(24);
    icon.setPadding(0, 0, 16, 0);

    LinearLayout textCol = new LinearLayout(this);
    textCol.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams textParams =
        new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
    textCol.setLayoutParams(textParams);

    TextView title = new TextView(this);
    title.setText(titleText);
    title.setTextColor(getColor(R.color.text_primary));
    title.setTextSize(15);
    title.setTypeface(null, android.graphics.Typeface.BOLD);

    TextView desc = new TextView(this);
    desc.setText(descText);
    desc.setTextColor(getColor(R.color.text_secondary));
    desc.setTextSize(12);

    textCol.addView(title);
    textCol.addView(desc);

    TextView status = new TextView(this);
    status.setText(unlocked ? "Earned" : "Locked");
    status.setTextColor(getColor(unlocked ? R.color.status_success : R.color.text_secondary));
    status.setTextSize(12);
    status.setTypeface(null, android.graphics.Typeface.BOLD);

    container.addView(icon);
    container.addView(textCol);
    container.addView(status);
    card.addView(container);
    binding.layoutBadges.addView(card);
  }

  private void renderChallenges(List<ChallengeDto> challenges) {
    binding.layoutChallenges.removeAllViews();
    if (challenges == null || challenges.isEmpty()) {
      renderDefaultChallenges();
      return;
    }
    for (ChallengeDto challenge : challenges) {
      addChallengeCard(
          challenge.title, challenge.description, challenge.progressPercent + "% Done");
    }
  }

  private void renderDefaultChallenges() {
    binding.layoutChallenges.removeAllViews();
    addChallengeCard(
        "7-Day Whole Grain Quest",
        "Incorporate oats, quinoa, or whole wheat into daily lunches",
        "71% Done");
    addChallengeCard(
        "Hydration Streak Challenge", "Drink at least 2.5L water daily this week", "85% Done");
  }

  private void addChallengeCard(String titleText, String descText, String progress) {
    MaterialCardView card = new MaterialCardView(this);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 8, 0, 8);
    card.setLayoutParams(params);
    card.setCardBackgroundColor(getColor(R.color.brand_surface));
    card.setStrokeColor(getColor(R.color.outline_subtle));
    card.setStrokeWidth(2);
    card.setRadius(16);

    LinearLayout container = new LinearLayout(this);
    container.setOrientation(LinearLayout.VERTICAL);
    container.setPadding(24, 20, 24, 20);

    LinearLayout top = new LinearLayout(this);
    top.setOrientation(LinearLayout.HORIZONTAL);

    TextView title = new TextView(this);
    title.setText(titleText);
    title.setTextColor(getColor(R.color.text_primary));
    title.setTextSize(15);
    title.setTypeface(null, android.graphics.Typeface.BOLD);
    LinearLayout.LayoutParams titleParams =
        new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
    title.setLayoutParams(titleParams);

    TextView prog = new TextView(this);
    prog.setText(progress);
    prog.setTextColor(getColor(R.color.brand_primary));
    prog.setTextSize(13);
    prog.setTypeface(null, android.graphics.Typeface.BOLD);

    top.addView(title);
    top.addView(prog);

    TextView desc = new TextView(this);
    desc.setText(descText);
    desc.setTextColor(getColor(R.color.text_secondary));
    desc.setTextSize(12);
    desc.setPadding(0, 4, 0, 0);

    container.addView(top);
    container.addView(desc);
    card.addView(container);
    binding.layoutChallenges.addView(card);
  }
}
