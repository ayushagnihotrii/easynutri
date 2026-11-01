package com.easynutri.feature.find;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.easynutri.R;
import com.easynutri.databinding.FragmentPlaceholderBinding;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FindFragment extends Fragment {

  private FragmentPlaceholderBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentPlaceholderBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    binding.tvPlaceholderTitle.setText(
        getString(R.string.placeholder_scaffolding_title, "2", getString(R.string.nav_find)));
    binding.tvPlaceholderDesc.setText(getString(R.string.placeholder_scaffolding_desc, "2"));
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
