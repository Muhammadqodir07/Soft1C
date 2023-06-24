package com.example.soft1c.repository

import com.example.soft1c.network.Network
import com.example.soft1c.repository.model.Loading
import com.example.soft1c.repository.model.LoadingEnableVisible
import com.example.soft1c.repository.model.LoadingModel
import com.example.soft1c.utils.Utils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LoadingRepository {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    suspend fun getLoadingApi(number: String): Pair<Loading, List<LoadingEnableVisible>>? {
        return suspendCoroutine { continuation ->
            Network.loadingApi.loading(number).enqueue(object: Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.isSuccessful){
                        val responseBody = response.body()?.string() ?: ""
                        continuation.resume(getLoadingJson(responseBody))
                    } else {
                        continuation.resume(Pair(Loading(""), emptyList()))
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    continuation.resumeWithException(t)
                }

            })
        }
    }

    suspend fun getBarcodeListApi(barcode: String): List<String>? {
        return suspendCoroutine { continuation ->
            Network.loadingApi.barcodeList(barcode).enqueue(object : Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.isSuccessful){
                        val responseBody = response.body()?.string() ?: ""
                        continuation.resume(getBarcodeListJson(responseBody))
                    } else {
                        continuation.resume(emptyList())
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

    private fun getBarcodeListJson(responseBody: String): List<String>? {
        val barcodeList = mutableListOf<String>()
        val arrayJson = JSONArray(responseBody)
        for (item in 0 until arrayJson.length()){
            val objectJson = arrayJson.getJSONObject(item)
            val barcode = objectJson.getString(BARCODE_KEY)
            barcodeList.add(barcode)
        }
        return barcodeList
    }

    suspend fun createUpdateLoadingApi(loading: Loading): Pair<Loading, String>{
        return suspendCoroutine { continuation ->
            val jsonObject = JSONObject()
            if (loading.ref.isNotEmpty())
                jsonObject.put(REF_KEY, loading.ref)
            else
                jsonObject.put(REF_KEY, UUID.randomUUID().toString())
            if (loading.guid.isNotEmpty())
                jsonObject.put(GUID_KEY, loading.guid)
            else
                jsonObject.put(GUID_KEY, null)
            if (loading.date.isNotEmpty()) {
                jsonObject.put(DATE_KEY, loading.date )
            }else{
                jsonObject.put(DATE_KEY, LocalDateTime.now().format(formatter))
            }
            jsonObject.put(CAR_KEY, loading.carUid)
            jsonObject.put(GETTER_WAREHOUSE_UID_KEY, loading.getterWarehUid)
            jsonObject.put(SENDER_WAREHOUSE_UID_KEY, loading.senderWarehUid)
            // Создаем массив товаров и заполняем его штрих-кодами
            val jsonArray = JSONArray()
            for (barcode in loading.barcodes) {
                val barcodeObject = JSONObject()
                barcodeObject.put(BARCODE_KEY, barcode)
                jsonArray.put(barcodeObject)
            }
            // Добавляем массив товаров в объект JSON
            jsonObject.put(ITEMS_KEY, jsonArray)
            val requestBody =
                jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
            Network.loadingApi.createUpdateLoading(requestBody)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            val jsonString = response.body()?.string() ?: ""
                            if (jsonString.isEmpty())
                                continuation.resume(Pair(loading, ""))
                            val ref = JSONArray(jsonString).getJSONObject(0).getString(REF_KEY)
                            val guid = JSONArray(jsonString).getJSONObject(0).getString(GUID_KEY)
                            //LoadingAdapter = ref
                            loading.ref = ref
                            loading.guid = guid
                            continuation.resume(Pair(loading, ""))
                        } else {

                            val errorBody = response.errorBody()?.string()
                            val jsonError =
                                JSONArray(errorBody).getJSONObject(0).getString(ERROR_ARRAY)
                            continuation.resume(Pair(loading, jsonError))
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        continuation.resumeWithException(t)
                    }
                })
        }
    }

    suspend fun getLoadingListApi(): List<Loading> {
        return suspendCoroutine { continuation ->
            Network.loadingApi.loadingList().enqueue(object : Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if(response.isSuccessful){
                        val responseBody = response.body()?.string() ?: ""
                        continuation.resume(getLoadingList(responseBody))
                    }else {
                        continuation.resume(emptyList())
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    continuation.resumeWithException(t)
                }

            })
        }
    }

    private fun getLoadingList(responseString:String): List<Loading> {
        val loadingList = mutableListOf<Loading>()
        val loadingArray = JSONArray(responseString)
        for(index in 0 until loadingArray.length()){
            val loadingJson = loadingArray.getJSONObject(index)
            val loading = getLoadingFromJsonObject(loadingJson, false)
            loadingList.add(loading)
        }
        return loadingList
    }

    private fun getLoadingJson(responseString: String): Pair<Loading, List<LoadingEnableVisible>>? {
        val jsonArray = JSONArray(responseString)
        val jsonObject = jsonArray.getJSONObject(0)
        val loading = getLoadingFromJsonObject(jsonObject, true)
        val enableVisibleList = mutableListOf<LoadingEnableVisible>()
        //val propertyArray = jsonObject.getJSONArray()
        return Pair(loading, enableVisibleList)
    }

    private fun getLoadingFromJsonObject(loadingJson: JSONObject, hasBarcodes: Boolean): Loading {
        val ref = loadingJson.getString(REF_KEY)
        val guid = loadingJson.getString(GUID_KEY)
        val date = loadingJson.getString(DOCUMENT_DATE_KEY)
        val number = loadingJson.getString(NUMBER_KEY)
        val carUid = loadingJson.getString(CAR_NUMBER_KEY)
        val car = getCarNumberFromUid(carUid)
        val senderWarehouseUid = loadingJson.getString(SENDER_WAREHOUSE_UID_KEY)
        val senderWarehouse = loadingJson.getString(SENDER_WAREHOUSE_KEY)
        val getterWarehouseUid = loadingJson.getString(GETTER_WAREHOUSE_UID_KEY)
        val getterWarehouse = loadingJson.getString(GETTER_WAREHOUSE_KEY)
        if (hasBarcodes) {
            val barcodesJsonArray = loadingJson.getJSONArray(ITEMS_KEY)
            val barcodes = mutableListOf<String>()
            for (i in 0 until barcodesJsonArray.length()) {
                barcodes.add(barcodesJsonArray.getString(i))
            }
            return Loading(
                number = number,
                ref = ref,
                guid = guid,
                date = date,
                carUid = carUid,
                car = car,
                senderWarehUid = senderWarehouseUid,
                senderWareh = senderWarehouse,
                getterWarehUid = getterWarehouseUid,
                getterWareh = getterWarehouse,
                barcodes = barcodes
            )
        }else{
            return Loading(
                number = number,
                ref = ref,
                guid = guid,
                date = date,
                carUid = carUid,
                senderWarehUid = senderWarehouseUid,
                senderWareh = senderWarehouse,
                getterWarehUid = getterWarehouseUid,
                getterWareh = getterWarehouse,
                car = car
            )
        }
    }

    private fun getCarNumberFromUid(carUid: String): String {
        val elem = Utils.cars.find {
            (it as LoadingModel.Car).ref == carUid
        } ?: return ""
        return (elem as LoadingModel.Car).number
    }


    companion object {
        const val REF_KEY = "GUID"
        const val GUID_KEY = "GUIDНаСервере"
        const val CAR_KEY = "Машина"
        const val SENDER_WAREHOUSE_UID_KEY = "Отправитель"
        const val SENDER_WAREHOUSE_KEY = "ОтправительНаименование"
        const val GETTER_WAREHOUSE_UID_KEY = "Получатель"
        const val GETTER_WAREHOUSE_KEY = "ПолучательНаименование"
        const val DATE_KEY = "Дата"
        const val DOCUMENT_DATE_KEY = "ДатаДокумента"
        const val NUMBER_KEY= "Номер"
        const val CAR_NUMBER_KEY = "НомерМашины"
        const val ITEMS_KEY = "Товары"
        const val BARCODE_KEY = "ШтрихКод"
        const val ERROR_ARRAY = "ПричинаОшибки"
    }
}



