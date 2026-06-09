package com.dselivetracker.data.remote

import org.jsoup.Jsoup

object QuotesParser {
    data class Quote(val symbol: String, val ltp: Double)

    data class ParsedQuotes(
        val quotes: List<Quote>,
        val timestamp: String?
    )

    data class StockQuoteFull(
        val symbol: String,
        val ltp: Double,
        val high: Double,
        val low: Double,
        val closep: Double,
        val ycp: Double,
        val change: Double,
        val pctChange: Double,
        val upperLimit: Double = 0.0,
        val lowerLimit: Double = 0.0,
        val category: String = ""
    )

    fun parseCbulHtml(html: String): Map<String, Pair<Double, Double>> {
        val doc = Jsoup.parse(html)
        val rows = doc.select("table tbody tr")
        val result = mutableMapOf<String, Pair<Double, Double>>()
        for (row in rows) {
            val cells = row.select("td")
            if (cells.size >= 3) {
                val symbol = cells[0].text().trim().uppercase()
                val ceiling = cells[1].text().replace(",", "").toDoubleOrNull() ?: 0.0
                val floor = cells[2].text().replace(",", "").toDoubleOrNull() ?: 0.0
                if (symbol.isNotEmpty() && ceiling > 0) {
                    result[symbol] = Pair(ceiling, floor)
                }
            }
        }
        return result
    }

    data class Top20Entry(
        val rank: Int,
        val symbol: String,
        val ltp: Double,
        val high: Double,
        val low: Double,
        val closep: Double,
        val ycp: Double,
        val change: Double,
        val pctChange: Double,
        val tradeCount: Long = 0,
        val value: Double = 0.0,
        val volume: Long = 0
    )

    fun parseTop20Html(html: String): List<Top20Entry> {
        val doc = Jsoup.parse(html)
        val rows = doc.select("table tbody tr")
        val result = mutableListOf<Top20Entry>()
        for (row in rows) {
            val cells = row.select("td")
            if (cells.size >= 12) {
                val symbol = cells[1].text().trim().uppercase()
                if (symbol.isNotEmpty()) {
                    result.add(Top20Entry(
                        rank = cells[0].text().toIntOrNull() ?: 0,
                        symbol = symbol,
                        ltp = cells[2].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                        high = cells[3].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                        low = cells[4].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                        closep = cells[5].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                        ycp = cells[6].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                        change = cells[7].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                        pctChange = cells[8].text().replace(",", "").replace("%", "").toDoubleOrNull() ?: 0.0,
                        tradeCount = cells[9].text().replace(",", "").toLongOrNull() ?: 0,
                        value = cells[10].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                        volume = cells[11].text().replace(",", "").toLongOrNull() ?: 0
                    ))
                }
            }
        }
        return result
    }

    fun parse(text: String): ParsedQuotes {
        val lines = text.split("\n")
        if (lines.size < 4) return ParsedQuotes(emptyList(), null)

        var timestamp: String? = null
        val dateMatch = Regex("""Date:\s*(\d{2}-\d{2}-\d{4})\s+Time:\s*(\d{2}:\d{2}:\d{2})""")
            .find(lines.getOrElse(0) { "" })
        if (dateMatch != null) {
            timestamp = "${dateMatch.groupValues[1]} ${dateMatch.groupValues[2]}"
        }

        val quotes = mutableListOf<Quote>()
        for (i in 4 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue
            val parts = line.split(Regex("\\s+"))
            if (parts.size >= 2) {
                val symbol = parts[0].uppercase()
                val ltp = parts[1].toDoubleOrNull()
                if (ltp != null) {
                    quotes.add(Quote(symbol, ltp))
                }
            }
        }
        return ParsedQuotes(quotes, timestamp)
    }

    fun parseMarketStatus(html: String): String? {
        val regex = Regex("""Market Status:\s*<[^>]*>\s*<b>\s*(Open|Closed)\s*</b>""", RegexOption.IGNORE_CASE)
        return regex.find(html)?.groupValues?.get(1)
    }

    fun parseFullHtml(html: String): Map<String, StockQuoteFull> {
        val doc = Jsoup.parse(html)
        val rows = doc.select("table tbody tr")
        val result = mutableMapOf<String, StockQuoteFull>()
        for (row in rows) {
            val cells = row.select("td")
            if (cells.size >= 9) {
                val symbol = cells[1].text().trim().uppercase()
                if (symbol.isNotEmpty()) {
                    result[symbol] = StockQuoteFull(
                        symbol = symbol,
                        ltp = cells[2].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                        high = cells[3].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                        low = cells[4].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                        closep = cells[5].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                        ycp = cells[6].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                        change = cells[7].text().replace(",", "").toDoubleOrNull() ?: 0.0,
                        pctChange = cells[8].text().replace(",", "").replace("%", "").toDoubleOrNull() ?: 0.0
                    )
                }
            }
        }
        return result
    }
}
