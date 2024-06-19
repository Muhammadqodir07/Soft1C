package com.example.soft1c.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

//Интерфейс определяет, какие методы и параметры необходимы для взаимодействия клиента и сервера, но не реализует их.
interface BaseApi {

    @GET("authorization")
    fun auth(): Call<ResponseBody>

    @POST("Logging/AddRecord")
    fun sendLog(@Body log: String): Call<ResponseBody>

    @GET("Priemki")
    fun acceptanceList(): Call<ResponseBody>

    @GET("DanniePriemki")
    fun acceptance(
        @Query("Nomer") number: String,
        @Query("ВидОперации") operationType: String
    ): Call<ResponseBody>

    @GET("DostupnostPoley")
    fun fieldsAccess(
        @Query("GUID") number: String,
        @Query("ВидОперации") operationType: String
    ): Call<ResponseBody>

    @GET("Addressa")
    fun addressList(): Call<ResponseBody>

    @GET("Zoni")
    fun zoneList(): Call<ResponseBody>

    @GET("TipiUpakovki")
    fun packageList(): Call<ResponseBody>

    @GET("VidiTovarov")
    fun productTypeList(): Call<ResponseBody>

    @GET("Klienti")
    fun client(@Query("Kod") clientCode: String): Call<ResponseBody>

    @PATCH("Priemki")
    fun createUpdateAcceptance(
        @Query("FillBarcodes") fillBarcodes: String,
        @Body requestBody: RequestBody
    ): Call<ResponseBody>

    @GET("VvodRazmerov")
    fun getAcceptanceSizeData(@Query("GUID") guid: String): Call<ResponseBody>

    @PATCH("VvodRazmerov")
    fun updateAcceptanceSize(
        @Query("GUID") guid: String,
        @Body requestBody: RequestBody,
    ): Call<ResponseBody>
}

interface LoadingApi{
    @GET("autorization")
    fun loadAuth(): Call<ResponseBody>

    @GET("cars")
    fun carsList(): Call<ResponseBody>

    @GET("Sklads")
    fun warehouseList(): Call<ResponseBody>

    @GET("container")
    fun containerList(): Call<ResponseBody>

    @POST("pogruzka")
    fun createUpdateLoading(@Body requestBody: RequestBody): Call<ResponseBody>

    @GET("pogruzka")
    fun loadingList(
    ): Call<ResponseBody>

    @GET("Razgruzka")
    fun reloadingList(): Call<ResponseBody>

    @GET("pogruzka")
    fun loading(@Query("GUIDНаСервере") number: String): Call<ResponseBody>

    @GET("Razgruzka")
    fun reloading(@Query("GUIDНаСервере") number: String): Call<ResponseBody>

    @GET("Barcodes")
    fun barcodeList(
        @Query("ШтрихКод") barcode: String,
        @Query("Склад") warehouse: String
    ): Call<ResponseBody>
}

interface AutoApi{
    @GET("AUTO")
    fun auto(): Call<ResponseBody>
}