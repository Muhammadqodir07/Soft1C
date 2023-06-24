package com.example.soft1c.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soft1c.repository.LoadingRepository
import com.example.soft1c.repository.model.Loading
import com.example.soft1c.repository.model.LoadingEnableVisible
import com.example.soft1c.utils.SingleLiveEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoadingViewModel(application: Application): AndroidViewModel(application) {
    private val repository = LoadingRepository()

    private val exceptionScope = CoroutineExceptionHandler { coroutineContext, throwable ->
        toastMutableData.postValue("Error on $coroutineContext , error message ${throwable.message}")
    }

    private val toastMutableData = SingleLiveEvent<String>()
    private val createUpdateMutableData = SingleLiveEvent<Pair<Loading, String>>()
    private val loadingListMutableData = MutableLiveData<List<Loading>>()
    private val barcodeListMutableData = MutableLiveData<List<String>>()
    private val loadingMutableData = SingleLiveEvent<Pair<Loading, List<LoadingEnableVisible>>>()

    val toastLiveDat: LiveData<String>
        get() = toastMutableData
    val createUpdateLiveDat: LiveData<Pair<Loading, String>>
        get() = createUpdateMutableData
    val loadingListLiveData: LiveData<List<Loading>>
        get() = loadingListMutableData
    val barcodeListLiveData: LiveData<List<String>>
        get() = barcodeListMutableData
    val loadingLiveData: LiveData<Pair<Loading, List<LoadingEnableVisible>>>
        get() = loadingMutableData

    fun createUpdateLoading(loading: Loading) {
        viewModelScope.launch((exceptionScope + Dispatchers.IO)) {
            createUpdateMutableData.postValue(repository.createUpdateLoadingApi(loading))
        }
    }

    fun getLoadingList() {
        viewModelScope.launch((exceptionScope+Dispatchers.IO)){
            loadingListMutableData.postValue(repository.getLoadingListApi())
        }
    }

    fun getLoading(number: String){
        viewModelScope.launch((exceptionScope + Dispatchers.IO)){
            loadingMutableData.postValue(repository.getLoadingApi(number))
        }
    }

    fun getBarcodeList(barcode: String){
        viewModelScope.launch((exceptionScope+Dispatchers.IO)){
            barcodeListMutableData.postValue(repository.getBarcodeListApi(barcode))
        }
    }
}