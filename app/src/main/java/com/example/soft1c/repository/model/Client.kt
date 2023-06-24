package com.example.soft1c.repository.model

data class Client(val code: String = "",
                  val serialDoc: String = "",
                  val numberDoc: String = ""
                  ) {

    companion object {
        const val CODE_KEY = "КодКлиента"
        const val SERAIL_DOC_KEY = "СерияПаспорта"
        const val NUMBER_DOC_KEY = "НомерПаспорта"
    }
}