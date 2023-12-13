package com.example.soft1c.network

import com.example.soft1c.utils.Utils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

object Network {

    lateinit var client: OkHttpClient
//    lateinit var retrofit: Retrofit

    fun refreshConnection(){
        Client = OkHttpClient.Builder()
            .addInterceptor(BasicAuthInterceptor())
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(Utils.clientTimeout, TimeUnit.SECONDS) // set the connection timeout to 30 seconds
            .readTimeout(Utils.clientTimeout, TimeUnit.SECONDS) // set the read timeout to 30 seconds
            .writeTimeout(Utils.clientTimeout, TimeUnit.SECONDS) // set the write timeout to 30 seconds
            .build()

        acceptanceRetrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .client(Client)
            .baseUrl(Utils.base_url+Utils.acceptance_auth)
            .build()

        autoRetrofit = Retrofit.Builder()
            //Конвертер, который позволяет сериализовать и десериализовать JSON данные.
            .addConverterFactory(MoshiConverterFactory.create())
            .client(Client)
            .baseUrl(Utils.base_url + Utils.auth)
            .build()

        loadingRetrofit = Retrofit.Builder()
            //Конвертер, который позволяет сериализовать и десериализовать JSON данные.
            .addConverterFactory(MoshiConverterFactory.create())
            .client(Client)
            .baseUrl(Utils.base_url + Utils.loading_auth)
            .build()
    }

    fun getBaseUrl(): String {
        return autoRetrofit.baseUrl().toUrl().toString()
    }

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
        builder.addInterceptor(BasicAuthInterceptor())
        return builder
    }

    // Объекта OkHttpClient, который предоставляет средства для работы с сетевыми запросами и ответами.
    private var Client = OkHttpClient.Builder()
        .addInterceptor(BasicAuthInterceptor())
        .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .connectTimeout(Utils.clientTimeout, TimeUnit.SECONDS) // set the connection timeout to 30 seconds
        .readTimeout(Utils.clientTimeout, TimeUnit.SECONDS) // set the read timeout to 30 seconds
        .writeTimeout(Utils.clientTimeout, TimeUnit.SECONDS) // set the write timeout to 30 seconds
        .build()

    // Объекта Retrofit, который предоставляет средства для выполнения запросов к API.
    private var autoRetrofit = Retrofit.Builder()
        //Конвертер, который позволяет сериализовать и десериализовать JSON данные.
        .addConverterFactory(MoshiConverterFactory.create())
        .client(Client)
        .baseUrl(Utils.base_url + Utils.auth)
        .build()

    val Api: AutoApi = autoRetrofit.create(AutoApi::class.java)


    // Объекта Retrofit, который предоставляет средства для выполнения запросов к API.
    private var loadingRetrofit = Retrofit.Builder()
        //Конвертер, который позволяет сериализовать и десериализовать JSON данные.
        .addConverterFactory(MoshiConverterFactory.create())
        .client(Client)
        .baseUrl(Utils.base_url + Utils.loading_auth)
        .build()

    // Объект CarApi, который будет использоваться для выполнения запросов к API. Оно будет иметь методы для выполнения запросов, определенные в интерфейсе CarApi.
    val loadingApi: LoadingApi = loadingRetrofit.create(LoadingApi::class.java)

    private var acceptanceRetrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create())
        .client(Client)
        .baseUrl(Utils.base_url+Utils.acceptance_auth)
        .build()

    val api: BaseApi = acceptanceRetrofit.create(BaseApi::class.java)




    const val KEY_BASENAME = "key_basename"
    const val KEY_USERNAME = "key_username"
    const val KEY_PASSWORD = "key_password"
    const val KEY_BASE_URL = "key_base_url"
    const val KEY_ADDRESS = "key_address"
    const val KEY_PORT = "key_port"
    const val KEY_MACADDRESS = "key_port"
}