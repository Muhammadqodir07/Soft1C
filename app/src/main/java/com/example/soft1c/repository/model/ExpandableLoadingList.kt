package com.example.soft1c.repository.model

class ExpandableLoadingList {
    companion object{
        const val PARENT = 1
        const val CHILD = 2
    }

    lateinit var loadingParent: LoadingBarcodeParent
    var type : Int = 0
    lateinit var loadingChild : LoadingBarcodeChild
    var isExpanded : Boolean = false

    constructor( type : Int, loadingParent: LoadingBarcodeParent, isExpanded : Boolean = false){
        this.type = type
        this.loadingParent = loadingParent
        this.isExpanded = isExpanded
    }

    constructor(type : Int, loadingChild : LoadingBarcodeChild, isExpanded : Boolean = false){
        this.type = type
        this.loadingChild = loadingChild
        this.isExpanded = isExpanded
    }
}