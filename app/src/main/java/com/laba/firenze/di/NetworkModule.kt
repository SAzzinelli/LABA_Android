package com.laba.firenze.di

import com.laba.firenze.BuildConfig
import com.laba.firenze.data.api.AuthApi
import com.laba.firenze.data.api.AuthHeaderInterceptor
import com.laba.firenze.data.api.GestionaleApi
import com.laba.firenze.data.api.LogosUniApiService
import com.laba.firenze.data.api.LogosUniAPIClient
import com.laba.firenze.data.api.SuperSaasApi
import com.laba.firenze.data.api.SuperSaasAuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // private const val BASE_URL = "https://logosuni.laba.biz/" (Dynamic now)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authHeaderInterceptor: AuthHeaderInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authHeaderInterceptor) // Aggiunge Bearer token automaticamente
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthRetrofit(
        okHttpClient: OkHttpClient,
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): Retrofit {
        val prefs = context.getSharedPreferences("laba_preferences", android.content.Context.MODE_PRIVATE)
        val version = prefs.getString("laba.apiVersion", "v2") ?: "v2"
        
        val baseUrl = if (version == "v3") {
            "https://logosuni.laba.biz/identityserver-test/"
        } else {
            "https://logosuni.laba.biz/identityserver/"
        }
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    @javax.inject.Named("ApiRetrofit") // Use Named to distinguish
    fun provideApiRetrofit(
        okHttpClient: OkHttpClient,
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): Retrofit {
        val prefs = context.getSharedPreferences("laba_preferences", android.content.Context.MODE_PRIVATE)
        val version = prefs.getString("laba.apiVersion", "v2") ?: "v2"
        
        // Allineato a iOS APIConfig: v2 prod = api/api, v3 test = api-test/api
        val baseUrl = if (version == "v3") {
            "https://logosuni.laba.biz/api-test/api/"
        } else {
            "https://logosuni.laba.biz/api/api/"
        }
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi { // Uses Default (Auth) Retrofit (unnamed)
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideLogosUniApiService(@javax.inject.Named("ApiRetrofit") retrofit: Retrofit): LogosUniApiService {
        return retrofit.create(LogosUniApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideLogosUniAPIClient(
        apiService: LogosUniApiService,
        gson: Gson,
        @dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context
    ): LogosUniAPIClient {
        return LogosUniAPIClient(apiService, gson, context)
    }
    
    @Provides
    @Singleton
    @javax.inject.Named("SupabaseRetrofit")
    fun provideSupabaseRetrofit(okHttpClient: OkHttpClient): Retrofit {
        // Supabase URL is static
        val baseUrl = "https://ikbbmgobwkobcsmvwatq.supabase.co/"
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(
                com.google.gson.GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // Supabase might return fractionals, handled by models if needed but default is standard ISO
                    .create()
            ))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideSupabaseApi(@javax.inject.Named("SupabaseRetrofit") retrofit: Retrofit): com.laba.firenze.data.api.SupabaseApi {
        return retrofit.create(com.laba.firenze.data.api.SupabaseApi::class.java)
    }
    
    /** Service LABA (Gestionale attrezzatura) - base https://attrezzatura.laba.biz/api */
    @Provides
    @Singleton
    @javax.inject.Named("GestionaleRetrofit")
    fun provideGestionaleRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://attrezzatura.laba.biz/api/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideGestionaleApi(@javax.inject.Named("GestionaleRetrofit") retrofit: Retrofit): GestionaleApi {
        return retrofit.create(GestionaleApi::class.java)
    }

    /** SuperSaas - base https://www.supersaas.it, Basic Auth account:api_key */
    @Provides
    @Singleton
    @javax.inject.Named("SuperSaasRetrofit")
    fun provideSuperSaasRetrofit(): Retrofit {
        val authInterceptor = SuperSaasAuthInterceptor("LABA_FIRENZE", BuildConfig.SUPERSAAS_API_KEY)
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://www.supersaas.it/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideSuperSaasApi(@javax.inject.Named("SuperSaasRetrofit") retrofit: Retrofit): SuperSaasApi {
        return retrofit.create(SuperSaasApi::class.java)
    }
}
