package com.easynutri.feature.find;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@HiltViewModel
public class FindViewModel extends ViewModel {
  private final MutableLiveData<String> title = new MutableLiveData<>("Finding Food");

  @Inject
  public FindViewModel() {}

  public LiveData<String> getTitle() {
    return title;
  }
}
