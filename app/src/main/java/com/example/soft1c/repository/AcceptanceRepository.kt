package com.example.soft1c.repository

import com.example.soft1c.adapter.AcceptanceAdapter
import com.example.soft1c.fragment.AcceptanceFragment
import com.example.soft1c.network.Network
import com.example.soft1c.repository.model.Acceptance
import com.example.soft1c.repository.model.AnyModel
import com.example.soft1c.repository.model.Client
import com.example.soft1c.repository.model.FieldsAccess
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AcceptanceRepository {

    suspend fun getAcceptanceApi(
        number: String,
        operation: String
    ): Triple<Acceptance, FieldsAccess, String> {
        return withRefreshedConnection{
            suspendCoroutine { continuation ->
                Network.api.acceptance(number, operation).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>,
                    ) {
                        if (response.isSuccessful) {
                            Utils.logFor1C = response.body()?.string() ?: ""
                            continuation.resume(getAcceptanceJson(Utils.logFor1C))
                        } else {
                            try {
                                val errorBody = response.errorBody()?.string()
                                continuation.resume(
                                    Triple(
                                        Acceptance(""),
                                        FieldsAccess(),
                                        errorBody.toString()
                                    )
                                )
                            } catch (e: Exception) {
                                continuation.resume(
                                    Triple(
                                        Acceptance(""),
                                        FieldsAccess(),
                                        e.message.toString()
                                    )
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        continuation.resumeWithException(t)
                    }
                })
            }
        }
    }

    suspend fun checkConnection(): Boolean {
        return withRefreshedConnection{
            suspendCoroutine { continuation ->
                Network.checkConnectionApi.check().enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>,
                    ) {
                        continuation.resume(response.isSuccessful)
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        continuation.resume(false)
                    }

                })
            }
        }
    }

    suspend fun getAcceptanceListApi(): List<Acceptance> {
        return withRefreshedConnection{
            suspendCoroutine { continuation ->
                if (Utils.debugMode) {
                    continuation.resume(getAcceptanceList(Acceptance.LIST_DEFAULT_DATA))
                } else {
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
        }

    }

    suspend fun getClientApi(clientCode: String): Pair<Client, Boolean> {
        return withRefreshedConnection{
            suspendCoroutine { continuation ->
                if (Utils.debugMode) {
                    val jsonObject = JSONArray(Client.DEFAULT_DATA).getJSONObject(0)
                    val client = getClientFromJsonObject(jsonObject)
                    continuation.resume(Pair(client, true))
                } else {
                    Network.api.client(clientCode).enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>,
                        ) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()?.string() ?: ""
                                val jsonObject = JSONArray(responseBody).getJSONObject(0)
                                val client = getClientFromJsonObject(jsonObject)
                                continuation.resume(Pair(client, true))
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
        }
    }

    suspend fun createUpdateAccApi(acceptance: Acceptance): Pair<Acceptance, String> {
        return withRefreshedConnection{
            suspendCoroutine { continuation ->
                val jsonObject = JSONObject()
                if (acceptance.ref.isNotEmpty())
                    jsonObject.put(REF_KEY, acceptance.ref)
                else
                    jsonObject.put(REF_KEY, null)
                if (acceptance.batchGuid.isNotEmpty())
                    jsonObject.put(BATCH_GUID_KEY, acceptance.batchGuid)
                else
                    jsonObject.put(BATCH_GUID_KEY, null)
                jsonObject.put(CLIENT_KEY, acceptance.client.code)
                jsonObject.put(PACKAGE_UID_KEY, acceptance.packageUid)
                jsonObject.put(ZONE_KEY, acceptance.zoneUid)
                jsonObject.put(ID_CARD_KEY, acceptance.idCard)
                jsonObject.put(TRACK_NUMBER_KEY, acceptance.trackNumber)
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
                jsonObject.put(TYPE_KEY, acceptance.type)
                jsonObject.put(PRINTED_KEY, acceptance.isPrinted)
                val requestBody =
                    jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
                if (Utils.debugMode) {
                    continuation.resume(Pair(acceptance, ""))
                } else {
                    Network.api.createUpdateAcceptance(Utils.Settings.fillBarcodes, requestBody)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>,
                            ) {
                                if (response.isSuccessful) {
                                    val body = response.body()?.string() ?: ""
                                    if (body.isEmpty())
                                        continuation.resume(Pair(acceptance, ""))
                                    val jsonObject = JSONArray(body).getJSONObject(0)
                                    var ref = ""
                                    var guid = ""
                                    var nextIsNeed = false
                                    try {
                                        if (jsonObject.getString(RESULT_KEY).equals("Ошибка")) {
                                            continuation.resume(
                                                Pair(
                                                    acceptance,
                                                    jsonObject.getString(ERROR_REASON_KEY)
                                                )
                                            )
                                        }
                                        ref = jsonObject.getString(REF_KEY)
                                        guid = jsonObject.getString(BATCH_GUID_KEY)
                                        nextIsNeed = jsonObject.getString(NEXT_IS_NEED_KEY).toBoolean()
                                    } catch (e: Exception) {
                                        continuation.resume(Pair(acceptance, e.message.toString()))
                                    }
                                    acceptance.ref = ref
                                    AcceptanceAdapter.ACCEPTANCE_GUID = ref
                                    AcceptanceFragment.NEXT_IS_NEED = nextIsNeed

                                    acceptance.number = jsonObject.getString(NUMBER_KEY).toString()
                                    acceptance.date = jsonObject.optString(DATE_KEY, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)).toString()

                                    acceptance.batchGuid = guid
                                    AcceptanceFragment.BATCH_GUID = guid

                                    continuation.resume(Pair(acceptance, ""))
                                } else {
                                    val errorBody = response.errorBody()?.string() ?: response.message()
                                    continuation.resume(Pair(acceptance, errorBody))
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

    private fun getAcceptanceJson(responseString: String): Triple<Acceptance, FieldsAccess, String> {
        val jsonArray = JSONArray(responseString)
        val jsonAcceptance = jsonArray.getJSONObject(0)
        val acceptance = getaAcceptanceFromJsonObject(jsonAcceptance, true)
        val property = jsonAcceptance.getString(FIELDS_PROPERTY_KEY)
        val fieldAccess = getFieldsJson(property)
        return Triple(acceptance, fieldAccess, "")
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
        val packageUid = acceptJson.getString(PACKAGE_UID_KEY)
        val packageName = getPackageNameFromUid(packageUid)
        val zoneUid = acceptJson.getString(ZONE_KEY)
        val zoneName = getZoneNameFromUid(zoneUid)
        val weight = acceptJson.getBoolean(WEIGHT_KEY)
        val capacity = acceptJson.getBoolean(CAPACITY_KEY)
        if (hasAdditionalFields) {
            val clientObject = acceptJson.getJSONArray("ДанныеКлиента").getJSONObject(0)
            val client = getClientFromJsonObject(clientObject)
            val autoNumber = acceptJson.getString(AUTO_NUMBER_KEY)
            val idCard = acceptJson.getString(ID_CARD_KEY)
            val trackNumber = acceptJson.getString(TRACK_NUMBER_KEY)
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
            val whoAccept = acceptJson.getString(WHO_ACCEPT_KEY)
            val whoWeigh = acceptJson.getString(WHO_WEIGH_KEY)
            val whoMeasure = acceptJson.getString(WHO_MEASURE_KEY)
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
                trackNumber = trackNumber,
                zone = zoneName,
                _package = packageName,
                allWeight = allWeight,
                client = client,
                zoneUid = zoneUid,
                representativeName = representativeName,
                isPrinted = isPrinted,
                weight = weight,
                capacity = capacity,
                whoAccept = whoAccept,
                whoMeasure = whoMeasure,
                whoWeigh = whoWeigh
            )
        }
        val client = acceptJson.getString(CLIENT_KEY)
        return Acceptance(
            _package = packageName,
            zoneUid = zoneUid,
            zone = zoneName,
            packageUid = packageUid,
            ref = ref,
            number = number,
            client = Client(
                code = client
            ),
            weight = weight,
            date = date,
            capacity = capacity
        )
    }

    private fun getClientFromJsonObject(jsonObject: JSONObject): Client {
        val code = jsonObject.getString(Client.CODE_KEY)
        val serialDoc = jsonObject.getString(Client.SERAIL_DOC_KEY)
        val numberDoc = jsonObject.getString(Client.NUMBER_DOC_KEY)
        val haveRepresentative = jsonObject.getBoolean(Client.HAVE_REPRESENTATIVE_KEY)
        val serialRepr = jsonObject.getString(Client.SERIAL_REPRESENTATIVE_KEY)
        val numberRepr = jsonObject.getString(Client.NUMBER_REPRESENTATIVE_KEY)
        val blockAcceptance = jsonObject.getBoolean(Client.BLOCK_ACCEPTANCE_KEY)
        val blacklist = jsonObject.getBoolean(Client.BLACKLIST_KEY)

        return Client(
            code = code,
            serialDoc = serialDoc,
            numberDoc = numberDoc,
            haveRepresentative = haveRepresentative,
            serialRepr = serialRepr,
            numberRepr = numberRepr,
            blockAcceptance = blockAcceptance,
            blacklist = blacklist
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

    fun getPackageNameFromUid(zoneUid: String): String {
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
        const val TRACK_NUMBER_KEY = "ТрекКод"
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
        const val CH_BOX_ENABLE = "Сheckbox"
        const val PACKAGE_ENABLE = "AmountInPackage"

        const val WHO_ACCEPT_KEY = "ТоварПринял"
        const val WHO_WEIGH_KEY = "ТоварВзвесил"
        const val WHO_MEASURE_KEY = "ТоварИзмерил"
        const val CREATOR_KEY = "Создатель"
        const val TYPE_KEY = "Тип"
        const val FIELDS_PROPERTY_KEY = "ПараметрыВидимости"
        const val ON_CHINESE = "НаКитайском"
        const val NEXT_IS_NEED_KEY = "НуженСледующий"
        const val RESULT_KEY = "Результат"
        const val ERROR_REASON_KEY = "ПричинаОшибки"
    }

}