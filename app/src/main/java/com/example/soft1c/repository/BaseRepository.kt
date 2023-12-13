package com.example.soft1c.repository

import com.example.soft1c.network.Network
import com.example.soft1c.repository.model.AnyModel
import com.example.soft1c.repository.model.LoadingModel
import com.example.soft1c.repository.model.User
import com.example.soft1c.utils.Utils
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BaseRepository(private val lang: String) {

    var errorCode = 0

    suspend fun getAccessToken(): Boolean {
        return suspendCoroutine { continuation ->
            Network.Api.auto()
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>,
                    ) {
                        //Если ответ есть то авторизовать
                        if (response.isSuccessful) {
                            val body = response.body()?.string() ?: ""
                            continuation.resume(getRights(body))
                            return
                        }
                        errorCode = response.code()
                        continuation.resume(false)
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Timber.d("error ${t.message}")
                        continuation.resumeWithException(t)
                    }
                })
        }
    }

    suspend fun getLoadingToken(): Boolean {
        return suspendCoroutine { continuation ->
            Network.loadingApi.loadAuth()
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>,
                    ) {
                        //Если ответ есть то авторизовать
                        if (response.isSuccessful) {
                            continuation.resume(true)
                            return
                        }
                        errorCode = response.code()
                        continuation.resume(false)
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Timber.d("error ${t.message}")
                        continuation.resumeWithException(t)
                    }
                })
        }
    }

    suspend fun getAnyApi(type: Int): Pair<Int, List<AnyModel>> {
        return suspendCoroutine { continuation ->
            when (type) {
                Utils.ObjectModelType.ADDRESS -> Network.api.addressList()
                Utils.ObjectModelType._PACKAGE -> Network.api.packageList()
                Utils.ObjectModelType.PRODUCT_TYPE -> Network.api.productTypeList()
                Utils.ObjectModelType.ZONE -> Network.api.zoneList()
                else -> Network.api.addressList()
            }
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>,
                    ) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()?.string() ?: ""
                            continuation.resume(Pair(type, getAddressListJson(responseBody, type)))
                        } else {
                            continuation.resume(Pair(Utils.ObjectModelType.EMPTY, emptyList()))
                        }

                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        continuation.resumeWithException(t)
                    }
                })
        }
    }


    //Приостанавливаемая функция suspend. Блокирует вызывающий поток до тех пор, пока результат не будет получен.
    suspend fun getLoading(type: Int): Pair<Int, List<LoadingModel>> {
        return suspendCoroutine { continuation ->
            //вызов API сети для асинхронного получения списка машин.
            when (type) {
                Utils.ObjectModelType.CAR -> Network.loadingApi.carsList()
                Utils.ObjectModelType.WAREHOUSE -> Network.loadingApi.warehouseList()
                else -> Network.loadingApi.carsList()
            }.enqueue(object : Callback<ResponseBody> {
                //При получении ответа от сервера.
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    //Если ответ успешен, получает тело ответа и передает его в функцию getCarsListJson()
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string() ?: ""
                        continuation.resume(Pair(type, getLoadModelListJson(responseBody, type)))
                    }
                    //Иначе вызывает continuation.resumeWithException(), передавая исключение Throwable("Failed to fetch cars list").
                    else {
                        continuation.resume(Pair(Utils.ObjectModelType.EMPTY, emptyList()))
                    }
                }

                //ПриОшибке во время запроса. Вызывает continuation.resumeWithException(), передавая исключение Throwable.
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })

        }
    }

    private fun getLoadModelListJson(responseBody: String, type: Int): List<LoadingModel>{
        val carList = mutableListOf<LoadingModel>()
        val arrayJson = JSONArray(responseBody)
        for (item in 0 until arrayJson.length()){
            val objectJson = arrayJson.getJSONObject(item)
            val ref = objectJson.getString(Utils.Cars.REF_KEY)
            val name = when (type){
                Utils.ObjectModelType.CAR -> objectJson.getString(Utils.Cars.NUMBER_KEY)
                else -> objectJson.getString(Utils.Contracts.NAME_KEY)

            }
            val loadingObject = when (type){
                Utils.ObjectModelType.CAR -> LoadingModel.Car(ref, name)
                Utils.ObjectModelType.WAREHOUSE -> LoadingModel.Warehouse(ref, name)
                else -> LoadingModel.Car(ref, name)
            }
            carList.add(loadingObject)
        }
        return carList
    }

    private fun getRights(responseBody: String): Boolean {
        val jsonObject = JSONObject(responseBody)
        val priemkiAccess = jsonObject.optBoolean(ACCEPTANCE_RIGHT, false)
        val pogruzkiAccess = jsonObject.optBoolean(LOADING_RIGHT, false)
        val isAdmin = jsonObject.optBoolean(IS_ADMIN, false)
        Utils.Settings.passportClientControl = jsonObject.optBoolean(CLIENT_CONTROL, false)
        if (isAdmin){
            Utils.user = User(
                username = Utils.username,
                password = Utils.password,
                isAdmin = true,
                weightAccess = true,
                measureCargo = true,
                acceptanceCargo = true
            )
        } else {
            val acceptanceAdd = jsonObject.optBoolean(ACCEPTANCE_ADD, false)
            val weightAdd = jsonObject.optBoolean(WEIGHT_ADD, false)
            val sizeAdd = jsonObject.optBoolean(SIZE_ADD, false)
            Utils.user = User(
                username = Utils.username,
                password = Utils.password,
                isAdmin = false,
                weightAccess = weightAdd,
                measureCargo = sizeAdd,
                acceptanceCargo = acceptanceAdd
            )
        }

        return true
    }



    private fun getAddressListJson(responseBody: String, type: Int): List<AnyModel> {
        val addressList = mutableListOf<AnyModel>()
        val arrayJson = JSONArray(responseBody)
        for (item in 0 until arrayJson.length()) {
            val objectJson = arrayJson.getJSONObject(item)
            val ref = objectJson.getString(Utils.Contracts.REF_KEY)
            val name = when (type) {
                Utils.ObjectModelType._PACKAGE -> {
                    val addLang = if (lang == "chinese") AcceptanceRepository.ON_CHINESE else ""
                    objectJson.getString(Utils.Contracts.NAME_KEY + addLang)
                }
                Utils.ObjectModelType.PRODUCT_TYPE ->{
                    val addLang = if (lang == "chinese") AcceptanceRepository.ON_CHINESE else ""
                    objectJson.getString(Utils.Contracts.NAME_KEY + addLang)
                }
                else -> objectJson.getString(Utils.Contracts.NAME_KEY)
            }
            val code = objectJson.getString(Utils.Contracts.CODE_KEY)
            val anyObject = when (type) {
                Utils.ObjectModelType.ADDRESS -> AnyModel.AddressModel(ref, name, code)
                Utils.ObjectModelType._PACKAGE -> AnyModel.PackageModel(ref, name, code)
                Utils.ObjectModelType.PRODUCT_TYPE -> AnyModel.ProductType(ref, name, code)
                Utils.ObjectModelType.ZONE -> AnyModel.Zone(ref, name, code)
                else -> AnyModel.AddressModel(ref, name, code)
            }
            addressList.add(anyObject)
        }
        if (type == Utils.ObjectModelType._PACKAGE) {
            addressList.sortBy { it ->
                when (it) {
                    is AnyModel.PackageModel -> it.name.filter { it.isDigit() }.toIntOrNull()
                    else -> null
                }
            }
        }
        return addressList
    }


    companion object{
        const val ACCEPTANCE_RIGHT = "PriemkiAccess"
        const val LOADING_RIGHT = "PogruzkiAccess"
        const val ACCEPTANCE_ADD= "PriemkiCargo"
        const val SIZE_ADD = "MeasureCargo"
        const val WEIGHT_ADD = "Weighing"
        const val IS_ADMIN = "ЭтоАдмин"
        const val CLIENT_CONTROL = "КонтрольКлиентаПоПаспорту"
    }
}