package com.example.soft1c.repository.model

sealed class AnyModel {

    data class AddressModel(val ref: String, val name: String, val code: String) :
        AnyModel()

    data class PackageModel(val ref: String, val name: String, val code: String) :
        AnyModel()

    data class ProductType(val ref: String, val name: String, val code: String) :
        AnyModel()

    data class Zone(val ref: String, val name: String, val code: String) :
        AnyModel()
}