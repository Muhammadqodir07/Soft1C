package com.example.soft1c.repository.model

data class User(var username: String = "",
                var password: String = "",
                var warehouse: String = "",
                var acceptanceAccess: Boolean = true,
                var loadingAccess: Boolean = true,
                var isAdmin: Boolean = false,
                var weightAccess: Boolean = false,
                var measureCargo: Boolean = false,
                var acceptanceCargo: Boolean = false){

    companion object{
        const val ACCEPTANCE_RIGHT = "PriemkiAccess"
        const val LOADING_RIGHT = "PogruzkiAccess"
        const val WAREHOUSE = "UserSklad"
        const val ACCEPTANCE_ADD= "PriemkiCargo"
        const val SIZE_ADD = "MeasureCargo"
        const val WEIGHT_ADD = "Weighing"
        const val IS_ADMIN = "ЭтоАдмин"

        const val DEFAULT_DATA = """
                {
"ЭтоАдмин": true,
"Weighing": false,
"MeasureCargo": false,
"PriemkiCargo": false,
"PogruzkiAccess": false,
"UserSklad": "e4ed54f8-380d-11ea-9912-48a47273c949"
}
            """
    }
}
