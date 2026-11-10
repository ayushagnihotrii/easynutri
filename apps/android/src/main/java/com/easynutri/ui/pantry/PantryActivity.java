package com.easynutri.ui.pantry;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.R;
import com.easynutri.data.model.Models.GroceryItemDto;
import com.easynutri.data.model.Models.PantryItemDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.ActivityPantryBinding;
import com.google.android.material.card.MaterialCardView;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.List;
import javax.inject.Inject;

@AndroidEntryPoint
public class PantryActivity extends AppCompatActivity {

  @Inject AppRepository repository;

  private ActivityPantryBinding binding;
  private boolean isPantryTab = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityPantryBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    String initialTab = getIntent().getStringExtra("initial_tab");
    if ("grocery".equalsIgnoreCase(initialTab)) {
      isPantryTab = false;
    }

    setupToolbar();
    setupTabs();
    setupActions();
    loadCurrentTabData();
  }

  private void setupToolbar() {
    binding.toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void setupTabs() {
    updateTabButtons();

    binding.btnTabPantry.setOnClickListener(
        v -> {
          isPantryTab = true;
          updateTabButtons();
          loadCurrentTabData();
        });

    binding.btnTabGrocery.setOnClickListener(
        v -> {
          isPantryTab = false;
          updateTabButtons();
          loadCurrentTabData();
        });
  }

  private void updateTabButtons() {
    if (isPantryTab) {
      binding.toolbar.setTitle(R.string.title_pantry);
      binding.btnTabPantry.setBackgroundColor(getColor(R.color.brand_primary));
      binding.btnTabPantry.setTextColor(getColor(R.color.text_inverse));
      binding.btnTabGrocery.setBackgroundColor(getColor(R.color.brand_secondary));
      binding.btnTabGrocery.setTextColor(getColor(R.color.brand_primary));
      binding.btnAddItem.setText("+ Add Pantry Item");
      binding.btnTransferGrocery.setVisibility(View.GONE);
    } else {
      binding.toolbar.setTitle(R.string.title_grocery);
      binding.btnTabGrocery.setBackgroundColor(getColor(R.color.brand_primary));
      binding.btnTabGrocery.setTextColor(getColor(R.color.text_inverse));
      binding.btnTabPantry.setBackgroundColor(getColor(R.color.brand_secondary));
      binding.btnTabPantry.setTextColor(getColor(R.color.brand_primary));
      binding.btnAddItem.setText("+ Add Grocery Item");
      binding.btnTransferGrocery.setVisibility(View.VISIBLE);
    }
  }

  private void setupActions() {
    binding.btnAddItem.setOnClickListener(
        v -> {
          if (isPantryTab) {
            showAddPantryDialog();
          } else {
            showAddGroceryDialog();
          }
        });

    binding.btnTransferGrocery.setOnClickListener(
        v -> {
          binding.progressBar.setVisibility(View.VISIBLE);
          repository.transferGroceryToPantry(
              new AppRepository.DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean resp) {
                  binding.progressBar.setVisibility(View.GONE);
                  Toast.makeText(
                          PantryActivity.this,
                          "Checked grocery items transferred to pantry!",
                          Toast.LENGTH_SHORT)
                      .show();
                  loadCurrentTabData();
                }

                @Override
                public void onError(String message) {
                  binding.progressBar.setVisibility(View.GONE);
                  Toast.makeText(
                          PantryActivity.this, "Transfer error: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });
  }

  private void loadCurrentTabData() {
    binding.progressBar.setVisibility(View.VISIBLE);
    binding.tvEmpty.setVisibility(View.GONE);
    binding.layoutItems.removeAllViews();

    if (isPantryTab) {
      repository.getPantry(
          new AppRepository.DataCallback<>() {
            @Override
            public void onSuccess(List<PantryItemDto> items) {
              binding.progressBar.setVisibility(View.GONE);
              renderPantryList(items);
            }

            @Override
            public void onError(String message) {
              binding.progressBar.setVisibility(View.GONE);
              binding.tvEmpty.setVisibility(View.VISIBLE);
              binding.tvEmpty.setText("Pantry error: " + message);
            }
          });
    } else {
      repository.getGroceryItems(
          new AppRepository.DataCallback<>() {
            @Override
            public void onSuccess(List<GroceryItemDto> items) {
              binding.progressBar.setVisibility(View.GONE);
              renderGroceryList(items);
            }

            @Override
            public void onError(String message) {
              binding.progressBar.setVisibility(View.GONE);
              binding.tvEmpty.setVisibility(View.VISIBLE);
              binding.tvEmpty.setText("Grocery error: " + message);
            }
          });
    }
  }

  private void renderPantryList(List<PantryItemDto> items) {
    binding.layoutItems.removeAllViews();
    if (items == null || items.isEmpty()) {
      binding.tvEmpty.setVisibility(View.VISIBLE);
      binding.tvEmpty.setText(R.string.empty_pantry);
      return;
    }
    binding.tvEmpty.setVisibility(View.GONE);

    for (PantryItemDto item : items) {
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
      container.setOrientation(LinearLayout.HORIZONTAL);
      container.setPadding(24, 20, 24, 20);
      container.setGravity(android.view.Gravity.CENTER_VERTICAL);

      LinearLayout textCol = new LinearLayout(this);
      textCol.setOrientation(LinearLayout.VERTICAL);
      LinearLayout.LayoutParams textParams =
          new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
      textCol.setLayoutParams(textParams);

      TextView title = new TextView(this);
      title.setText(item.foodNameEn != null ? item.foodNameEn : "Pantry Item");
      title.setTextColor(getColor(R.color.text_primary));
      title.setTextSize(15);
      title.setTypeface(null, android.graphics.Typeface.BOLD);

      TextView sub = new TextView(this);
      String expiryText =
          item.daysUntilExpiry < 0 ? "Expired" : "Expires in " + item.daysUntilExpiry + " days";
      sub.setText(
          item.quantityG + " " + (item.unit != null ? item.unit : "g") + " · " + expiryText);
      int subColor =
          item.daysUntilExpiry <= 2
              ? getColor(R.color.status_error)
              : getColor(R.color.text_secondary);
      sub.setTextColor(subColor);
      sub.setTextSize(12);

      textCol.addView(title);
      textCol.addView(sub);
      container.addView(textCol);

      Button btnConsume =
          new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
      btnConsume.setText("Consume");
      btnConsume.setTextSize(11);
      btnConsume.setOnClickListener(
          v -> {
            repository.consumePantryItem(
                item.id,
                new AppRepository.DataCallback<Boolean>() {
                  @Override
                  public void onSuccess(Boolean resp) {
                    Toast.makeText(
                            PantryActivity.this,
                            "Consumed " + (item.foodNameEn != null ? item.foodNameEn : "item"),
                            Toast.LENGTH_SHORT)
                        .show();
                    loadCurrentTabData();
                  }

                  @Override
                  public void onError(String message) {
                    Toast.makeText(
                            PantryActivity.this, "Consume error: " + message, Toast.LENGTH_SHORT)
                        .show();
                  }
                });
          });
      container.addView(btnConsume);

      card.addView(container);
      binding.layoutItems.addView(card);
    }
  }

  private void renderGroceryList(List<GroceryItemDto> items) {
    binding.layoutItems.removeAllViews();
    if (items == null || items.isEmpty()) {
      binding.tvEmpty.setVisibility(View.VISIBLE);
      binding.tvEmpty.setText(R.string.empty_grocery);
      return;
    }
    binding.tvEmpty.setVisibility(View.GONE);

    for (GroceryItemDto item : items) {
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
      container.setOrientation(LinearLayout.HORIZONTAL);
      container.setPadding(24, 20, 24, 20);
      container.setGravity(android.view.Gravity.CENTER_VERTICAL);

      CheckBox cb = new CheckBox(this);
      cb.setChecked(item.checked);
      cb.setOnCheckedChangeListener(
          (buttonView, isChecked) -> {
            repository.toggleGroceryItem(
                item.id,
                new AppRepository.DataCallback<>() {
                  @Override
                  public void onSuccess(GroceryItemDto updated) {
                    // Updated
                  }

                  @Override
                  public void onError(String message) {
                    Toast.makeText(
                            PantryActivity.this, "Toggle error: " + message, Toast.LENGTH_SHORT)
                        .show();
                  }
                });
          });
      container.addView(cb);

      LinearLayout textCol = new LinearLayout(this);
      textCol.setOrientation(LinearLayout.VERTICAL);
      LinearLayout.LayoutParams textParams =
          new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
      textCol.setLayoutParams(textParams);

      TextView title = new TextView(this);
      title.setText(item.foodNameEn != null ? item.foodNameEn : "Grocery Item");
      title.setTextColor(getColor(R.color.text_primary));
      title.setTextSize(15);
      title.setTypeface(null, android.graphics.Typeface.BOLD);

      TextView sub = new TextView(this);
      sub.setText(
          item.quantityG
              + " "
              + (item.unit != null ? item.unit : "units")
              + " · "
              + (item.category != null ? item.category : "General"));
      sub.setTextColor(getColor(R.color.text_secondary));
      sub.setTextSize(12);

      textCol.addView(title);
      textCol.addView(sub);
      container.addView(textCol);

      Button btnDelete =
          new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
      btnDelete.setText("✕");
      btnDelete.setLayoutParams(new LinearLayout.LayoutParams(54, 40));
      btnDelete.setOnClickListener(
          v -> {
            repository.deleteGroceryItem(
                item.id,
                new AppRepository.DataCallback<Boolean>() {
                  @Override
                  public void onSuccess(Boolean resp) {
                    loadCurrentTabData();
                  }

                  @Override
                  public void onError(String message) {
                    Toast.makeText(
                            PantryActivity.this, "Delete error: " + message, Toast.LENGTH_SHORT)
                        .show();
                  }
                });
          });
      container.addView(btnDelete);

      card.addView(container);
      binding.layoutItems.addView(card);
    }
  }

  private void showAddPantryDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.dialog_add_pantry_title);

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(40, 20, 40, 20);

    EditText etName = new EditText(this);
    etName.setHint("Item Name (e.g. Greek Yogurt, Oats)");
    layout.addView(etName);

    EditText etQuantity = new EditText(this);
    etQuantity.setHint("Quantity (e.g. 500)");
    etQuantity.setInputType(
        android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
    layout.addView(etQuantity);

    EditText etUnit = new EditText(this);
    etUnit.setHint("Unit (g, ml, count, pack)");
    layout.addView(etUnit);

    builder.setView(layout);

    builder.setPositiveButton(
        "Add",
        (dialog, which) -> {
          String name = etName.getText().toString().trim();
          if (name.isEmpty()) return;
          double qty = 1.0;
          try {
            if (!etQuantity.getText().toString().trim().isEmpty()) {
              qty = Double.parseDouble(etQuantity.getText().toString().trim());
            }
          } catch (Exception ignored) {
          }
          String unit = etUnit.getText().toString().trim();
          if (unit.isEmpty()) unit = "g";

          repository.addPantryItem(
              name,
              qty,
              unit,
              7,
              "FRIDGE",
              new AppRepository.DataCallback<>() {
                @Override
                public void onSuccess(PantryItemDto data) {
                  Toast.makeText(
                          PantryActivity.this, "Added " + name + " to pantry", Toast.LENGTH_SHORT)
                      .show();
                  loadCurrentTabData();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(PantryActivity.this, "Add error: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });

    builder.setNegativeButton(R.string.btn_cancel, null);
    builder.show();
  }

  private void showAddGroceryDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.dialog_add_grocery_title);

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(40, 20, 40, 20);

    EditText etName = new EditText(this);
    etName.setHint("Grocery Item (e.g. Olive Oil, Spinach)");
    layout.addView(etName);

    EditText etQuantity = new EditText(this);
    etQuantity.setHint("Quantity (e.g. 1)");
    etQuantity.setInputType(
        android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
    layout.addView(etQuantity);

    builder.setView(layout);

    builder.setPositiveButton(
        "Add",
        (dialog, which) -> {
          String name = etName.getText().toString().trim();
          if (name.isEmpty()) return;
          double qty = 1.0;
          try {
            if (!etQuantity.getText().toString().trim().isEmpty()) {
              qty = Double.parseDouble(etQuantity.getText().toString().trim());
            }
          } catch (Exception ignored) {
          }

          repository.addGroceryItem(
              name,
              qty,
              "item",
              "Produce",
              new AppRepository.DataCallback<>() {
                @Override
                public void onSuccess(GroceryItemDto data) {
                  Toast.makeText(PantryActivity.this, "Added to grocery list", Toast.LENGTH_SHORT)
                      .show();
                  loadCurrentTabData();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(PantryActivity.this, "Add error: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });

    builder.setNegativeButton(R.string.btn_cancel, null);
    builder.show();
  }
}
