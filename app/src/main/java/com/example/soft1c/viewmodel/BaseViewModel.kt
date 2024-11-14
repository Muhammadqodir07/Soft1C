package com.example.soft1c.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soft1c.R
import com.example.soft1c.network.Network
import com.example.soft1c.repository.BaseRepository
import com.example.soft1c.repository.model.AnyModel
import com.example.soft1c.repository.model.LoadingModel
import com.example.soft1c.utils.SingleLiveEvent
import com.example.soft1c.utils.Utils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.HttpURLConnection

class BaseViewModel(application: Application) : AndroidViewModel(application) {

    private val exceptionScope = CoroutineExceptionHandler { coroutineContext, throwable ->
        //Чтобы узнать на какой строке ошибка

        val stackTrace = throwable.stackTrace.joinToString("\n")
        val lines = stackTrace.split("\n")
        var comExampleSoft1cLine: String? = null
        for (line in lines) {
            if (line.contains("com.example.soft1c")) {
                comExampleSoft1cLine = line
                break
            }
        }
        if (throwable.stackTraceToString().startsWith("java.net.SocketTimeoutException")) {
            toastResIdMutableData.postValue(R.string.socket_timeout_exception)
        }else if (throwable.stackTraceToString().startsWith("java.net.ConnectException")) {
            toastResIdMutableData.postValue(R.string.no_connection_exception)
        }
        else {
            toastMutableData.postValue(
                "$comExampleSoft1cLine\n"
                    ?: "Error: no line containing 'com.example.soft1c' found.\n"
            )
        }
//        toastMutableData.postValue("Error on ${coroutineContext}, text ${throwable.message}")
        Network.refreshConnection(Utils.authorizationTimeout)
    }
    private val repository = BaseRepository(Utils.lang)

    private val authMutableData = MutableLiveData<Boolean>()
    private val loadAuthMutableData = MutableLiveData<Boolean>()

    //переменная типа MutableLiveData, которая будет содержать список машин.
    private val loadingObjectMutableData = MutableLiveData<Pair<Int, List<LoadingModel>>>()
    private val toastMutableData = MutableLiveData<String>()
    private val toastResIdMutableData = MutableLiveData<Int>()
    private val anyObjectMutableData = SingleLiveEvent<Pair<Int, List<AnyModel>>>()

    val authLiveData: LiveData<Boolean>
        get() = authMutableData
    val loadAuthLiveData: LiveData<Boolean>
        get() = loadAuthMutableData
    val toastLiveData: LiveData<String>
        get() = toastMutableData
    val toastResIdLiveData: LiveData<Int>
        get() = toastResIdMutableData
    val anyObjectLiveData: LiveData<Pair<Int, List<AnyModel>>>
        get() = anyObjectMutableData

    //Объявление свойства get(), чтобы предотвратить изменение списка машин, которое будет возвращать carsMutableData
    val loadingObjectLiveData: LiveData<Pair<Int, List<LoadingModel>>>
        get() = loadingObjectMutableData

    fun acceptanceAuth() {
        viewModelScope.launch(exceptionScope + Dispatchers.IO) {
            authMutableData.postValue(repository.getAccessToken())
            delay(1000)
            if (repository.errorCode != 0) {
                when (repository.errorCode) {
                    HttpURLConnection.HTTP_UNAUTHORIZED -> toastResIdMutableData.postValue(R.string.unauthorized)
                    HttpURLConnection.HTTP_NOT_FOUND -> toastResIdMutableData.postValue(R.string.not_found)
                    else -> toastMutableData.postValue("Error code: " + repository.errorCode)
                }
            }
        }
    }

    fun loadingAuth() {
        viewModelScope.launch(exceptionScope + Dispatchers.IO) {
            loadAuthMutableData.postValue(repository.getLoadingToken())
        }

        viewModelScope.launch(Dispatchers.Main) {
            delay(1000) // Add a delay of 2000 milliseconds (2 seconds)
            if (repository.errorCode != 0) {
                when (repository.errorCode) {
                    HttpURLConnection.HTTP_UNAUTHORIZED -> toastResIdMutableData.postValue(R.string.unauthorized)
                    HttpURLConnection.HTTP_NOT_FOUND -> toastResIdMutableData.postValue(R.string.not_found)
                    else -> toastMutableData.postValue("Error code: " + repository.errorCode)
                }
            }
        }
    }

    fun downloadType(type: Int) {
        viewModelScope.launch(exceptionScope + Dispatchers.IO) {
            anyObjectMutableData.postValue(repository.getAnyApi(type))
        }
    }

    //Ассинхронная функция загрузки машин из сервера. Устанавливает список машин в carsMutableData при помощи метода postValue()
    fun loadType(type: Int) {
        viewModelScope.launch(exceptionScope + Dispatchers.IO) {
            loadingObjectMutableData.postValue(repository.getLoading(type))
        }
    }
}