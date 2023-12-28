package com.example.soft1c.repository.model

sealed class AnyModel {

    data class AddressModel(val ref: String, val name: String, val code: String) :
        AnyModel(){
            companion object{
                const val DEFAULT_DATA = """
                    [
{
"Ссылка": "6fd2d0f7-4ed7-11ee-9100-000c29ed8257",
"Наименование": "Ichki Munggul",
"Код": "00099"
},
{
"Ссылка": "38236ca4-3672-11ea-9912-48a47273c949",
"Наименование": "Xiyu 1-pres ",
"Код": "00025"
},
{
"Ссылка": "38236ca5-3672-11ea-9912-48a47273c949",
"Наименование": "135 skl 1 pres",
"Код": "00026"
}]
                """
            }
        }

    data class PackageModel(val ref: String, val name: String, val code: String) :
        AnyModel(){
        companion object{
            const val DEFAULT_DATA = """
                    [
{
"Ссылка": "6da09485-4dbf-11e2-9401-00262d0aaf56",
"Наименование": "Seyf 10",
"Код": "10",
"НаименованиеНаКитайском": "Seyf 10"
},
{
"Ссылка": "4cee5deb-4755-11e1-9f02-e0b9a59b5726",
"Наименование": "Korobka 1",
"Код": "1",
"НаименованиеНаКитайском": "Korobka 1"
},
{
"Ссылка": "4cee5dec-4755-11e1-9f02-e0b9a59b5726",
"Наименование": "Meshok 2",
"Код": "2",
"НаименованиеНаКитайском": "Meshok 2"
},
{
"Ссылка": "4cee5ded-4755-11e1-9f02-e0b9a59b5726",
"Наименование": "Pres 3",
"Код": "3",
"НаименованиеНаКитайском": "Pres 3"
},
{
"Ссылка": "6b38d787-5d6d-11e1-b652-00262d0aaf57",
"Наименование": "Rulon 4",
"Код": "4",
"НаименованиеНаКитайском": "Rulon 4"
},
{
"Ссылка": "6b38d790-5d6d-11e1-b652-00262d0aaf57",
"Наименование": "Yashik 5",
"Код": "5",
"НаименованиеНаКитайском": "Yashik 5"
},
{
"Ссылка": "6b38d791-5d6d-11e1-b652-00262d0aaf57",
"Наименование": "Bez upakovki 6",
"Код": "6",
"НаименованиеНаКитайском": "Bez upakovki 6"
},
{
"Ссылка": "6b38d792-5d6d-11e1-b652-00262d0aaf57",
"Наименование": "Bochka 7",
"Код": "7",
"НаименованиеНаКитайском": "Bochka 7"
},
{
"Ссылка": "079deb80-5dd6-11e1-b652-00262d0aaf57",
"Наименование": "Poddon 8",
"Код": "8",
"НаименованиеНаКитайском": "Poddon 8"
},
{
"Ссылка": "36e0038f-77ca-11e1-b9d4-00262d0aaf56",
"Наименование": "Pres zavodskoy 9",
"Код": "9",
"НаименованиеНаКитайском": "Pres zavodskoy 9"
}
]
                """
        }
    }

    data class ProductType(val ref: String, val name: String, val code: String) :
        AnyModel(){
            companion object{
                const val DEFAULT_DATA = """
                    [
{
"Ссылка": "56cf65ed-6aa5-11e1-b9d0-00262d0aaf57",
"Наименование": "Posuda 2",
"Код": "2",
"НаименованиеНаКитайском": "Posuda 2"
},
{
"Ссылка": "a9269890-a14f-11e5-832b-005056c00008",
"Наименование": "Attorlik buyumlari 6",
"Код": "6",
"НаименованиеНаКитайском": "Attorlik buyumlari 6"
},
{
"Ссылка": "fd394e6e-6b4a-11e1-b9d0-00262d0aaf56",
"Наименование": "AVTO Karcher 11",
"Код": "11",
"НаименованиеНаКитайском": "AVTO Karcher 11"
}]
                """
            }
        }

    data class Zone(val ref: String, val name: String, val code: String) :
        AnyModel(){
        companion object{
            const val DEFAULT_DATA = """
[
{
"Ссылка": "cb83e897-7257-11ea-91ec-b82a72dc4ef3",
"Наименование": "17",
"Код": "000000053"
},
{
"Ссылка": "e938ddc1-7257-11ea-91ec-b82a72dc4ef3",
"Наименование": "18",
"Код": "000000054"
},
{
"Ссылка": "e938ddc2-7257-11ea-91ec-b82a72dc4ef3",
"Наименование": "19",
"Код": "000000055"
}]"""
        }
    }
}