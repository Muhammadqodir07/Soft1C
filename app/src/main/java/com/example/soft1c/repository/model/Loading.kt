package com.example.soft1c.repository.model

data class Loading(
    val number: String,
    var ref: String = "",
    var guid: String = "",
    var date: String = "",
    var car: LoadingModel.Car= LoadingModel.Car(),
    var senderWarehouseUid: String = "",
    var senderWarehouse: String = "",
    var getterWarehouseUid: String = "",
    var getterWarehouse: String = "",
    var recipient: LoadingModel.Warehouse = LoadingModel.Warehouse(),
    // Штрих-коды передней части машины
    var barcodesFront: List<LoadingBarcode> = listOf(),
    //Штрих-коды задней части машины
    var barcodesBack: List<LoadingBarcode> = listOf()
){
    companion object{
        const val REF_KEY = "GUID"
        const val NUMBER_KEY = "Номер"
        const val GUID_KEY = "GUIDНаСервере"
        const val DATE_KEY = "Дата"
        const val CAR_KEY = "Машина"
        const val WAREHOUSE_BEGIN_KEY = "СкладНачало"
        const val WAREHOUSE_END_KEY = "СкладКонец"
        const val SENDER_WAREHOUSE_UID_KEY = "Отправитель"
        const val SENDER_WAREHOUSE_KEY = "ОтправительНаименование"
        const val GETTER_WAREHOUSE_UID_KEY = "Получатель"
        const val GETTER_WAREHOUSE_KEY = "ПолучательНаименование"
        const val RECIPIENT_KEY = "СкладПолучатель"
        const val DOCUMENT_DATE_KEY = "ДатаДокумента" // Дата в списке
        const val CAR_NUMBER_KEY = "НомерМашины"
        const val ITEMS_FRONT_KEY = "ТоварыПеред"
        const val ITEMS_BACK_KEY = "ТоварыЗад"
    }
}
