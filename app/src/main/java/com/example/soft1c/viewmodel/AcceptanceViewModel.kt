package com.example.soft1c.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soft1c.R
import com.example.soft1c.network.Network
import com.example.soft1c.repository.AcceptanceRepository
import com.example.soft1c.repository.AcceptanceSizeRepository
import com.example.soft1c.repository.model.Acceptance
import com.example.soft1c.repository.model.Client
import com.example.soft1c.repository.model.FieldsAccess
import com.example.soft1c.repository.model.SizeAcceptance
import com.example.soft1c.utils.SingleLiveEvent
import com.example.soft1c.utils.Utils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class AcceptanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        AcceptanceRepository()
    private val sizeRepository = AcceptanceSizeRepository()

    private val exceptionScope = CoroutineExceptionHandler { coroutineContext, throwable ->
//        val stackTrace = throwable.stackTrace.joinToString("\n")
//        val lines = stackTrace.split("\n")
//        var comExampleSoft1cLine: String? = null
//        for (line in lines) {
//            if (line.contains("com.example.soft1c")) {
//                comExampleSoft1cLine = line
//                break
//            }
//        }
//        toastMutableData.postValue(comExampleSoft1cLine ?: "Error: no line containing 'com.example.soft1c' found.")
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
    private val acceptanceListMutableData = MutableLiveData<List<Acceptance>>()
    private val acceptanceMutableData =
        SingleLiveEvent<Triple<Acceptance, FieldsAccess, String>>()
    private val clientMutableData = SingleLiveEvent<Pair<Client, Boolean>>()
    private val createUpdateMutableData = SingleLiveEvent<Pair<Acceptance, String>>()
    private val acceptanceSizeMutableData = SingleLiveEvent<Pair<SizeAcceptance, String>>()
    private val updateAcceptanceSizeMutableData = SingleLiveEvent<Pair<String, Boolean>>()
    private val logSendingResultMutableData = SingleLiveEvent<Boolean>()

    val toastLiveData: LiveData<String>
        get() = toastMutableData
    val toastResIdLiveData: LiveData<Int>
        get() = toastResIdMutableData

    val acceptanceListLiveData: LiveData<List<Acceptance>>
        get() = acceptanceListMutableData

    val acceptanceLiveData: LiveData<Triple<Acceptance, FieldsAccess, String>>
        get() = acceptanceMutableData

    val clientLiveData: LiveData<Pair<Client, Boolean>>
        get() = clientMutableData

    val createUpdateLiveData: LiveData<Pair<Acceptance, String>>
        get() = createUpdateMutableData

    val acceptanceSizeLiveData: LiveData<Pair<SizeAcceptance, String>>
        get() = acceptanceSizeMutableData

    val updateAcceptanceSizeLiveData: LiveData<Pair<String, Boolean>>
        get() = updateAcceptanceSizeMutableData

    val logSendingResultLiveData: LiveData<Boolean>
        get() = logSendingResultMutableData

    fun getAcceptanceList() {
        viewModelScope.launch((exceptionScope + Dispatchers.IO)) {
            acceptanceListMutableData.postValue(repository.getAcceptanceListApi())
        }
    }

    fun getAcceptance(number: String, operation: String) {
        viewModelScope.launch((exceptionScope + Dispatchers.IO)) {
            acceptanceMutableData.postValue(repository.getAcceptanceApi(number, operation))
        }
    }

    fun getClient(clientCode: String) {
        viewModelScope.launch((exceptionScope + Dispatchers.IO)) {
            clientMutableData.postValue(repository.getClientApi(clientCode))
        }
    }

    fun createUpdateAcceptance(acceptance: Acceptance) {
        viewModelScope.launch((exceptionScope + Dispatchers.IO)) {
            createUpdateMutableData.postValue(repository.createUpdateAccApi(acceptance))
        }
    }


    fun getAcceptanceSizeData(acceptanceGuid: String) {
        viewModelScope.launch((exceptionScope + Dispatchers.IO)) {
            acceptanceSizeMutableData.postValue(sizeRepository.getSizeDataApi(acceptanceGuid))
        }
    }

    fun updateAcceptanceSize(acceptanceGuid: String, acceptance: SizeAcceptance) {
        viewModelScope.launch((exceptionScope + Dispatchers.IO)) {
            updateAcceptanceSizeMutableData.postValue(
                sizeRepository.updateSizeDataApi(
                    acceptanceGuid,
                    acceptance
                )
            )
        }

    }

    fun sendLogs() {
        viewModelScope.launch((exceptionScope + Dispatchers.IO)) {
            logSendingResultMutableData.postValue(repository.sendLogs(Utils.logFor1C))
        }
    }
}