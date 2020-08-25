package com.appfab.cozgec.network

import android.util.Log
import com.mfpolat.starter.base.BaseNavigator
import com.mfpolat.starter.utils.CacheManager
import com.mfpolat.starter.utils.isOnline
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiController(val baseUrl: String) : KoinComponent {


    val gson: Gson by lazy {
        Gson()
    }
    val cacheManager: CacheManager by inject()
    var okHttpClient: OkHttpClient? = null
    var retrofit: Retrofit? = null

    fun addTokenToHeader(token: String): Retrofit {

        val interceptor = Interceptor { chain ->

            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Expect", "application/json")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(interceptor)
        builder.addInterceptor(loggingInterceptor)
        builder.connectTimeout(60, TimeUnit.SECONDS)
        builder.readTimeout(60, TimeUnit.SECONDS)
        builder.writeTimeout(60, TimeUnit.SECONDS)

        okHttpClient = builder.build()

        retrofit = Retrofit.Builder()
                .client(okHttpClient!!)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit!!

    }

    fun buildWithoutToken(): Retrofit {

        val interceptor = Interceptor { chain ->

            val requestBuilder = chain.request().newBuilder()
            requestBuilder.addHeader("Content-Type", "application/json")
            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(interceptor)
        builder.addInterceptor(loggingInterceptor)
        builder.connectTimeout(60, TimeUnit.SECONDS)
        builder.readTimeout(60, TimeUnit.SECONDS)
        builder.writeTimeout(60, TimeUnit.SECONDS)
        okHttpClient = builder.build()
        retrofit = Retrofit.Builder()
                .client(okHttpClient!!)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        return retrofit!!
    }

    fun <DATAMODEL> fetch(
            call: Call<DATAMODEL?>,
            successFunction: (DATAMODEL) -> Unit = {},
            errorFunction: (ApiErrorResponse) -> Unit = { apierrorResponse -> },
            navigator: BaseNavigator? = null,
            async: Boolean = true
    ) {
        if (async) fetch(call, successFunction, errorFunction, navigator)
        else get(call, successFunction, errorFunction, navigator)
    }


    private fun <DATAMODEL> fetch(
            call: Call<DATAMODEL?>,
            successFunc: (DATAMODEL) -> Unit = {},
            errorFunc: (ApiErrorResponse) -> Unit = {},
            navigator: BaseNavigator? = null
    ) {
        if (!cacheManager.context.isOnline()) {
            throwError(
                    errorFunc,
                    ApiErrorResponse(
                            type = "no_network",
                            title = "Lütfen internet bağlantınızı kontrol edin",
                            status = 0,
                            invalid_params = listOf(InvalidParam("dummy", listOf())),
                            detail = "",
                            instance = ""
                    ),
                    navigator
            )
            return
        }

        call.clone().enqueue(object : Callback<DATAMODEL?> {
            override fun onFailure(call: Call<DATAMODEL?>, t: Throwable) {
                Log.e("NetworkError", t.localizedMessage)
                throwError(
                        errorFunc, ApiErrorResponse(
                        type = "Connection Error",
                        title = t.localizedMessage,
                        status = 0,
                        invalid_params = listOf(InvalidParam("dummy", listOf())),
                        detail = "",
                        instance = ""
                ), navigator
                )
            }

            override fun onResponse(call: Call<DATAMODEL?>, response: Response<DATAMODEL?>) {
                handleResponse(call, response, errorFunc, successFunc, navigator)
            }

        })
    }

    private fun <DATAMODEL> get(
            call: Call<DATAMODEL?>,
            successFunction: (DATAMODEL) -> Unit = {},
            errorFunction: (ApiErrorResponse) -> Unit = { apiErrorResponse -> },
            navigator: BaseNavigator? = null
    ) {
        if (!cacheManager.context.isOnline()) {
            throwError(
                    errorFunction,
                    ApiErrorResponse(
                            type = "no_network",
                            title = "Lütfen internet bağlantınızı kontrol edin",
                            status = 0,
                            invalid_params = listOf(InvalidParam("dummy", listOf())),
                            detail = "",
                            instance = ""
                    ),
                    navigator
            )
            return
        }


        var resp: Response<DATAMODEL?>? = null
        try {
            resp = call.clone().execute()
            resp?.let { safeResponse ->
                handleResponse(call, safeResponse, errorFunction, successFunction, navigator)
            } ?: throwError(
                    errorFunction, ApiErrorResponse(
                    type = "Connection Error",
                    title = "Null Response",
                    status = 0,
                    invalid_params = listOf(InvalidParam("dummy", listOf())),
                    detail = "",
                    instance = ""
            ), navigator
            )

        } catch (e: Exception) {
            navigator?.unhandledError()
        }
    }

    private fun <DATAMODEL> handleResponse(
            call: Call<DATAMODEL?>,
            safeResponse: Response<DATAMODEL?>,
            errorFunction: (ApiErrorResponse) -> Unit,
            successFunction: (DATAMODEL) -> Unit,
            navigator: BaseNavigator?
    ) {
        try {
            val responseBody = safeResponse.body()
            var errorBody: ApiErrorResponse? = null
            if (safeResponse.errorBody() != null)
                errorBody =
                        gson.fromJson(safeResponse.errorBody()?.string(), ApiErrorResponse::class.java)
            else errorBody = ApiErrorResponse(
                    type = "Connection Error",
                    title = "Beklenmeyen hata",
                    status = 0,
                    invalid_params = listOf(InvalidParam("dummy", listOf())),
                    detail = "",
                    instance = ""
            )

            when (safeResponse.code()) {
                200, 201 -> {
                    responseBody?.let { body ->
                        successFunction(body)
                    } ?: throwError(
                            errorFunction,
                            responseBody as ApiErrorResponse,
                            navigator
                    )
                }
                400, 401, 422 -> {
                    throwError(
                            errorFunction,
                            errorBody as ApiErrorResponse,
                            navigator
                    )
                }
                else -> {


                }
            }
        } catch (e: java.lang.Exception) {
            val apiErrorResponse = ApiErrorResponse(
                    type = "Connection Error",
                    title = "Beklenmeyen hata",
                    status = 0,
                    invalid_params = listOf(InvalidParam("dummy", listOf())),
                    detail = "",
                    instance = ""
            )
            throwError(
                    errorFunction,
                    apiErrorResponse,
                    navigator
            )
        }

    }

    private fun throwError(
            errorFunction: ((ApiErrorResponse) -> Unit)?,
            aresErrorResponse: ApiErrorResponse,
            navigator: BaseNavigator?
    ) {
        errorFunction?.invoke(aresErrorResponse)
        navigator?.unhandledError(null)
    }

    class EmptyData()


}