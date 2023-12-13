package com.example.soft1c.network

import com.example.soft1c.utils.Utils
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import kotlin.text.Charsets.UTF_8

class BasicAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val credentials = Credentials.basic(Utils.username, Utils.password, UTF_8)
        request = request.newBuilder()
            .addHeader("Authorization", credentials)
            .build()
        return chain.proceed(request)
    }
}