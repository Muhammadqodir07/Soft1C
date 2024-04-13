package com.example.soft1c.reloading.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.AutoCompleteTextView
import com.example.soft1c.repository.model.LoadingModel
import com.example.soft1c.utils.Utils
import com.google.android.material.textfield.TextInputEditText

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

//Функция НайтиИЗаполнитьВыбранныйЭлемент получает данные списка Машин, поле НомераМашин и текст выбранного элемента в списке
fun findAndFillAnySelectedModel(
    anyList: List<LoadingModel>,
    model: Int,
    selectedModel: String
): LoadingModel? {
    //Переменная текстЭлемента. Если текст выбранного элемента не пусто то передать его, если пусто то вернуться
    val textElement = when {
        selectedModel.isNotEmpty() -> selectedModel
        selectedModel.isEmpty() -> return null
        else -> ""
    }
    //Найти номер из списка номеров с помощью текстЭлемента
    return textElementFound(anyList, model, textElement)
}

fun findAndFillAnyModel(
    anyList: List<LoadingModel>,
    model: Int,
    view: AutoCompleteTextView,
): LoadingModel? {
    val textElement = when {
        view.text.isNotEmpty() -> {
            if (!view.adapter.isEmpty) {
                view.adapter.getItem(0).toString()
            } else {
                view.text.clear()
                return null
            }
        }

        view.text.isEmpty() -> return null
        else -> ""
    }
    //Найти номер из списка номеров с помощью текстЭлемента
    return textElementFound(anyList, model, textElement)
}

//Находим номер из списка номеров с помощью текстЭлемента
private fun textElementFound(
    anyList: List<LoadingModel>,
    model: Int,
    textElement: String,
): LoadingModel? {
    //Переменная элемент. Найти из списка машин номер по условию СписокМашин.Номер = текстЭлемента с помощью встроенной функции find
    val element = anyList.find {
        when (it) {
            is LoadingModel.Car -> it.number == textElement
            is LoadingModel.Warehouse -> it.name == textElement
            is LoadingModel.Container -> it.name == textElement
        }
    }

    return when (element) {
        is LoadingModel.Car -> element
        is LoadingModel.Container -> element
        is LoadingModel.Warehouse -> element
        else -> when (model) {
            Utils.ObjectModelType.CAR -> LoadingModel.Car()
            Utils.ObjectModelType.CONTAINER -> LoadingModel.Container()
            Utils.ObjectModelType.WAREHOUSE -> LoadingModel.Warehouse()
            else -> null
        }
    }
}

fun etxtFocusChangeListener(view: View, hasFocus: Boolean) {
    if (hasFocus) {
        view as TextInputEditText
        view.text?.let {
            view.selectAll()
        }
    }
}