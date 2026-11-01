package com.easynutri.feature.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.easynutri.core.db.proof.ProofDao;
import com.easynutri.core.db.proof.ProofEntity;
import dagger.hilt.android.lifecycle.HiltViewModel;
import java.util.List;
import javax.inject.Inject;

@HiltViewModel
public class HomeViewModel extends ViewModel {

  private final ProofDao proofDao;
  private final MutableLiveData<String> greeting = new MutableLiveData<>("Namaste, User");

  @Inject
  public HomeViewModel(ProofDao proofDao) {
    this.proofDao = proofDao;
  }

  public LiveData<String> getGreeting() {
    return greeting;
  }

  public LiveData<List<ProofEntity>> getProofRecords() {
    return proofDao.getAllRecordsLive();
  }
}
