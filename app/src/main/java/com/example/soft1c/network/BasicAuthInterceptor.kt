package com.example.soft1c.network

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import kotlin.text.Charsets.UTF_8

class BasicAuthInterceptor(private val userName:String, private val password:String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val credentials = Credentials.basic(userName,password,UTF_8)
        request = request.newBuilder()
            .addHeader("Authorization", credentials)
            .build()
        return chain.proceed(request)
    }
}