package com.example.wavelengthlocationservices.ui.broker2;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class Broker2ViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public Broker2ViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}