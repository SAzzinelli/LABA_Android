package com.laba.firenze.data.api

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

/** Adds Basic Auth to SuperSaas requests: account:api_key */
class SuperSaasAuthInterceptor(
    private val account: String,
    private val apiKey: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val credentials = Credentials.basic(account, apiKey)
        val request = chain.request().newBuilder()
            .header("Authorization", credentials)
            .header("User-Agent", "Mozilla/5.0 (Android; LABA App)")
            .build()
        return chain.proceed(request)
    }
}
