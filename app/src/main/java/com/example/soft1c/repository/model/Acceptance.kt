package com.example.soft1c.repository.model

data class Acceptance(
    var number: String, // Номер
    var ref: String = "", // Ссылка
    var client: Client = Client(), // Клиент
    var date: String = "", // Дата
    var weight: Boolean = false, //
    var capacity: Boolean = false,
    var z: Boolean = false, // ZТовар
    var brand: Boolean = false, //Брэнд
    var glass: Boolean = false, //Стекло
    var expensive: Boolean = false, //Дорогой
    var notTurnOver: Boolean = false, //Некантовать
    var zone: String = "",
    var autoNumber: String = "", //НомерАвто
    var idCard: String = "", //IDПродавца
    var trackNumber: String = "", // Номер трека
    var productTypeName: String = "",
    var storeName: String = "", // НаименованиеМагазина
    var storeAddressName: String = "", // НаименованиеАдресаМагазина
    var phoneNumber: String = "", //ТелефонМагазина
    var representativeName: String = "", // ИмяПредставителя
    var countInPackage: Int = 0, // КоличествоВУпаковке
    var countPackage: Int = 1, // КоличествоТиповУпаковок
    var countSeat: Int = 0, // КоличествоМест
    var allWeight: Double = 0.0, // ОбщийВес
    var _package: String = "",
    var packageUid: String = "", //ТипУпаковки
    var storeUid: String = "", //АдресМагазина
    var zoneUid: String = "", // Зона
    var productType: String = "", // ВидТовара
    var batchGuid: String = "", // GUIDПартии
    var creator:String = "",
    var whoAccept: String = "", //Тот кто принял
    var whoWeigh: String = "", //Тот кто взвесил
    var whoMeasure: String = "", //Тот кто измерил
    var isPrinted: Boolean = false, //Напечатан
    var type: Int = -1,
    var correctPassport: Boolean = false
) {

    companion object{
        const val LIST_DEFAULT_DATA = """
            [
{
"Ссылка": "c6e9e920-f301-11ee-9120-000c29ed8257",
"Номер": "YI0024461",
"Клиент": "80076",
"ТипУпаковки": "6b38d790-5d6d-11e1-b652-00262d0aaf57",
"Дата": "2024-04-05T09:05:46",
"Вес": false,
"Замер": false,
"ВидТовара": "fd394ed2-6b4a-11e1-b9d0-00262d0aaf56",
"Зона": "d629375f-57c5-11ea-992b-48a47273c949",
"ВидТовараНаименование": "ABP Batareya bitovaya",
"ТипУпаковкиНаименование": "Yashik",
"ЗонаНаименование": "18",
"Напечатан": true
},
{
"Ссылка": "6ffd635f-f2fa-11ee-9120-000c29ed8257",
"Номер": "YI0024441",
"Клиент": "86404",
"ТипУпаковки": "6b38d790-5d6d-11e1-b652-00262d0aaf57",
"Дата": "2024-04-05T08:13:18",
"Вес": true,
"Замер": false,
"ВидТовара": "2d42f165-519b-11e7-b3ca-005056c00001",
"Зона": "d629375f-57c5-11ea-992b-48a47273c949",
"ВидТовараНаименование": "Flakon va raspilitellar",
"ТипУпаковкиНаименование": "Yashik",
"ЗонаНаименование": "18",
"Напечатан": true
},
{
"Ссылка": "32f691d7-f31c-11ee-9120-000c29ed8257",
"Номер": "YI0024509",
"Клиент": "20743",
"ТипУпаковки": "079deb80-5dd6-11e1-b652-00262d0aaf57",
"Дата": "2024-04-05T12:14:54",
"Вес": true,
"Замер": false,
"ВидТовара": "83ca0eaf-6f7e-11e7-a3c9-005056c00001",
"Зона": "d629373d-57c5-11ea-992b-48a47273c949",
"ВидТовараНаименование": "Plastikovaya posuda",
"ТипУпаковкиНаименование": "Poddon",
"ЗонаНаименование": "05",
"Напечатан": true
},
{
"Ссылка": "7c476a4b-f311-11ee-9120-000c29ed8257",
"Номер": "YI0024494",
"Клиент": "30006",
"ТипУпаковки": "4cee5deb-4755-11e1-9f02-e0b9a59b5726",
"Дата": "2024-04-05T10:58:16",
"Вес": true,
"Замер": false,
"ВидТовара": "59d8719c-2a6a-11e8-a79f-f01fafe5916d",
"Зона": "d629373d-57c5-11ea-992b-48a47273c949",
"ВидТовараНаименование": "Shampun ",
"ТипУпаковкиНаименование": "Korobka",
"ЗонаНаименование": "05",
"Напечатан": false
},
{
"Ссылка": "dc470c60-f334-11ee-9120-000c29ed8257",
"Номер": "YI0024616",
"Клиент": "32277",
"ТипУпаковки": "36e0038f-77ca-11e1-b9d4-00262d0aaf56",
"Дата": "2024-04-05T15:11:26",
"Вес": true,
"Замер": false,
"ВидТовара": "0cea0e44-e3ea-11e4-84ca-f01fafe5916d",
"Зона": "d6293749-57c5-11ea-992b-48a47273c949",
"ВидТовараНаименование": "Sharf (ayol)",
"ТипУпаковкиНаименование": "Pres zavodskoy",
"ЗонаНаименование": "09",
"Напечатан": true
},
{
"Ссылка": "2040aa85-f335-11ee-9120-000c29ed8257",
"Номер": "YI0024617",
"Клиент": "80889",
"ТипУпаковки": "36e0038f-77ca-11e1-b9d4-00262d0aaf56",
"Дата": "2024-04-05T15:13:26",
"Вес": true,
"Замер": false,
"ВидТовара": "0cea0e44-e3ea-11e4-84ca-f01fafe5916d",
"Зона": "d6293749-57c5-11ea-992b-48a47273c949",
"ВидТовараНаименование": "Sharf (ayol)",
"ТипУпаковкиНаименование": "Pres zavodskoy",
"ЗонаНаименование": "09",
"Напечатан": true
}
]
        """
    }
}