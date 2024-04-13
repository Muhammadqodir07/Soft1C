package com.example.soft1c.reloading.repository

import com.example.soft1c.network.Network
import com.example.soft1c.repository.LoadingRepository
import com.example.soft1c.repository.model.Loading
import com.example.soft1c.repository.model.LoadingBarcode
import com.example.soft1c.repository.model.LoadingEnableVisible
import com.example.soft1c.utils.Utils
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ReloadingRepository : LoadingRepository() {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    suspend fun getReloadingApi(number: String): Pair<Loading, List<LoadingEnableVisible>>? {
        return suspendCoroutine { continuation ->
            if (Utils.debugMode) {
                val doc = getLoadingList(Loading.DEFAULT_DATA_LIST)
                continuation.resume(Pair(doc.first(), mutableListOf()))
            } else {
                Network.loadingApi.reloading(number).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()?.string() ?: ""
                            continuation.resume(getReloadingJson(responseBody))
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

    suspend fun getReloadingListApi(): List<Loading> {
        return suspendCoroutine { continuation ->
            if (Utils.debugMode) {
                continuation.resume(getLoadingList(Loading.DEFAULT_DATA_LIST))
            } else {
                Network.loadingApi.reloadingList().enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()?.string() ?: ""
                            continuation.resume(getReloadingList(responseBody))
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
    fun getReloadingJson(responseString: String): Pair<Loading, List<LoadingEnableVisible>>? {
        val jsonArray = JSONArray(responseString)
        val jsonObject = jsonArray.getJSONObject(0)
        val loading = getReloadingFromJsonObject(jsonObject, true)
        val enableVisibleList = mutableListOf<LoadingEnableVisible>()
        //val propertyArray = jsonObject.getJSONArray()
        return Pair(loading, enableVisibleList)
    }

    fun getReloadingList(responseString: String): List<Loading> {
        val loadingList = mutableListOf<Loading>()
        val loadingArray = JSONArray(responseString)
        for (index in 0 until loadingArray.length()) {
            val loadingJson = loadingArray.getJSONObject(index)
            val loading = getReloadingFromJsonObject(loadingJson, false)
            loadingList.add(loading)
        }
        return loadingList
    }

    private fun getReloadingFromJsonObject(loadingJson: JSONObject, hasBarcodes: Boolean): Loading {
        val ref = loadingJson.optString("Ссылка", "")
        val guid = loadingJson.getString(Loading.GUID_KEY)
        val number = loadingJson.getString(Loading.NUMBER_KEY)
        val senderWarehouseUid = loadingJson.getString(Loading.WAREHOUSE_BEGIN_KEY)
        val getterWarehouseUid = loadingJson.getString(Loading.WAREHOUSE_END_KEY)
        val sender = getWarehouseFromUid(senderWarehouseUid)
        val getter = getWarehouseFromUid(getterWarehouseUid)
        if (hasBarcodes) {
            val barcodesFrontJson = loadingJson.getJSONArray(Loading.ITEMS_FRONT_KEY)
            val date = loadingJson.getString(Loading.DATE_KEY)
            val carUid = loadingJson.getString(Loading.CAR_KEY)
            val car = getCarNumberFromUid(carUid)
            val barcodesFront = mutableListOf<LoadingBarcode>()
            for (i in 0 until barcodesFrontJson.length()) {
                val jsonObject = barcodesFrontJson.getString(i)
                barcodesFront.add(
                    LoadingBarcode(
                        barcode = jsonObject,
                    )
                )
            }
            val barcodesBackJson = loadingJson.getJSONArray(Loading.ITEMS_BACK_KEY)
            val barcodesBack = mutableListOf<LoadingBarcode>()
            for (i in 0 until barcodesBackJson.length()) {
                val jsonObject = barcodesBackJson.getString(i)
                barcodesBack.add(
                    LoadingBarcode(
                        barcode = jsonObject,
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
            val carUid = loadingJson.getString(Loading.CAR_NUMBER_KEY)
            val car = getCarNumberFromUid(carUid)
            val date = loadingJson.getString(Loading.DOCUMENT_DATE_KEY)
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
}