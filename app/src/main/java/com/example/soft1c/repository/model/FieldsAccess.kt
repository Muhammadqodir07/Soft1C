package com.example.soft1c.repository.model

data class FieldsAccess(
    var readOnly: Boolean = true,
    var weightEnable: Boolean = true,
    var sizeEnable: Boolean = true,
    var isCreator: Boolean = true,
    var zoneEnable: Boolean = true,
    var chBoxEnable: Boolean = true,
    var properties: Boolean = true,
    var packageEnable: Boolean = true
)
