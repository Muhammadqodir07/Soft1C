package com.example.soft1c.utils

import com.example.soft1c.repository.model.Acceptance
import com.example.soft1c.repository.model.AnyModel
import com.example.soft1c.repository.model.LoadingModel
import com.example.soft1c.repository.model.User

object Utils {
    var base_url = ""
    var basename = ""
    var username = ""
    var password = ""
    var acceptance_auth = ""
    var loading_auth = ""
    var auth = ""
    var lang = ""
    var productTypes: List<AnyModel> = listOf()
    var addressess: List<AnyModel> = listOf()
    var packages: List<AnyModel> = listOf()
    var zones: List<AnyModel> = listOf()
    //Переменная для хранения списка машин
    var cars: List<LoadingModel> = listOf()
    var warehouse: List<LoadingModel> = listOf()
    var acceptanceCopyList: MutableList<Acceptance> = mutableListOf()
    var user = User()
    var anyModel: AnyModel? = null
    var refreshList: Boolean = false

    fun setAttributes(
        baseUrl: String,
        base_name: String,
        user_name: String,
        _password: String,
        _lang: String
    ) {
        base_url = baseUrl
        basename = base_name
        username = user_name
        password = _password
        acceptance_auth = "/${base_name}/hs/PriemkiAPI/"
        loading_auth = "/${base_name}/hs/PogruzkaApi/"
        auth = "/${base_name}/hs/TSD/"
        lang = _lang
    }

    object Contracts {
        const val REF_KEY = "Ссылка"
        const val NAME_KEY = "Наименование"
        const val CODE_KEY = "Код"
    }

    //Объект с ключевыми названиями машин
    object Cars {
        const val REF_KEY = "GUID"
        const val NUMBER_KEY = "Номер"
    }

    object ObjectModelType {
        const val ADDRESS = 1
        const val _PACKAGE = 2
        const val PRODUCT_TYPE = 3
        const val ZONE = 4
        const val CAR = 5
        const val WAREHOUSE = 6
        const val EMPTY = 7
    }

    object OperationType {
        const val ACCEPTANCE = "ПриемГруза"
        const val WEIGHT = "ВзвешиваниеГруза"
        const val SIZE = "ЗамерГруза"
    }
}