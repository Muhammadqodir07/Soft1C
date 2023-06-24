package com.example.soft1c.repository.model

data class User(var username: String = "",
                var password: String = "",
//                var acceptanceAccess: Boolean = false,
//                var loadingAccess: Boolean = false,
                var isAdmin: Boolean = false,
                var weightAccess: Boolean = false,
                var measureCargo: Boolean = false,
                var acceptanceCargo: Boolean = false)
