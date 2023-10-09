package com.example.soft1c.repository.model

data class FieldsAccess(
    var readOnly: Boolean = true,
    var weightEnable: Boolean = false,
    var sizeEnable: Boolean = false,
    var isCreator: Boolean = false,
    var zoneEnable: Boolean = false,
    var chBoxEnable: Boolean = false,
    var properties: Boolean = false,
    var packageEnable: Boolean = false
)
