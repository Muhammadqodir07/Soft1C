package com.example.soft1c.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import com.example.soft1c.repository.model.Acceptance
import com.example.soft1c.repository.model.AnyModel
import com.example.soft1c.repository.model.LoadingModel
import com.example.soft1c.repository.model.User
import java.text.SimpleDateFormat
import java.util.Locale

object Utils {
    var base_url = ""
    var basename = ""
    var username = ""
    var password = ""

    var acceptance_auth = ""
    var loading_auth = ""
    var auth = ""
    var lang = ""

    var clientTimeout = 30L
    var logFor1C = ""

    var productTypes: List<AnyModel> = listOf()
    var addressess: List<AnyModel> = listOf()
    var packages: List<AnyModel> = listOf()
    var zones: List<AnyModel> = listOf()

    //Переменная для хранения списка машин
    var cars: List<LoadingModel> = listOf()
    var warehouse: List<LoadingModel> = listOf()
    var container: List<LoadingModel> = listOf()

    var acceptanceCopyList: MutableList<Acceptance> = mutableListOf()

    var user = User()
    var refreshList: Boolean = false

    var debugMode = false
    val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val outputDateFormat = SimpleDateFormat("dd/MM/yy\nHH:mm", Locale.getDefault())


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

    object ObjectModelType {
        const val ADDRESS = 1
        const val _PACKAGE = 2
        const val PRODUCT_TYPE = 3
        const val ZONE = 4
        const val CAR = 5
        const val WAREHOUSE = 6
        const val CONTAINER = 7
        const val EMPTY = 0
    }

    object OperationType {
        const val ACCEPTANCE = "ПриемГруза"
        const val WEIGHT = "ВзвешиваниеГруза"
        const val SIZE = "ЗамерГруза"
    }

    object Settings {
        var passportClientControl: Boolean = true
        const val SHOW_DISABILITY_DIALOG = "ПоказатьОкноСИнфойНедоступности"
    }
}

fun getDisplayWidth(context: Context): Int {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val displayMetrics = DisplayMetrics()

    // Get the default display
    windowManager.defaultDisplay.getMetrics(displayMetrics)

    val screenWidthPixels = displayMetrics.widthPixels
    val density = context.resources.displayMetrics.density
    val paddingInPixels = (8 * density + 0.5f).toInt() // Converting dp to pixels

    return screenWidthPixels - paddingInPixels * 2 // Subtracting padding from both sides
}