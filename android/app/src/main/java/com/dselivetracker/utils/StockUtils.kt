package com.dselivetracker.utils

object StockUtils {
    private val symbolToName = mapOf(
        "1JANATAMF" to "First Janata Bank Mutual Fund",
        "AAMRANET" to "aamra networks limited",
        "AAMRATECH" to "aamra technologies limited",
        "ABBANK" to "AB Bank Limited",
        "ACI" to "ACI Limited",
        "ACIFORMULA" to "ACI Formulations Limited",
        "ACMELAB" to "The ACME Laboratories Limited",
        "ADNTEL" to "ADN Telecom Limited",
        "ADVENT" to "Advent Pharma Limited",
        "AFCAGRO" to "AFC Agro Biotech Ltd.",
        "AFTABAUTO" to "Aftab Automobiles Limited",
        "AGNISYSL" to "Agni Systems Ltd.",
        "ALARABANK" to "Al-Arafah Islami Bank Ltd",
        "ALIF" to "Alif Manufacturing Company Ltd.",
        "AMANFEED" to "Aman Feed Limited",
        "AMBEEPHA" to "Ambee Pharmaceuticals PLC",
        "AMCL(PRAN)" to "Agricultural Marketing Company Ltd. (Pran)",
        "ANWARGALV" to "Anwar Galvanizing Ltd.",
        "APEXFOOT" to "Apex Footwear Limited",
        "APEXSPINN" to "Apex Spinning & Knitting Mills Limited",
        "BATASHOE" to "Bata Shoe Company (Bangladesh) Limited",
        "BATBC" to "British American Tobacco Bangladesh Company Limited",
        "BBS" to "Bangladesh Building Systems Ltd.",
        "BBSCABLES" to "BBS Cables Limited",
        "BERGERPBL" to "Berger Paints Bangladesh Limited",
        "BEXIMCO" to "Bangladesh Export Import Company Limited",
        "BRACBANK" to "BRAC Bank PLC",
        "BSCCL" to "Bangladesh Submarine Cables PLC",
        "BSRMLTD" to "Bangladesh Steel Re-Rolling Mills Limited",
        "BXPHARMA" to "Beximco Pharmaceuticals PLC",
        "CITYBANK" to "City Bank PLC",
        "CONFIDCEM" to "Confidence Cement Limited",
        "DUTCHBANGL" to "Dutch-Bangla Bank PLC",
        "EBL" to "Eastern Bank PLC",
        "FORTUNE" to "Fortune Shoes Limited",
        "GP" to "Grameenphone Ltd.",
        "IDLC" to "IDLC Finance PLC",
        "IFADAUTOS" to "IFAD Autos Limited",
        "ISLAMIBANK" to "Islami Bank Bangladesh PLC",
        "JAMUNABANK" to "Jamuna Bank PLC",
        "LHB" to "LafargeHolcim Bangladesh PLC",
        "LANKABANG" to "LankaBangla Finance PLC",
        "MARICO" to "Marico Bangladesh Limited",
        "MJLBD" to "MJL Bangladesh PLC",
        "MTB" to "Mutual Trust Bank PLC",
        "NATLIFEINS" to "National Life Insurance Co. Ltd.",
        "OLYMPIC" to "Olympic Industries PLC",
        "ORIONPHARM" to "Orion Pharma Ltd.",
        "POWERGRID" to "Power Grid Bangladesh PLC",
        "RECKITTBEN" to "Reckitt Benckiser (Bangladesh) PLC",
        "RENATA" to "Renata PLC",
        "ROBI" to "Robi Axiata PLC",
        "SQURPHARMA" to "Square Pharmaceuticals PLC",
        "SUMITPOWER" to "Summit Power Limited",
        "TITASGAS" to "Titas Gas Transmission & Distribution Co. Ltd.",
        "UPGDCL" to "United Power Generation & Distribution Company Ltd.",
        "UTTARABANK" to "Uttara Bank PLC",
        "WALTONHIL" to "Walton Hi-Tech Industries PLC"
    )

    fun getStockName(symbol: String): String {
        return symbolToName[symbol.uppercase()] ?: symbol
    }

    fun getAllMappings(): Map<String, String> = symbolToName

    fun getBreakerPctForPrice(price: Double): Double = when {
        price <= 200 -> 10.0
        price <= 500 -> 8.75
        price <= 1000 -> 7.5
        price <= 2000 -> 6.25
        price <= 5000 -> 5.0
        else -> 3.75
    }
}
