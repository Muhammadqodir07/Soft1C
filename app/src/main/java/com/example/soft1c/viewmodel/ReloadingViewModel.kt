package com.example.soft1c.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soft1c.R
import com.example.soft1c.network.Network
import com.example.soft1c.repository.ReloadingRepository
import com.example.soft1c.repository.model.Loading
import com.example.soft1c.repository.model.LoadingBarcode
import com.example.soft1c.repository.model.LoadingEnableVisible
import com.example.soft1c.utils.SingleLiveEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReloadingViewModel(application: Application): AndroidViewModel(application) {
    private val repository = ReloadingRepository()

    private val exceptionScope = CoroutineExceptionHandler { coroutineContext, throwable ->
        if (throwable.stackTraceToString().startsWith("java.net.SocketTimeoutException")) {
            toastResIdMutableData.postValue(R.string.socket_timeout_exception)
        } else if (throwable.stackTraceToString().startsWith("java.net.ConnectException")) {
            toastResIdMutableData.postValue(R.string.no_connection_exception)
        } else {
            toastMutableData.postValue("Error on $coroutineContext , error message ${throwable.localizedMessage}")
        }
        Network.refreshConnection()
    }

    private val toastMutableData = SingleLiveEvent<String>()
    private val toastResIdMutableData = SingleLiveEvent<Int>()
    private val createUpdateMutableData = SingleLiveEvent<Pair<Loading, String>>()
    private val reloadingListMutableData = MutableLiveData<List<Loading>>()
    private val barcodeListMutableData = MutableLiveData<List<LoadingBarcode>>()
    private val reloadingMutableData = SingleLiveEvent<Pair<Loading, List<LoadingEnableVisible>>>()

    val toastLiveDat: LiveData<String>
        get() = toastMutableData
    val toastResIdLiveData: LiveData<Int>
        get() = toastResIdMutableData
    val createUpdateLiveDat: LiveData<Pair<Loading, String>>
        get() = createUpdateMutableData
    val barcodeListLiveData: LiveData<List<LoadingBarcode>>
        get() = barcodeListMutableData
    val reloadingLiveData: LiveData<Pair<Loading, List<LoadingEnableVisible>>>
        get() = reloadingMutableData
    val reloadingListLiveData: LiveData<List<Loading>>
        get() = reloadingListMutableData

    fun createUpdateLoading(loading: Loading) {
        viewModelScope.launch((exceptionScope + Dispatchers.IO)) {
            createUpdateMutableData.postValue(repository.createUpdateLoadingApi(loading))
        }
    }

    fun getReloadingList() {
        viewModelScope.launch((exceptionScope+ Dispatchers.IO)){
            reloadingListMutableData.postValue(repository.getReloadingListApi())
        }
    }

    fun getReloading(number: String){
        viewModelScope.launch((exceptionScope + Dispatchers.IO)){
            reloadingMutableData.postValue(repository.getReloadingApi(number))
        }
    }

    fun getBarcodeList(code: String, warehouse: String){
        viewModelScope.launch((exceptionScope+ Dispatchers.IO)){
            barcodeListMutableData.postValue(repository.getBarcodeListApi(code, warehouse))
        }
    }
}