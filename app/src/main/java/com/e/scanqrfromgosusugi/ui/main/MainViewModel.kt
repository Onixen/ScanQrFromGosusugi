package com.e.scanqrfromgosusugi.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val scanQrResult: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val refresh: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
}