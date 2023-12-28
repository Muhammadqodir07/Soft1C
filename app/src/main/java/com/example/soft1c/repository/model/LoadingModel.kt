package com.example.soft1c.repository.model

sealed class LoadingModel{
    data class Car(
        var ref: String = "", // Ссылка
        var number: String = "" //Номер
    ) : LoadingModel(){
        companion object{
            const val REF_KEY = "GUID"
            const val NUMBER_KEY = "Номер"

            const val DEFAULT_DATA = """[
{
"GUID": "6d71ed0b-7e10-11ee-910a-000c29ed8257",
"Номер": ".新AF4923"
},
{
"GUID": "c786c86b-c6e9-11ed-90f3-000c29ed824d",
"Номер": "00007OC"
},
{
"GUID": "c0cc9ef8-cf98-11ed-90f4-000c29ed824d",
"Номер": "0007OC"
}]"""
        }
    }

    data class Warehouse(
        var ref: String = "", // Ссылка
        var name: String = "", //Наименование
        var prefix: String = "" //Краткое наименование
    ) : LoadingModel(){
        companion object{
            const val REF_KEY = "GUID"
            const val NAME_KEY = "Наименование"
            const val PREFIX_KEY = "ПрефиксДляНумерации"

            const val DEFAULT_DATA = """[
{
"GUID": "a86db662-a170-11ea-bcca-b82a72dc4ef3",
"Наименование": "Alashankou",
"ПрефиксДляНумерации": "ALK"
},
{
"GUID": "2b1bfb4c-7ddf-11ec-80d5-000c29508723",
"Наименование": "Andijan Dustlik",
"ПрефиксДляНумерации": "AD"
},
{
"GUID": "a3c0a10d-cb65-11eb-80dd-b88303f1228d",
"Наименование": "Bahtu",
"ПрефиксДляНумерации": "BHT"
}]"""
        }
    }
}
