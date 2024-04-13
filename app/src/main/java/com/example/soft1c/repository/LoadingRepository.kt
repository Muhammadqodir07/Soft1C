package com.example.soft1c.repository

import com.example.soft1c.adapter.LoadingAdapter
import com.example.soft1c.network.Network
import com.example.soft1c.repository.model.Loading
import com.example.soft1c.repository.model.Loading.Companion.CAR_KEY
import com.example.soft1c.repository.model.Loading.Companion.CAR_NUMBER_KEY
import com.example.soft1c.repository.model.Loading.Companion.DATE_KEY
import com.example.soft1c.repository.model.Loading.Companion.DOCUMENT_DATE_KEY
import com.example.soft1c.repository.model.Loading.Companion.GUID_KEY
import com.example.soft1c.repository.model.Loading.Companion.ITEMS_BACK_KEY
import com.example.soft1c.repository.model.Loading.Companion.ITEMS_FRONT_KEY
import com.example.soft1c.repository.model.Loading.Companion.NUMBER_KEY
import com.example.soft1c.repository.model.Loading.Companion.REF_KEY
import com.example.soft1c.repository.model.Loading.Companion.WAREHOUSE_BEGIN_KEY
import com.example.soft1c.repository.model.Loading.Companion.WAREHOUSE_END_KEY
import com.example.soft1c.repository.model.LoadingBarcode
import com.example.soft1c.repository.model.LoadingBarcode.Companion.ACCEPTANCE_GUID_KEY
import com.example.soft1c.repository.model.LoadingBarcode.Companion.ACCEPTANCE_NUMBER_KEY
import com.example.soft1c.repository.model.LoadingBarcode.Companion.BARCODE_KEY
import com.example.soft1c.repository.model.LoadingBarcode.Companion.CLIENT_CODE_KEY
import com.example.soft1c.repository.model.LoadingBarcode.Companion.PACKAGE_KEY
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

open class LoadingRepository {
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
                            }else if (response.code() == 502) {
                                continuation.resume(emptyList())
                                //TODO()
                            }
                            else {
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
                val packageUid = objectJson.optString(PACKAGE_KEY, "")
                val packages = AcceptanceRepository().getPackageNameFromUid(packageUid)
                barcodeList.add(
                    LoadingBarcode(
                        barcode = objectJson.optString(BARCODE_KEY, ""),
                        weight = objectJson.optDouble(WEIGHT_KEY, 0.0),
                        volume = objectJson.optDouble(VOLUME_KEY, 0.0),
                        acceptanceNumber = objectJson.optString(ACCEPTANCE_NUMBER_KEY, ""),
                        acceptanceUid = objectJson.optString(ACCEPTANCE_GUID_KEY, ""),
                        seatNumber = objectJson.optInt(SEAT_NUMBER_KEY, -1),
                        packageTypeUid = packageUid,
                        packageType = packages,
                        clientCode = objectJson.optString(CLIENT_CODE_KEY, ""),
                        date = objectJson.optString(LoadingBarcode.DATE_KEY, "")
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
            jsonObject.put(WAREHOUSE_BEGIN_KEY, loading.senderWarehouse.ref)
            jsonObject.put(WAREHOUSE_END_KEY, loading.getterWarehouse.ref)
            // Создаем массив товаров и заполняем его штрих-кодами
            val frontBarcodeArray = JSONArray()
            for (barcode in loading.barcodesFront) {
                val barcodeObject = JSONObject()
                barcodeObject.put(BARCODE_KEY, barcode.barcode)
                frontBarcodeArray.put(barcodeObject)
            }
            jsonObject.put(ITEMS_FRONT_KEY, frontBarcodeArray)
            val backBarcodeArray = JSONArray()
            for (barcode in loading.barcodesBack) {
                val barcodeObject = JSONObject()
                barcodeObject.put(BARCODE_KEY, barcode.barcode)
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
                            LoadingAdapter.LOADING_GUID = ref
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
            if (Utils.debugMode) {
                continuation.resume(getLoadingList(Loading.DEFAULT_DATA_LIST))
            } else {
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
    }

    fun getLoadingList(responseString: String): List<Loading> {
        val loadingList = mutableListOf<Loading>()
        val loadingArray = JSONArray(responseString)
        for (index in 0 until loadingArray.length()) {
            val loadingJson = loadingArray.getJSONObject(index)
            val loading = getLoadingFromJsonObject(loadingJson, false)
            loadingList.add(loading)
        }
        return loadingList
    }

    fun getLoadingJson(responseString: String): Pair<Loading, List<LoadingEnableVisible>>? {
        val jsonArray = JSONArray(responseString)
        val jsonObject = jsonArray.getJSONObject(0)
        val loading = getLoadingFromJsonObject(jsonObject, true)
        val enableVisibleList = mutableListOf<LoadingEnableVisible>()
        //val propertyArray = jsonObject.getJSONArray()
        return Pair(loading, enableVisibleList)
    }

    private fun getLoadingFromJsonObject(loadingJson: JSONObject, hasBarcodes: Boolean): Loading {
        val ref = loadingJson.optString(REF_KEY, "")
        val guid = loadingJson.getString(GUID_KEY)
        val number = loadingJson.getString(NUMBER_KEY)
        val senderWarehouseUid = loadingJson.getString(WAREHOUSE_BEGIN_KEY)
        val getterWarehouseUid = loadingJson.getString(WAREHOUSE_END_KEY)
        val sender = getWarehouseFromUid(senderWarehouseUid)
        val getter = getWarehouseFromUid(getterWarehouseUid)
        if (hasBarcodes) {
            val barcodesFrontJson = loadingJson.getJSONArray(ITEMS_FRONT_KEY)
            val date = loadingJson.getString(DATE_KEY)
            val carUid = loadingJson.getString(CAR_KEY)
            val car = getCarNumberFromUid(carUid)
            val barcodesFront = mutableListOf<LoadingBarcode>()
            for (i in 0 until barcodesFrontJson.length()) {
                val jsonObject = barcodesFrontJson.getJSONObject(i)
                val packageUid = jsonObject.getString(PACKAGE_KEY)
                barcodesFront.add(
                    LoadingBarcode(
                        barcode = jsonObject.getString(Loading.BARCODE_KEY),
                        weight = jsonObject.getDouble(WEIGHT_KEY),
                        volume = jsonObject.getDouble(VOLUME_KEY),
                        clientCode = jsonObject.getString(CLIENT_CODE_KEY),
                        packageTypeUid = packageUid,
                        packageType = AcceptanceRepository().getPackageNameFromUid(packageUid),
                        acceptanceUid = jsonObject.getString(ACCEPTANCE_GUID_KEY),
                        date = jsonObject.getString("ДатаПриема")
                    )
                )
            }
            val barcodesBackJson = loadingJson.getJSONArray(ITEMS_BACK_KEY)
            val barcodesBack = mutableListOf<LoadingBarcode>()
            for (i in 0 until barcodesBackJson.length()) {
                val jsonObject = barcodesBackJson.getJSONObject(i)
                val packageUid = jsonObject.getString(PACKAGE_KEY)
                barcodesBack.add(
                    LoadingBarcode(
                        barcode = jsonObject.getString(Loading.BARCODE_KEY),
                        weight = jsonObject.getDouble(WEIGHT_KEY),
                        volume = jsonObject.getDouble(VOLUME_KEY),
                        clientCode = jsonObject.getString(CLIENT_CODE_KEY),
                        packageTypeUid = packageUid,
                        packageType = AcceptanceRepository().getPackageNameFromUid(packageUid),
                        acceptanceUid = jsonObject.getString(ACCEPTANCE_GUID_KEY),
                        date = jsonObject.getString("ДатаПриема")
                    )
                )
            }
            return Loading(
                number = number,
                ref = ref,
                guid = guid,
                date = date,
                car = car,
                senderWarehouse = sender,
                getterWarehouse = getter,
                barcodesFront = barcodesFront,
                barcodesBack = barcodesBack
            )
        } else {
            val carUid = loadingJson.getString(CAR_NUMBER_KEY)
            val car = getCarNumberFromUid(carUid)
            val date = loadingJson.getString(DOCUMENT_DATE_KEY)
            return Loading(
                number = number,
                ref = ref,
                guid = guid,
                date = date,
                senderWarehouse = sender,
                getterWarehouse = getter,
                car = car
            )
        }
    }

    fun getCarNumberFromUid(carUid: String): LoadingModel.Car {
        val elem = Utils.cars.find {
            (it as LoadingModel.Car).ref == carUid
        } ?: return LoadingModel.Car()
        return (elem as LoadingModel.Car)
    }

    fun getWarehouseFromUid(warehouseUid: String): LoadingModel.Warehouse {
        val elem = Utils.warehouse.find {
            (it as LoadingModel.Warehouse).ref == warehouseUid
        } ?: return LoadingModel.Warehouse()
        return (elem as LoadingModel.Warehouse)
    }


    companion object {
        const val ERROR_ARRAY = "ПричинаОшибки"
    }
}



