package com.dselivetracker.data.remote

object QuotesParser {
    data class Quote(val symbol: String, val ltp: Double)

    data class ParsedQuotes(
        val quotes: List<Quote>,
        val timestamp: String?
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
}
