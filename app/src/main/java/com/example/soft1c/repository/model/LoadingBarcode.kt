package com.example.soft1c.repository.model

data class LoadingBarcode(
    val barcode: String = "",
    val weight: Double = 0.0,
    val volume: Double = 0.0,
    val acceptanceNumber: String = "",
    val acceptanceUid: String = "",
    val seatNumber: Int = -1,
    val packageTypeUid: String = "",
    val packageType: String = "",
    val clientCode: String = "",
    val date: String = ""
) {
    companion object {
        const val BARCODE_KEY = "ШтрихКод"
        const val WEIGHT_KEY = "Вес"
        const val VOLUME_KEY = "Объем"
        const val DATE_KEY = "Дата"
        const val ACCEPTANCE_NUMBER_KEY = "НомерПриема"
        const val ACCEPTANCE_GUID_KEY = "ПриемТовара"
        const val SEAT_NUMBER_KEY = "НомерМеста"
        const val PACKAGE_KEY = "ТипУпаковки"
        const val CLIENT_CODE_KEY = "КодКлиента"

        const val DEFAULT_DATA = """
            [
{
"Объем": 0.017204,
"Вес": 8.4,
"ПриемТовара": "e164a1f5-a23e-11eb-80da-b88303f1228d",
"Дата": "2021-04-21T06:14:12",
"НомерПриема": "GZ0005731",
"ШтрихКод": "90000153429210421",
"НомерМеста": 1,
"КоличествоМест": 1,
"КодКлиента": "53429",
"ТипУпаковки": "4cee5deb-4755-11e1-9f02-e0b9a59b5726"
},
{
"Объем": 0.069498,
"Вес": 33.93,
"ПриемТовара": "e164a1f5-a23e-11eb-80da-b88303f1228d",
"Дата": "2021-04-21T06:14:12",
"НомерПриема": "GZ0005731",
"ШтрихКод": "90000253422210421",
"НомерМеста": 2,
"КоличествоМест": 1,
"КодКлиента": "53422",
"ТипУпаковки": "4cee5deb-4755-11e1-9f02-e0b9a59b5726"
},
{
"Объем": 0.06032,
"Вес": 29.45,
"ПриемТовара": "e164a1f5-a23e-11eb-80da-b88303f1228d",
"Дата": "2021-04-21T06:14:12",
"НомерПриема": "GZ0005731",
"ШтрихКод": "90000353423210421",
"НомерМеста": 3,
"КоличествоМест": 1,
"КодКлиента": "53423",
"ТипУпаковки": "4cee5deb-4755-11e1-9f02-e0b9a59b5726"
},
{
"Объем": 0.072504,
"Вес": 35.39,
"ПриемТовара": "e164a1f5-a23e-11eb-80da-b88303f1228d",
"Дата": "2021-04-21T06:14:12",
"НомерПриема": "GZ0005731",
"ШтрихКод": "90000453424210421",
"НомерМеста": 4,
"КоличествоМест": 1,
"КодКлиента": "53424",
"ТипУпаковки": "4cee5deb-4755-11e1-9f02-e0b9a59b5726"
}
]
        """
    }
}
