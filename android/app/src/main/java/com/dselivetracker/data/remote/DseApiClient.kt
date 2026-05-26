package com.dselivetracker.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.Triple
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object DseApiClient {
    private const val QUOTES_URL = "https://www.dsebd.org/datafile/quotes.txt"
    private const val PROXY_BASE = "https://corsproxy.io/?"
    private const val FULL_QUOTES_URL = "https://www.dsebd.org/latest_share_price_scroll_l.php"
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

    suspend fun fetchFullQuotesHtml(): String? = withContext(Dispatchers.IO) {
        val urls = listOf(
            FULL_QUOTES_URL,
            PROXY_BASE + URLEncoder.encode(FULL_QUOTES_URL, "UTF-8")
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
                        return@withContext response.body?.string()
                    }
                    response.close()
                } catch (e: Exception) {
                    if (attempt < MAX_RETRIES) delay(RETRY_DELAY_MS)
                }
            }
        }
        null
    }

    suspend fun fetchHomepage(): String? = withContext(Dispatchers.IO) {
        val urls = listOf(
            "https://www.dsebd.org/",
            PROXY_BASE + URLEncoder.encode("https://www.dsebd.org/", "UTF-8")
        )
        for (url in urls) {
            for (attempt in 0..MAX_RETRIES) {
                try {
                    val request = Request.Builder().url(url)
                        .cacheControl(CacheControl.Builder().noCache().build()).build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) return@withContext response.body?.string()
                    response.close()
                } catch (e: Exception) {
                    if (attempt < MAX_RETRIES) delay(RETRY_DELAY_MS)
                }
            }
        }
        null
    }

    suspend fun fetchBothSources(): Pair<String?, String?> = withContext(Dispatchers.IO) {
        val def1 = async { try { fetchQuotes() } catch (e: Exception) { null } }
        val def2 = async { try { fetchFullQuotesHtml() } catch (e: Exception) { null } }
        val (r1, r2) = awaitAll(def1, def2)
        Pair(r1, r2)
    }

    suspend fun fetchAllThree(): Triple<String?, String?, String?> = withContext(Dispatchers.IO) {
        val def1 = async { try { fetchQuotes() } catch (e: Exception) { null } }
        val def2 = async { try { fetchFullQuotesHtml() } catch (e: Exception) { null } }
        val def3 = async { try { fetchHomepage() } catch (e: Exception) { null } }
        val (r1, r2, r3) = awaitAll(def1, def2, def3)
        Triple(r1, r2, r3)
    }
}
