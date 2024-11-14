package com.example.soft1c.repository.model

data class Loading(
    val number: String,
    var ref: String = "",
    var guid: String = "",
    var date: String = "",
    var car: LoadingModel.Car= LoadingModel.Car(),
    var senderWarehouse: LoadingModel.Warehouse = LoadingModel.Warehouse(),
    var getterWarehouse: LoadingModel.Warehouse = LoadingModel.Warehouse(),
    // Штрих-коды передней части машины
    var barcodesFront: List<LoadingBarcode> = listOf(),
    //Штрих-коды задней части машины
    var barcodesBack: List<LoadingBarcode> = listOf(),
    var container: LoadingModel.Container = LoadingModel.Container()
){
    companion object{
        const val REF_KEY = "GUID"
        const val NUMBER_KEY = "Номер"
        const val GUID_KEY = "GUIDНаСервере"
        const val DATE_KEY = "Дата"
        const val CAR_KEY = "Машина"
        const val WAREHOUSE_BEGIN_KEY = "Отправитель"
        const val WAREHOUSE_END_KEY = "Получатель"
        const val DOCUMENT_DATE_KEY = "ДатаДокумента" // Дата в списке
        const val CAR_NUMBER_KEY = "НомерМашины" //В списке
        const val ITEMS_FRONT_KEY = "ТоварыПеред"
        const val ITEMS_BACK_KEY = "ТоварыЗад"
        const val BARCODE_KEY = "Штрихкод"

        const val DEFAULT_DATA_LIST ="""[
            {
                "GUIDНаСервере": "1f0abfb0-9fe9-11ee-99d1-48a47273c949",
                "НомерМашины": "c0cc9ef8-cf98-11ed-90f4-000c29ed824d",
                "ДатаДокумента": "2023-12-21T15:10:11",
                "GUID": "6a931d05-ea32-4232-aef5-76a9a444b27d",
                "Номер": "YI0000001",
                "Отправитель": "e4ed54f8-380d-11ea-9912-48a47273c949",
                "Получатель": "a86db662-a170-11ea-bcca-b82a72dc4ef3",
                "ОтправительНаименование": "Yiwu 义乌",
                "ПолучательНаименование": "Alashankou"
            },
            {
                "GUIDНаСервере": "fcc3b7d7-9fee-11ee-99d1-48a47273c949",
                "НомерМашины": "6d71ed0b-7e10-11ee-910a-000c29ed8257",
                "ДатаДокумента": "2023-12-21T15:14:58",
                "GUID": "fb4200c2-3f27-4422-b187-ab2c6f3f672f",
                "Номер": "YI0000002",
                "Отправитель": "e4ed54f8-380d-11ea-9912-48a47273c949",
                "Получатель": "2b1bfb4c-7ddf-11ec-80d5-000c29508723",
                "ОтправительНаименование": "Yiwu 义乌",
                "ПолучательНаименование": "Andijan Dustlik"
            }
        ]"""
    }
}
