package com.easynutri.feature.track;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

@HiltViewModel
public class TrackViewModel extends ViewModel {
  private final MutableLiveData<String> title = new MutableLiveData<>("Tracking");

  @Inject
  public TrackViewModel() {}

  public LiveData<String> getTitle() {
    return title;
  }
}
