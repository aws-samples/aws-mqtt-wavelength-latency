package com.example.wavelengthlocationservices.ui.broker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BrokerViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public BrokerViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}