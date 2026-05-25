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
        val pctChange: Double
    )

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
