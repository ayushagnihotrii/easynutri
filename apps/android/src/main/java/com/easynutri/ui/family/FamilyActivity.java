package com.easynutri.ui.family;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.R;
import com.easynutri.data.model.Models.HouseholdDto;
import com.easynutri.data.model.Models.HouseholdMemberDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.ActivityFamilyBinding;
import com.google.android.material.card.MaterialCardView;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

@AndroidEntryPoint
public class FamilyActivity extends AppCompatActivity {

  @Inject AppRepository repository;

  private ActivityFamilyBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityFamilyBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setupToolbar();
    setupActions();
    loadHousehold();
  }

  private void setupToolbar() {
    binding.toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void setupActions() {
    binding.btnAddMember.setOnClickListener(v -> showAddMemberDialog());
  }

  private void loadHousehold() {
    binding.progressBar.setVisibility(View.VISIBLE);
    binding.tvEmpty.setVisibility(View.GONE);

    repository.getHousehold(
        new AppRepository.DataCallback<>() {
          @Override
          public void onSuccess(HouseholdDto household) {
            binding.progressBar.setVisibility(View.GONE);
            renderHousehold(household);
          }

          @Override
          public void onError(String message) {
            binding.progressBar.setVisibility(View.GONE);
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.tvEmpty.setText("No household found or unable to load: " + message);
          }
        });
  }

  private void renderHousehold(HouseholdDto household) {
    if (household.name != null && !household.name.isEmpty()) {
      binding.tvHouseholdName.setText(household.name);
    }
    binding.layoutMembers.removeAllViews();
    if (household.members == null || household.members.isEmpty()) {
      binding.tvEmpty.setVisibility(View.VISIBLE);
      return;
    }
    binding.tvEmpty.setVisibility(View.GONE);

    for (HouseholdMemberDto member : household.members) {
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

      TextView name = new TextView(this);
      name.setText(member.displayName != null ? member.displayName : "Member");
      name.setTextColor(getColor(R.color.text_primary));
      name.setTextSize(16);
      name.setTypeface(null, android.graphics.Typeface.BOLD);

      TextView sub = new TextView(this);
      String relation = member.relationship != null ? member.relationship : "Family";
      sub.setText(
          relation
              + " · Age "
              + member.age
              + (member.allergies != null && !member.allergies.isEmpty()
                  ? " · Allergies: " + String.join(", ", member.allergies)
                  : " · No allergies"));
      sub.setTextColor(getColor(R.color.text_secondary));
      sub.setTextSize(13);

      container.addView(name);
      container.addView(sub);
      card.addView(container);
      binding.layoutMembers.addView(card);
    }
  }

  private void showAddMemberDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.dialog_add_member_title);

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(40, 20, 40, 20);

    EditText etName = new EditText(this);
    etName.setHint("Member Name (e.g. Priya, Aarav)");
    layout.addView(etName);

    EditText etCalories = new EditText(this);
    etCalories.setHint("Target Calories (e.g. 1800)");
    etCalories.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    layout.addView(etCalories);

    EditText etPref = new EditText(this);
    etPref.setHint("Dietary Preference (e.g. Vegetarian, Gluten-Free)");
    layout.addView(etPref);

    builder.setView(layout);

    builder.setPositiveButton(
        R.string.btn_save,
        (dialog, which) -> {
          String name = etName.getText().toString().trim();
          if (name.isEmpty()) return;
          int cal = 2000;
          try {
            if (!etCalories.getText().toString().trim().isEmpty()) {
              cal = Integer.parseInt(etCalories.getText().toString().trim());
            }
          } catch (Exception ignored) {
          }
          String pref = etPref.getText().toString().trim();
          List<String> prefs =
              pref.isEmpty() ? Collections.emptyList() : Collections.singletonList(pref);

          repository.addHouseholdMember(
              name,
              cal,
              Collections.<String>emptyList(),
              prefs,
              new AppRepository.DataCallback<HouseholdMemberDto>() {
                @Override
                public void onSuccess(HouseholdMemberDto data) {
                  Toast.makeText(
                          FamilyActivity.this, "Added family member: " + name, Toast.LENGTH_SHORT)
                      .show();
                  loadHousehold();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(
                          FamilyActivity.this, "Add member error: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });

    builder.setNegativeButton(R.string.btn_cancel, null);
    builder.show();
  }
}
