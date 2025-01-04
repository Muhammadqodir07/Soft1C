package com.example.soft1c.repository

import com.example.soft1c.network.Network
import com.example.soft1c.repository.model.ExpandableLoadingList
import com.example.soft1c.repository.model.Loading
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
import com.example.soft1c.repository.model.LoadingBarcodeChild
import com.example.soft1c.repository.model.LoadingBarcodeParent
import com.example.soft1c.repository.model.LoadingEnableVisible
import com.example.soft1c.repository.model.LoadingModel
import com.example.soft1c.utils.Utils
import com.example.soft1c.utils.withRefreshedConnection
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

open class LoadingRepository {

    suspend fun getLoadingApi(number: String): Pair<Loading, List<LoadingEnableVisible>>? {
        return withRefreshedConnection {
            suspendCoroutine { continuation ->
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
    }

    suspend fun getBarcodeListApi(barcode: String, warehouse: String): List<ExpandableLoadingList>? {
        return withRefreshedConnection {
            suspendCoroutine { continuation ->
                if (Utils.debugMode) {
                    continuation.resume(getBarcodeListJson(LoadingBarcode.DEFAULT_DATA, barcode))
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
                                } else if (response.code() == 502) {
                                    continuation.resume(emptyList())
                                    //TODO()
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
    }

    fun getBarcodeListJson(responseBody: String, debugBarcode: String? = null): List<ExpandableLoadingList>? {
        var barcodeList = mutableListOf<LoadingBarcode>()
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

            if (Utils.debugMode && debugBarcode != null){
                val acceptanceUid = barcodeList.find { it.barcode == debugBarcode }?.acceptanceUid
                acceptanceUid?.let {
                    barcodeList = barcodeList.filter { it.acceptanceUid == acceptanceUid }.toMutableList()
                }
            }

            return groupToExpandableList(barcodeList)
        } catch (e: Exception) {
            return null
        }
    }

    private fun groupToExpandableList(barcodes: List<LoadingBarcode>): List<ExpandableLoadingList> {
        val grouped = barcodes.groupBy { it.acceptanceNumber to it.acceptanceUid }

        val expandableList = mutableListOf<ExpandableLoadingList>()

        grouped.forEach { (key, groupItems) ->
            val (acceptanceNumber, acceptanceUid) = key

            // Create parent
            val parent = LoadingBarcodeParent(
                barcodes = groupItems.map {
                    LoadingBarcodeChild(
                        parentUid = it.acceptanceUid,
                        barcode = it.barcode,
                        weight = it.weight,
                        volume = it.volume,
                        packageTypeUid = it.packageTypeUid,
                        packageType = it.packageType,
                        seatNumber = it.seatNumber
                    )
                },
                acceptanceNumber = acceptanceNumber,
                acceptanceUid = acceptanceUid,
                totalSeats = groupItems.size,
                clientCode = groupItems.firstOrNull()?.clientCode ?: "",
                date = groupItems.firstOrNull()?.date ?: ""
            )

            expandableList.add(
                ExpandableLoadingList(
                    type = ExpandableLoadingList.PARENT,
                    loadingParent = parent
                )
            )
        }

        return expandableList
    }

    suspend fun createUpdateLoadingApi(jsonBody: JSONObject): String {
        return withRefreshedConnection {
            suspendCoroutine { continuation ->
                val requestBody =
                    jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
                Network.loadingApi.createUpdateLoading(requestBody)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.isSuccessful) {
                                continuation.resume("")
                            } else {
                                val errorBody = response.errorBody()?.string()
                                val jsonError =
                                    JSONArray(errorBody).getJSONObject(0).getString(ERROR_ARRAY)
                                continuation.resume(jsonError)
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            continuation.resumeWithException(t)
                        }
                    })
            }
        }
    }

    suspend fun getLoadingListApi(): List<Loading> {
        return withRefreshedConnection{
            suspendCoroutine { continuation ->
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

    fun getLoadingJson(responseString: String): Pair<Loading, List<LoadingEnableVisible>> {
        val jsonArray = JSONArray(responseString)
        val jsonObject = jsonArray.getJSONObject(0)
        val loading = getLoadingFromJsonObject(jsonObject, true)
        val enableVisibleList = mutableListOf<LoadingEnableVisible>()
        //val propertyArray = jsonObject.getJSONArray()
        return Pair(loading, enableVisibleList)
    }

    fun getLoadingFromJsonObject(loadingJson: JSONObject, hasBarcodes: Boolean): Loading {
        val ref = loadingJson.optString(REF_KEY, "")
        val guid = loadingJson.optString(GUID_KEY, "")
        val number = loadingJson.optString(NUMBER_KEY, "")
        val senderWarehouseUid = loadingJson.getString(WAREHOUSE_BEGIN_KEY)
        val getterWarehouseUid = loadingJson.getString(WAREHOUSE_END_KEY)
        val sender = getWarehouseFromUid(senderWarehouseUid)
        val getter = getWarehouseFromUid(getterWarehouseUid)
        val carUid = loadingJson.getString(CAR_NUMBER_KEY)
        val car = getCarNumberFromUid(carUid)
        if (hasBarcodes) {
            val barcodesFrontJson = loadingJson.getJSONArray(ITEMS_FRONT_KEY)
            val date = loadingJson.getString(DATE_KEY)
            val barcodesFront = mutableListOf<LoadingBarcode>()
            for (i in 0 until barcodesFrontJson.length()) {
                val jsonObject = barcodesFrontJson.getJSONObject(i)
                val packageUid = jsonObject.getString(PACKAGE_KEY)
                if (jsonObject.getString(BARCODE_KEY) == "")
                    continue
                barcodesFront.add(
                    LoadingBarcode(
                        barcode = jsonObject.getString(BARCODE_KEY),
                        weight = jsonObject.getDouble(WEIGHT_KEY),
                        volume = jsonObject.getDouble(VOLUME_KEY),
                        clientCode = jsonObject.getString(CLIENT_CODE_KEY),
                        seatNumber = jsonObject.optInt(SEAT_NUMBER_KEY, -1),
                        packageTypeUid = packageUid,
                        packageType = AcceptanceRepository().getPackageNameFromUid(packageUid),
                        acceptanceUid = jsonObject.getString(ACCEPTANCE_GUID_KEY),
                        acceptanceNumber = jsonObject.getString(ACCEPTANCE_NUMBER_KEY),
                        date = jsonObject.getString(DATE_KEY)
                    )
                )
            }
            val barcodesBackJson = loadingJson.getJSONArray(ITEMS_BACK_KEY)
            val barcodesBack = mutableListOf<LoadingBarcode>()
            for (i in 0 until barcodesBackJson.length()) {
                val jsonObject = barcodesBackJson.getJSONObject(i)
                val packageUid = jsonObject.optString(PACKAGE_KEY, "")
                if (jsonObject.getString(BARCODE_KEY) == "")
                    continue
                barcodesBack.add(
                    LoadingBarcode(
                        barcode = jsonObject.getString(BARCODE_KEY),
                        weight = jsonObject.getDouble(WEIGHT_KEY),
                        volume = jsonObject.getDouble(VOLUME_KEY),
                        clientCode = jsonObject.getString(CLIENT_CODE_KEY),
                        seatNumber = jsonObject.optInt(SEAT_NUMBER_KEY, -1),
                        packageTypeUid = packageUid,
                        packageType = AcceptanceRepository().getPackageNameFromUid(packageUid),
                        acceptanceUid = jsonObject.getString(ACCEPTANCE_GUID_KEY),
                        acceptanceNumber = jsonObject.getString(ACCEPTANCE_NUMBER_KEY),
                        date = jsonObject.getString(DATE_KEY)
                    )
                )
            }
            val expandableBarcodesFront = groupToExpandableList(barcodesFront)
            val expandableBarcodesBack = groupToExpandableList(barcodesBack)
            return Loading(
                number = number,
                ref = ref,
                guid = guid,
                date = date,
                car = car,
                senderWarehouse = sender,
                getterWarehouse = getter,
                barcodesFront = expandableBarcodesFront,
                barcodesBack = expandableBarcodesBack
            )
        } else {
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



