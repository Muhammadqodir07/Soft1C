package com.example.soft1c.repository.model

import com.example.soft1c.R

data class Client(
    val code: String = "",
    val serialDoc: String = "",
    val numberDoc: String = "",
    val haveRepresentative: Boolean = false,
    val serialRepr: String = "",
    val numberRepr: String = "",
    val blockAcceptance: Boolean = false,
    val blacklist: Boolean = false
) {

    fun isPassportNumberMatching(enteredNumber: String): Pair<Boolean, Int> {
        if (blockAcceptance) return Pair(false, R.string.txt_is_blacklist)
        if (blacklist) return Pair(false, R.string.txt_client_acceptance_blocked)

        if (enteredNumber == numberDoc)
            return Pair(true, 0)

        if (haveRepresentative && enteredNumber == numberRepr)
            return Pair(true, 0)

        return Pair(false, R.string.txt_error_passport)
    }

    companion object {
        const val CODE_KEY = "КодКлиента"
        const val SERAIL_DOC_KEY = "СерияПаспорта"
        const val NUMBER_DOC_KEY = "НомерПаспорта"
        const val HAVE_REPRESENTATIVE_KEY = "ЕстьПредставитель"
        const val SERIAL_REPRESENTATIVE_KEY = "СерияПаспортаПредставителя"
        const val NUMBER_REPRESENTATIVE_KEY = "НомерПаспортаПредставителя"
        const val BLOCK_ACCEPTANCE_KEY = "ЗаблокироватьПриемВременно"
        const val BLACKLIST_KEY = "ЧерныйСписок"

        const val DEFAULT_DATA = """[
            {
                "КодКлиента": "00000",
                "СерияПаспорта": "AA",
                "НомерПаспорта": "123456789"
            }]"""
    }
}