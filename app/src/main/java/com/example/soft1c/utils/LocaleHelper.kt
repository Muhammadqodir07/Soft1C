package com.example.soft1c.utils

import android.content.Context
import android.preference.PreferenceManager
import java.util.*

class LocaleHelper {

    private val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    fun onAttach(context: Context): Context {
        val lang = getPersistedData(context, Locale.getDefault().getLanguage()).toString()
        return setLocale(context, lang)
    }

    fun onAttach(context: Context, defaultLanguage: String): Context {
        val lang = getPersistedData(context, defaultLanguage).toString()
        return setLocale(context, lang)
    }

    //Получение языка
    fun getLanguage(context: Context): String {
        return getPersistedData(context, Locale.getDefault().language).toString()
    }

    //Определение языка
    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        return updateResources(context, language)
    }

    //Выбор языка
    private fun getPersistedData(context: Context, defaultLanguage: String): String? {
        //Получает SharedPreferences экземпляр, указывающий на файл по умолчанию, который используется платформой предпочтений в заданном контексте.
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage)
    }

    private fun persist(context: Context, language: String) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()

        editor.putString(SELECTED_LANGUAGE, language)
        editor.apply()
    }

    //Изменение языка
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    //Suppress -> Подавление
    @Suppress("DEPRECATION")
    //Обновление устаревших ресурсов
    private fun updateResourcesLegacy(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources

        val configuration = resources.configuration
        configuration.locale = locale
        configuration.setLayoutDirection(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)

        return context
    }
}