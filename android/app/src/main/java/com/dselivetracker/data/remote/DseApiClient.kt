package com.dselivetracker.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object DseApiClient {
    private const val QUOTES_URL = "https://www.dsebd.org/datafile/quotes.txt"
    private const val PROXY_BASE = "https://corsproxy.io/?"
    private const val TIMEOUT_MS = 10000L
    private const val MAX_RETRIES = 2
    private const val RETRY_DELAY_MS = 1500L

    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .readTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetchQuotes(): String = withContext(Dispatchers.IO) {
        val urls = listOf(
            QUOTES_URL,
            PROXY_BASE + URLEncoder.encode(QUOTES_URL, "UTF-8")
        )

        for (url in urls) {
            for (attempt in 0..MAX_RETRIES) {
                try {
                    val request = Request.Builder()
                        .url(url)
                        .cacheControl(CacheControl.Builder().noCache().build())
                        .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        return@withContext response.body?.string() ?: throw Exception("Empty response")
                    }
                    response.close()
                } catch (e: Exception) {
                    if (attempt < MAX_RETRIES) delay(RETRY_DELAY_MS)
                }
            }
        }
        throw Exception("All sources failed")
    }
}
