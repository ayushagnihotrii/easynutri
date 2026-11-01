package com.easynutri.feature.assistant;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@HiltViewModel
public class AssistantViewModel extends ViewModel {
  private final MutableLiveData<String> title = new MutableLiveData<>("AI Assistant");

  @Inject
  public AssistantViewModel() {}

  public LiveData<String> getTitle() {
    return title;
  }
}
