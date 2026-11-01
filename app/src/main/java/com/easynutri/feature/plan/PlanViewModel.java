package com.easynutri.feature.plan;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@HiltViewModel
public class PlanViewModel extends ViewModel {
  private final MutableLiveData<String> title = new MutableLiveData<>("Meal Planning");

  @Inject
  public PlanViewModel() {}

  public LiveData<String> getTitle() {
    return title;
  }
}
