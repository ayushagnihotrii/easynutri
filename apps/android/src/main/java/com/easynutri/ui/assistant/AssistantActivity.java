package com.easynutri.ui.assistant;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.R;
import com.easynutri.data.model.Models.AssistantResponse;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.ActivityAssistantBinding;
import com.google.android.material.card.MaterialCardView;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class AssistantActivity extends AppCompatActivity {

  @Inject AppRepository repository;

  private ActivityAssistantBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityAssistantBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setupToolbar();
    setupPromptChips();
    setupActions();
    addAssistantMessage(
        "Namaste! I am your clinical wellness and nutrition assistant. How can I assist your meal planning or dietary targets today?");
  }

  private void setupToolbar() {
    binding.toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void setupPromptChips() {
    binding.chipPrompt1.setOnClickListener(
        v -> sendUserPrompt("What are high protein snacks suitable for evening cravings?"));
    binding.chipPrompt2.setOnClickListener(
        v -> sendUserPrompt("Give me low sodium meal suggestions for dinner."));
    binding.chipPrompt3.setOnClickListener(
        v -> sendUserPrompt("How much water should I drink on active workout days?"));
  }

  private void setupActions() {
    binding.btnSendChat.setOnClickListener(
        v -> {
          String text = binding.etChatInput.getText().toString().trim();
          if (!text.isEmpty()) {
            sendUserPrompt(text);
          }
        });
  }

  private void sendUserPrompt(String prompt) {
    addUserMessage(prompt);
    binding.etChatInput.setText("");
    binding.btnSendChat.setEnabled(false);

    repository.askAssistant(
        prompt,
        new AppRepository.DataCallback<AssistantResponse>() {
          @Override
          public void onSuccess(AssistantResponse response) {
            binding.btnSendChat.setEnabled(true);
            if (response != null && response.answer != null) {
              addAssistantMessage(response.answer);
            } else {
              addAssistantMessage(
                  "Thank you for your question. Maintaining balanced macronutrients and hydration supports your daily wellness goals.");
            }
          }

          @Override
          public void onError(String message) {
            binding.btnSendChat.setEnabled(true);
            addAssistantMessage(
                "EasyNutri Clinical Rule: Please ensure meals include complex carbohydrates, lean protein, and adequate hydration.");
          }
        });
  }

  private void addUserMessage(String message) {
    MaterialCardView card = new MaterialCardView(this);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.setMargins(80, 8, 0, 8);
    params.gravity = android.view.Gravity.END;
    card.setLayoutParams(params);
    card.setCardBackgroundColor(getColor(R.color.brand_primary));
    card.setRadius(24);
    card.setStrokeWidth(0);

    TextView text = new TextView(this);
    text.setText(message);
    text.setTextColor(getColor(R.color.text_inverse));
    text.setTextSize(14);
    text.setPadding(24, 16, 24, 16);

    card.addView(text);
    binding.layoutChatMessages.addView(card);
    scrollDown();
  }

  private void addAssistantMessage(String message) {
    MaterialCardView card = new MaterialCardView(this);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 8, 80, 8);
    params.gravity = android.view.Gravity.START;
    card.setLayoutParams(params);
    card.setCardBackgroundColor(getColor(R.color.brand_surface));
    card.setStrokeColor(getColor(R.color.outline_subtle));
    card.setStrokeWidth(2);
    card.setRadius(24);

    TextView text = new TextView(this);
    text.setText(message);
    text.setTextColor(getColor(R.color.text_primary));
    text.setTextSize(14);
    text.setLineSpacing(0, 1.2f);
    text.setPadding(24, 16, 24, 16);

    card.addView(text);
    binding.layoutChatMessages.addView(card);
    scrollDown();
  }

  private void scrollDown() {
    binding.scrollChat.post(() -> binding.scrollChat.fullScroll(android.view.View.FOCUS_DOWN));
  }
}
