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

    fun refreshConnection(timeout: Long){
        Client = getUnsafeOkHttpClient()
            .addInterceptor(BasicAuthInterceptor())
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(timeout, TimeUnit.SECONDS)
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .build()

        acceptanceRetrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .client(Client)
            .baseUrl(Utils.base_url+Utils.acceptance_auth)
            .build()

        autoRetrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .client(Client)
            .baseUrl(Utils.base_url + Utils.auth)
            .build()

        loadingRetrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .client(Client)
            .baseUrl(Utils.base_url + Utils.loading_auth)
            .build()

        api = acceptanceRetrofit.create(BaseApi::class.java)
        loadingApi = loadingRetrofit.create(LoadingApi::class.java)
        Api = autoRetrofit.create(AutoApi::class.java)
        checkConnectionApi = checkRetrofit.create(AutoApi::class.java)
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

    private var Client = getUnsafeOkHttpClient()
        .addInterceptor(BasicAuthInterceptor())
        .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .connectTimeout(Utils.authorizationTimeout, TimeUnit.SECONDS)
        .readTimeout(Utils.authorizationTimeout, TimeUnit.SECONDS)
        .writeTimeout(Utils.authorizationTimeout, TimeUnit.SECONDS)
        .build()

    private var autoRetrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create())
        .client(Client)
        .baseUrl(Utils.base_url + Utils.auth)
        .build()

    var Api: AutoApi = autoRetrofit.create(AutoApi::class.java)


    private var loadingRetrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create())
        .client(Client)
        .baseUrl(Utils.base_url + Utils.loading_auth)
        .build()

    var loadingApi: LoadingApi = loadingRetrofit.create(LoadingApi::class.java)

    private var acceptanceRetrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create())
        .client(Client)
        .baseUrl(Utils.base_url+Utils.acceptance_auth)
        .build()

    var api: BaseApi = acceptanceRetrofit.create(BaseApi::class.java)

    private var checkRetrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create())
        .client(Client)
        .baseUrl(Utils.base_url)
        .build()

    var checkConnectionApi: AutoApi = checkRetrofit.create(AutoApi::class.java)




    const val KEY_BASENAME = "key_basename"
    const val KEY_USERNAME = "key_username"
    const val KEY_PASSWORD = "key_password"
    const val KEY_BASE_URL = "key_base_url"
    const val KEY_ADDRESS = "key_address"
    const val KEY_PORT = "key_port"
    const val KEY_PROTOCOL = "key_protocol"
    const val KEY_MACADDRESS = "key_port"
}