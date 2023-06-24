package com.example.soft1c.utils

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import timber.log.Timber
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class BasicAuthClient<T> {

    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocket = sslContext.socketFactory

        val builder = OkHttpClient.Builder()
        builder.sslSocketFactory(sslSocket, trustAllCerts[0] as X509TrustManager)
        builder.hostnameVerifier { _, _ -> true }
        builder.addInterceptor(
            com.example.soft1c.network.BasicAuthInterceptor(
                "Администратор",
                "1"
            )
        )
        return builder
    }

    private val client =  OkHttpClient.Builder()
        .addInterceptor(BasicAuthInterceptor("Администратор", "1"))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://81.95.233.209/tp-last/ru_RU/hs/PriemkiAPI/")
        .client(getUnsafeOkHttpClient().build())
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    fun create(service: Class<T>): T {
        return retrofit.create(service)
    }
}

interface DemoRemoteService {

    @GET("authorization")
    fun getProfile(): Call<ResponseBody>
}

class Demo {
    fun loadProfile() {
        val call = BasicAuthClient<DemoRemoteService>().create(DemoRemoteService::class.java).getProfile()

        call.enqueue(object: Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                //Пишет в лог ошибку t
                Timber.d(t)
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                   Timber.d("Profile Loaded.")
                } else {
                    Timber.d("Error: ${response.code()} ${response.message()}")
                }
            }
        })
    }
}

class BasicAuthInterceptor(username: String, password: String): Interceptor {
    private var credentials: String = Credentials.basic(username, password)

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        request = request.newBuilder().header("Authorization", credentials).build()

        return chain.proceed(request)
    }
}