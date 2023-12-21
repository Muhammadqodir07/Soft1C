package com.example.soft1c.repository

import com.example.soft1c.network.Network
import com.example.soft1c.repository.model.Loading
import com.example.soft1c.repository.model.Loading.Companion.CAR_KEY
import com.example.soft1c.repository.model.Loading.Companion.CAR_NUMBER_KEY
import com.example.soft1c.repository.model.Loading.Companion.DATE_KEY
import com.example.soft1c.repository.model.Loading.Companion.DOCUMENT_DATE_KEY
import com.example.soft1c.repository.model.Loading.Companion.GETTER_WAREHOUSE_KEY
import com.example.soft1c.repository.model.Loading.Companion.GETTER_WAREHOUSE_UID_KEY
import com.example.soft1c.repository.model.Loading.Companion.GUID_KEY
import com.example.soft1c.repository.model.Loading.Companion.ITEMS_BACK_KEY
import com.example.soft1c.repository.model.Loading.Companion.ITEMS_FRONT_KEY
import com.example.soft1c.repository.model.Loading.Companion.NUMBER_KEY
import com.example.soft1c.repository.model.Loading.Companion.RECIPIENT_KEY
import com.example.soft1c.repository.model.Loading.Companion.REF_KEY
import com.example.soft1c.repository.model.Loading.Companion.SENDER_WAREHOUSE_KEY
import com.example.soft1c.repository.model.Loading.Companion.SENDER_WAREHOUSE_UID_KEY
import com.example.soft1c.repository.model.Loading.Companion.WAREHOUSE_BEGIN_KEY
import com.example.soft1c.repository.model.Loading.Companion.WAREHOUSE_END_KEY
import com.example.soft1c.repository.model.LoadingBarcode
import com.example.soft1c.repository.model.LoadingBarcode.Companion.ACCEPTANCE_GUID_KEY
import com.example.soft1c.repository.model.LoadingBarcode.Companion.ACCEPTANCE_NUMBER_KEY
import com.example.soft1c.repository.model.LoadingBarcode.Companion.BARCODE_KEY
import com.example.soft1c.repository.model.LoadingBarcode.Companion.SEAT_NUMBER_KEY
import com.example.soft1c.repository.model.LoadingBarcode.Companion.VOLUME_KEY
import com.example.soft1c.repository.model.LoadingBarcode.Companion.WEIGHT_KEY
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
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LoadingRepository {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    suspend fun getLoadingApi(number: String): Pair<Loading, List<LoadingEnableVisible>>? {
        return suspendCoroutine { continuation ->
            Network.loadingApi.loading(number).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
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

    suspend fun getBarcodeListApi(barcode: String, warehouse: String): List<LoadingBarcode>? {
        return suspendCoroutine { continuation ->
            if (Utils.debugMode) {
                continuation.resume(getBarcodeListJson(LoadingBarcode.DEFAULT_DATA))
            } else {
                Network.loadingApi.barcodeList(barcode, warehouse)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.isSuccessful) {
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
    }

    fun getBarcodeListJson(responseBody: String): List<LoadingBarcode>? {
        val barcodeList = mutableListOf<LoadingBarcode>()
        try {

            val arrayJson = JSONArray(responseBody)
            for (item in 0 until arrayJson.length()) {
                val objectJson = arrayJson.getJSONObject(item)
                barcodeList.add(
                    LoadingBarcode(
                        barcode = objectJson.optString(BARCODE_KEY, ""),
                        weight = objectJson.optDouble(WEIGHT_KEY, 0.0),
                        volume = objectJson.optDouble(VOLUME_KEY, 0.0),
                        acceptanceNumber = objectJson.optString(ACCEPTANCE_NUMBER_KEY, ""),
                        acceptanceUid = objectJson.optString(ACCEPTANCE_GUID_KEY, ""),
                        seatNumber = objectJson.optInt(SEAT_NUMBER_KEY, -1),
                    )
                )
            }
            return barcodeList
        } catch (e: Exception) {
            return null
        }
    }

    suspend fun createUpdateLoadingApi(loading: Loading): Pair<Loading, String> {
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
                jsonObject.put(DATE_KEY, loading.date)
            } else {
                jsonObject.put(DATE_KEY, LocalDateTime.now().format(formatter))
            }
            jsonObject.put(CAR_KEY, loading.car.ref)
            jsonObject.put(WAREHOUSE_BEGIN_KEY, loading.senderWarehouseUid)
            jsonObject.put(WAREHOUSE_END_KEY, loading.getterWarehouseUid)
            jsonObject.put(RECIPIENT_KEY, loading.recipient)
            // Создаем массив товаров и заполняем его штрих-кодами
            val frontBarcodeArray = JSONArray()
            for (barcode in loading.barcodesFront) {
                val barcodeObject = JSONObject()
                barcodeObject.put(BARCODE_KEY, barcode)
                frontBarcodeArray.put(barcodeObject)
            }
            jsonObject.put(ITEMS_FRONT_KEY, frontBarcodeArray)
            val backBarcodeArray = JSONArray()
            for (barcode in loading.barcodesBack) {
                val barcodeObject = JSONObject()
                barcodeObject.put(BARCODE_KEY, barcode)
                backBarcodeArray.put(barcodeObject)
            }
            // Добавляем массив товаров в объект JSON
            jsonObject.put(ITEMS_BACK_KEY, backBarcodeArray)
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
            Network.loadingApi.loadingList().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string() ?: ""
                        continuation.resume(getLoadingList(responseBody))
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

    private fun getLoadingList(responseString: String): List<Loading> {
        val loadingList = mutableListOf<Loading>()
        val loadingArray = JSONArray(responseString)
        for (index in 0 until loadingArray.length()) {
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
            val barcodesJsonArray = loadingJson.getJSONArray(ITEMS_FRONT_KEY)
            val barcodes = mutableListOf<LoadingBarcode>()
            for (i in 0 until barcodesJsonArray.length()) {
                val jsonObject = barcodesJsonArray.getJSONObject(i)
                barcodes.add(
                    LoadingBarcode(
                        barcode = jsonObject.getString(BARCODE_KEY)
                    )
                )
            }
            return Loading(
                number = number,
                ref = ref,
                guid = guid,
                date = date,
                car = car,
                senderWarehouseUid = senderWarehouseUid,
                senderWarehouse = senderWarehouse,
                getterWarehouseUid = getterWarehouseUid,
                getterWarehouse = getterWarehouse,
                barcodesFront = barcodes
            )
        } else {
            return Loading(
                number = number,
                ref = ref,
                guid = guid,
                date = date,
                senderWarehouseUid = senderWarehouseUid,
                senderWarehouse = senderWarehouse,
                getterWarehouseUid = getterWarehouseUid,
                getterWarehouse = getterWarehouse,
                car = car
            )
        }
    }

    private fun getCarNumberFromUid(carUid: String): LoadingModel.Car {
        val elem = Utils.cars.find {
            (it as LoadingModel.Car).ref == carUid
        } ?: return LoadingModel.Car()
        return (elem as LoadingModel.Car)
    }


    companion object {
        const val ERROR_ARRAY = "ПричинаОшибки"
    }
}



