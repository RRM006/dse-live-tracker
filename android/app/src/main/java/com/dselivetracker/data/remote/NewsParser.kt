package com.dselivetracker.data.remote

import org.jsoup.Jsoup

object NewsParser {

    data class NewsItem(
        val tradingCode: String,
        val title: String,
        val body: String
    )

    fun parseNews(html: String): List<NewsItem> {
        val doc = Jsoup.parse(html)
        val leftElements = doc.select("div.LeftColHome div.panel.panel-dse div.panel-body.panel-body-custom marquee left")
        val items = mutableListOf<NewsItem>()

        for (left in leftElements) {
            val anchors = left.select("a.ab1.np.ncol")
            if (anchors.size < 3) continue

            val tradingCode = anchors[0].text().trim().uppercase()
            val title = anchors[1].text().trim()
            val body = anchors[2].text().trim()

            if (tradingCode.isNotEmpty() && title.isNotEmpty()) {
                items.add(NewsItem(tradingCode = tradingCode, title = title, body = body))
            }
        }
        return items
    }
}
