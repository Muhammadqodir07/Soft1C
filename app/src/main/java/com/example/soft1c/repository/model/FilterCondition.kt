package com.example.soft1c.repository.model

object Filter{
    var client: String = ""
    var _package: String = ""
    var zone: String = ""
    var weight: Boolean? = null
    var size: Boolean? = null
    var ascending: Pair<Int, Boolean> =Pair(-1, false)
    const val DOCUMENT = 0
    const val CLIENT = 1
    const val PACKAGE = 2
    const val ZONE = 3
    const val WEIGHT = 4
    const val SIZE = 5
}
