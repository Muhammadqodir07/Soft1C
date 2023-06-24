package com.example.soft1c.repository.model

data class Loading(
    val number: String,
    var ref: String = "",
    var guid: String = "",
    var date: String = "",
    var carUid: String = "",
    var car: String = "",
    var senderWarehUid: String = "",
    var senderWareh: String = "",
    var getterWarehUid: String = "",
    var getterWareh: String = "",
    var barcodes: List<String> = listOf()
)
