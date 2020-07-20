data class FlavorConfig(
    val appId: String,
    val appName: String,
    val appHost: String,
    val primaryColor: String,
    val secondaryColor: String,
    val languages: List<String> = listOf("en", "de")
)

val FLAVORS = mapOf(
    "openhpi" to FlavorConfig(
        "de.xikolo.openhpi",
        "openHPI",
        "open.hpi.de",
        "#de6212",
        "#b42946",
        listOf("en", "de")
    ),
    "opensap" to FlavorConfig(
        "de.xikolo.opensap",
        "openSAP",
        "open.sap.com",
        "#f0ab00",
        "#226ca9",
        listOf("en", "de")
    ),
    "moochouse" to FlavorConfig(
        "de.xikolo.moochouse",
        "mooc.house",
        "mooc.house",
        "#abb324",
        "#abb324",
        listOf("en", "de")
    ),
    "openwho" to FlavorConfig(
        "de.xikolo.openwho",
        "OpenWHO",
        "openwho.org",
        "#0b72b5",
        "#0b72b5",
        listOf("en", "fr", "pt")
    ),
    "lernencloud" to FlavorConfig(
        "de.xikolo.lernencloud",
        "Lernen.cloud",
        "lernen.cloud",
        "#dd6108",
        "#f6a800",
        listOf("en", "de")
    )
)
