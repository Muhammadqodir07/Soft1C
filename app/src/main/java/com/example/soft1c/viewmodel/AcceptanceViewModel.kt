package com.example.soft1c.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soft1c.repository.AcceptanceRepository
import com.example.soft1c.repository.AcceptanceSizeRepository
import com.example.soft1c.repository.model.Acceptance
import com.example.soft1c.repository.model.AcceptanceEnableVisible
import com.example.soft1c.repository.model.Client
import com.example.soft1c.repository.model.FieldsAccess
import com.example.soft1c.repository.model.SizeAcceptance
import com.example.soft1c.utils.SingleLiveEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class AcceptanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        AcceptanceRepository()
    private val sizeRepository = AcceptanceSizeRepository()

    private val exceptionScope = CoroutineExceptionHandler { coroutineContext, throwable ->
        val stackTrace = throwable.stackTrace.joinToString("\n")
        val lines = stackTrace.split("\n")
        var comExampleSoft1cLine: String? = null
        for (line in lines) {
            if (line.contains("com.example.soft1c")) {
                comExampleSoft1cLine = line
                break
            }
        }
        toastMutableData.postValue(comExampleSoft1cLine ?: "Error: no line containing 'com.example.soft1c' found.")
        //toastMutableData.postValue("Error on $coroutineContext , error message ${throwable.message}")
    }

    private val toastMutableData = SingleLiveEvent<String>()
    private val acceptanceListMutableData = MutableLiveData<List<Acceptance>>()
    private val acceptanceMutableData =
        SingleLiveEvent<Pair<Acceptance, List<AcceptanceEnableVisible>>>()
    private val clientMutableData = SingleLiveEvent<Pair<Client, Boolean>>()
    private val fieldMutableData = SingleLiveEvent<FieldsAccess>()
    private val createUpdateMutableData = SingleLiveEvent<Pair<Acceptance, String>>()
    private val acceptanceSizeMutableData = SingleLiveEvent<SizeAcceptance>()
    private val updateAcceptanceSizeMutableData = SingleLiveEvent<Pair<String, Boolean>>()

    val toastLiveData: LiveData<String>
        get() = toastMutableData

    val acceptanceListLiveData: LiveData<List<Acceptance>>
        get() = acceptanceListMutableData

    val acceptanceLiveData: LiveData<Pair<Acceptance, List<AcceptanceEnableVisible>>>
        get() = acceptanceMutableData

    val clientLiveData: LiveData<Pair<Client, Boolean>>
        get() = clientMutableData

    val fieldLiveData: LiveData<FieldsAccess>
        get() = fieldMutableData

    val createUpdateLiveData: LiveData<Pair<Acceptance, String>>
        get() = createUpdateMutableData

    val acceptanceSizeLiveData: LiveData<SizeAcceptance>
        get() = acceptanceSizeMutableData

    val updateAcceptanceSizeLiveData: LiveData<Pair<String, Boolean>>
        get() = updateAcceptanceSizeMutableData

    fun getAcceptanceList() {
        viewModelScope.launch((exceptionScope + Dispatchers.IO)) {
            acceptanceListMutableData.postValue(repository.getAcceptanceListApi())
        }
    }

    fun getAcceptance(number: String) {
        viewModelScope.launch((exceptionScope + Dispatchers.IO)) {
            acceptanceMutableData.postValue(repository.getAcceptanceApi(number))
        }
    }

    fun getFieldsAccess(guid: String, type: String){
        viewModelScope.launch((exceptionScope+Dispatchers.IO)){
            fieldMutableData.postValue(repository.getFieldsApi(guid, type))
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
}