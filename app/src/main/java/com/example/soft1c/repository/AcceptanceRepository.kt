package com.example.soft1c.repository

import com.example.soft1c.adapter.AcceptanceAdapter
import com.example.soft1c.fragment.AcceptanceFragment
import com.example.soft1c.network.Network
import com.example.soft1c.repository.model.*
import com.example.soft1c.utils.Utils
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

class AcceptanceRepository {

    suspend fun getAcceptanceApi(number: String): Pair<Acceptance, List<AcceptanceEnableVisible>> {
        return suspendCoroutine { continuation ->
            Network.api.acceptance(number).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>,
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string() ?: ""
                        continuation.resume(getAcceptanceJson(responseBody))
                    } else {
                        continuation.resume(Pair(Acceptance(""), emptyList()))
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    continuation.resumeWithException(t)
                }

            })
        }
    }

    suspend fun getFieldsApi(guid: String, operation: String): FieldsAccess {
        return suspendCoroutine { continuation ->
            Network.api.fieldsAccess(guid, operation).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>,
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string() ?: ""
                        continuation.resume(getFieldsJson(responseBody))
                    } else {
                        continuation.resume(FieldsAccess())
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    continuation.resumeWithException(t)
                }

            })
        }
    }

    suspend fun getAcceptanceListApi(): List<Acceptance> {
        return suspendCoroutine { continuation ->
            Network.api.acceptanceList().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>,
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string() ?: ""
                        continuation.resume(getAcceptanceList(responseBody))
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

    suspend fun getClientApi(clientCode: String): Pair<Client, Boolean> {
        return suspendCoroutine { continuation ->
            Network.api.client(clientCode).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>,
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string() ?: ""
                        val jsonObject = JSONArray(responseBody).getJSONObject(0)
                        val code = jsonObject.getString(Client.CODE_KEY)
                        val serialDoc = jsonObject.getString(Client.SERAIL_DOC_KEY)
                        val numberDoc = jsonObject.getString(Client.NUMBER_DOC_KEY)
                        continuation.resume(Pair(Client(code, serialDoc, numberDoc), true))
                    } else {
                        continuation.resume(Pair(Client(), false))
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    continuation.resumeWithException(t)
                }

            })
        }
    }

    suspend fun createUpdateAccApi(acceptance: Acceptance): Pair<Acceptance, String> {
        return suspendCoroutine { continuation ->
            val jsonObject = JSONObject()
            if (acceptance.ref.isNotEmpty())
                jsonObject.put(REF_KEY, acceptance.ref)
            else
                jsonObject.put(REF_KEY, null)
            if (acceptance.batchGuid.isNotEmpty())
                jsonObject.put(BATCH_GUID_KEY, acceptance.batchGuid)
            else
                jsonObject.put(BATCH_GUID_KEY, null)
            jsonObject.put(CLIENT_KEY, acceptance.client)
            jsonObject.put(PACKAGE_UID_KEY, acceptance.packageUid)
            jsonObject.put(ZONE_KEY, acceptance.zoneUid)
            jsonObject.put(ID_CARD_KEY, acceptance.idCard)
            jsonObject.put(STORE_UID_KEY, acceptance.storeUid)
            jsonObject.put(PHONE_KEY, acceptance.phoneNumber)
            jsonObject.put(STORE_NAME_KEY, acceptance.storeName)
            jsonObject.put(PRODUCT_TYPE_KEY, acceptance.productType)
            jsonObject.put(REPRESENTATIVE_NAME_KEY, acceptance.representativeName)
            jsonObject.put(AUTO_NUMBER_KEY, acceptance.autoNumber)
            jsonObject.put(COUNT_SEAT_KEY, acceptance.countSeat)
            jsonObject.put(COUNT_IN_PACKAGE_KEY, acceptance.countInPackage)
            jsonObject.put(ALL_WEIGHT_KEY, acceptance.allWeight)
            jsonObject.put(GLASS_KEY, acceptance.glass)
            jsonObject.put(EXPENSIVE_KEY, acceptance.expensive)
            jsonObject.put(Z_KEY, acceptance.z)
            jsonObject.put(NOT_TURN_OVER_KEY, acceptance.notTurnOver)
            jsonObject.put(BRAND_KEY, acceptance.brand)
            jsonObject.put(COUNT_PACKAGE_KEY, acceptance.countPackage)
            jsonObject.put(CREATOR_KEY, acceptance.creator)
            jsonObject.put(PRINTED_KEY, acceptance.isPrinted)
            val requestBody =
                jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
            Network.api.createUpdateAcceptance(requestBody)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>,
                    ) {
                        if (response.isSuccessful) {
                            val jsonString = response.body()?.string() ?: ""
                            if (jsonString.isEmpty())
                                continuation.resume(Pair(acceptance, ""))
                            val ref = JSONArray(jsonString).getJSONObject(0).getString(REF_KEY)
                            val nextIsNeed =
                                JSONArray(jsonString).getJSONObject(0).getString(NEXT_IS_NEED_KEY)
                                    .toBoolean()
                            val guid =
                                JSONArray(jsonString).getJSONObject(0).getString(BATCH_GUID_KEY)
                            acceptance.ref = ref
                            AcceptanceAdapter.ACCEPTANCE_GUID = ref
                            AcceptanceFragment.NEXT_IS_NEED = nextIsNeed

                            acceptance.batchGuid = guid
                            AcceptanceFragment.BATCH_GUID = guid

                            continuation.resume(Pair(acceptance, ""))
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val jsonError =
                                JSONArray(errorBody).getJSONObject(0).getString(ERROR_ARRAY)
                            continuation.resume(Pair(acceptance, jsonError))
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        continuation.resumeWithException(t)
                    }

                })
        }
    }

    private fun getAcceptanceJson(responseString: String): Pair<Acceptance, List<AcceptanceEnableVisible>> {
        val jsonArray = JSONArray(responseString)
        val jsonObject = jsonArray.getJSONObject(0)
        val acceptance = getaAcceptanceFromJsonObject(jsonObject, true)
        val enableVisibleList = mutableListOf<AcceptanceEnableVisible>()
        val propertyArray = jsonObject.getJSONArray(FIELDS_PROPERTY_KEY)
        for (item in 0 until propertyArray.length()) {
            val propertyJson = propertyArray.getJSONObject(item)
            val field = propertyJson.getString(FIELD_KEY)
            val enable = propertyJson.getBoolean(ENABLE_KEY)
            val visible = propertyJson.getBoolean(VISIBLE_KEY)
            enableVisibleList.add(AcceptanceEnableVisible(field, enable, visible))
        }
        return Pair(acceptance, enableVisibleList)
    }

    private fun getFieldsJson(responseString: String): FieldsAccess {
        val acceptJson = JSONObject(responseString)
        val readOnly = acceptJson.optBoolean(READ_ONLY, true)
        val weightEnable = acceptJson.optBoolean(WEIGHT_ENABLE, true)
        val sizeEnable = acceptJson.optBoolean(SIZE_ENABLE, true)
        val isCreator = acceptJson.optBoolean(IS_CREATOR, true)
        val zoneEnable = acceptJson.optBoolean(ZONE_ENABLE, true)
        val chBoxEnable = acceptJson.optBoolean(CH_BOX_ENABLE, true)
        val propsEnable = acceptJson.optBoolean(PROPS_ENABLE, true)
        val packageEnable = acceptJson.optBoolean(PACKAGE_ENABLE, true)
        return FieldsAccess(
            readOnly = readOnly,
            weightEnable = weightEnable,
            sizeEnable = sizeEnable,
            isCreator = isCreator,
            zoneEnable = zoneEnable,
            chBoxEnable = chBoxEnable,
            properties = propsEnable,
            packageEnable = packageEnable
        )
    }

    private fun getAcceptanceList(responseString: String): List<Acceptance> {
        val acceptanceList = mutableListOf<Acceptance>()
        val acceptArray = JSONArray(responseString)
        for (index in 0 until acceptArray.length()) {
            val acceptJson = acceptArray.getJSONObject(index)
            val acceptance = getaAcceptanceFromJsonObject(acceptJson)
            acceptanceList.add(acceptance)
        }
        return acceptanceList
    }

    private fun getaAcceptanceFromJsonObject(
        acceptJson: JSONObject,
        hasAdditionalFields: Boolean = false,
    ): Acceptance {
        val ref = acceptJson.getString(REF_KEY)
        val date = acceptJson.getString(DATE_KEY)
        val number = acceptJson.getString(NUMBER_KEY)
        val client = acceptJson.getString(CLIENT_KEY)
        val packageUid = acceptJson.getString(PACKAGE_UID_KEY)
        val packageName = getPackageNameFromUid(packageUid)
        val zoneUid = acceptJson.getString(ZONE_KEY)
        val zoneName = getZoneNameFromUid(zoneUid)
        val weight = acceptJson.getBoolean(WEIGHT_KEY)
        val capacity = acceptJson.getBoolean(CAPACITY_KEY)
        if (hasAdditionalFields) {
            val autoNumber = acceptJson.getString(AUTO_NUMBER_KEY)
            val idCard = acceptJson.getString(ID_CARD_KEY)
            val countSeat = acceptJson.getInt(COUNT_SEAT_KEY)
            val countInPackage = acceptJson.getInt(COUNT_IN_PACKAGE_KEY)
            val countPackage = acceptJson.getInt(COUNT_PACKAGE_KEY)
            val allWeight = acceptJson.getDouble(ALL_WEIGHT_KEY)
            val storeUid = acceptJson.getString(STORE_UID_KEY)
            val storeAddressName = getAddressNameFromUid(storeUid)
            val productType = acceptJson.getString(PRODUCT_TYPE_KEY)
            val productTypeName = getProductTypeFromUid(productType)
            val phoneNumber = acceptJson.getString(PHONE_KEY)
            val storeName = acceptJson.getString(STORE_NAME_KEY)
            val batchGuid = acceptJson.getString(BATCH_GUID_KEY)
            val representativeName = acceptJson.getString(REPRESENTATIVE_NAME_KEY)
            val z = acceptJson.getBoolean(Z_KEY)
            val brand = acceptJson.getBoolean(BRAND_KEY)
            val glass = acceptJson.getBoolean(GLASS_KEY)
            val expensive = acceptJson.getBoolean(EXPENSIVE_KEY)
            val isPrinted = acceptJson.getBoolean(PRINTED_KEY)
            val notTurnOver = acceptJson.getBoolean(NOT_TURN_OVER_KEY)
            return Acceptance(
                z = z,
                brand = brand,
                glass = glass,
                expensive = expensive,
                notTurnOver = notTurnOver,
                ref = ref,
                date = date,
                countPackage = countPackage,
                storeAddressName = storeAddressName,
                productTypeName = productTypeName,
                batchGuid = batchGuid,
                autoNumber = autoNumber,
                countInPackage = countInPackage,
                number = number,
                phoneNumber = phoneNumber,
                packageUid = packageUid,
                storeName = storeName,
                productType = productType,
                countSeat = countSeat,
                storeUid = storeUid,
                idCard = idCard,
                zone = zoneName,
                _package = packageName,
                allWeight = allWeight,
                client = client,
                zoneUid = zoneUid,
                representativeName = representativeName,
                isPrinted = isPrinted,
                weight = weight,
                capacity = capacity
            )
        }
        return Acceptance(
            _package = packageName,
            zoneUid = zoneUid,
            zone = zoneName,
            packageUid = packageUid,
            ref = ref,
            number = number,
            client = client,
            weight = weight,
            date = date,
            capacity = capacity
        )
    }

    private fun getZoneNameFromUid(zoneUid: String): String {
        val elem = Utils.zones.find {
            (it as AnyModel.Zone).ref == zoneUid
        } ?: return ""
        return (elem as AnyModel.Zone).name
    }

    private fun getProductTypeFromUid(zoneUid: String): String {
        val elem = Utils.productTypes.find {
            (it as AnyModel.ProductType).ref == zoneUid
        } ?: return ""
        return (elem as AnyModel.ProductType).name
    }

    private fun getPackageNameFromUid(zoneUid: String): String {
        val elem = Utils.packages.find {
            (it as AnyModel.PackageModel).ref == zoneUid
        } ?: return ""
        return (elem as AnyModel.PackageModel).name
    }

    private fun getAddressNameFromUid(zoneUid: String): String {
        val elem = Utils.addressess.find {
            (it as AnyModel.AddressModel).ref == zoneUid
        } ?: return ""
        return (elem as AnyModel.AddressModel).name
    }

    companion object {
        const val REF_KEY = "Ссылка"
        const val DATE_KEY = "Дата"
        const val NUMBER_KEY = "Номер"
        const val CLIENT_KEY = "Клиент"
        const val AUTO_NUMBER_KEY = "НомерАвто"
        const val ID_CARD_KEY = "IDПродавца"
        const val WEIGHT_KEY = "Вес"
        const val CAPACITY_KEY = "Замер"
        const val ZONE_KEY = "Зона"
        const val COUNT_SEAT_KEY = "КоличествоМест"
        const val COUNT_IN_PACKAGE_KEY = "КоличествоВУпаковке"
        const val COUNT_PACKAGE_KEY = "КоличествоТиповУпаковок"
        const val ALL_WEIGHT_KEY = "ОбщийВес"
        const val PACKAGE_UID_KEY = "ТипУпаковки"
        const val PRODUCT_TYPE_KEY = "ВидТовара"
        const val STORE_UID_KEY = "АдресМагазина"
        const val PHONE_KEY = "ТелефонМагазина"
        const val STORE_NAME_KEY = "НаименованиеМагазина"
        const val REPRESENTATIVE_NAME_KEY = "ИмяПредставителя"
        const val BATCH_GUID_KEY = "GUIDПартии"
        const val Z_KEY = "ZТовар"
        const val BRAND_KEY = "Брэнд"
        const val GLASS_KEY = "Стекло"
        const val EXPENSIVE_KEY = "Дорогой"
        const val NOT_TURN_OVER_KEY = "НеКантовать"
        const val PRINTED_KEY = "Напечатан"

        const val READ_ONLY = "ViewOnly"
        const val WEIGHT_ENABLE = "InputWeight"
        const val SIZE_ENABLE = "InputSizes"
        const val IS_CREATOR = "OwnDocument"
        const val PROPS_ENABLE = "PropertiesProducts"
        const val ZONE_ENABLE = "Zona"
        const val CH_BOX_ENABLE = "Checkbox"
        const val PACKAGE_ENABLE = "AmountInPackage"

        const val CREATOR_KEY = "Создатель"
        const val ERROR_ARRAY = "ПричинаОшибки"
        const val FIELDS_PROPERTY_KEY = "ПараметрыВидимости"
        const val FIELD_KEY = "Поле"
        const val ENABLE_KEY = "Доступность"
        const val VISIBLE_KEY = "Видимость"
        const val ON_CHINESE = "НаКитайском"
        const val NEXT_IS_NEED_KEY = "НуженСледующий"
    }

}