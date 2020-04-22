data class FlavorConfig(
    val appId: String,
    val appName: String,
    val appHost: String,
    val languages: List<String> = listOf("en", "de")
)

val FLAVORS = mapOf(
    "openhpi" to FlavorConfig(
        "de.xikolo.openhpi",
        "openHPI",
        "open.hpi.de"
    ),
    "opensap" to FlavorConfig(
        "de.xikolo.opensap",
        "openSAP",
        "open.sap.com"
    ),
    "moochouse" to FlavorConfig(
        "de.xikolo.moochouse",
        "mooc.house",
        "mooc.house"
    ),
    "openwho" to FlavorConfig(
        "de.xikolo.openwho",
        "OpenWHO",
        "openwho.org",
        listOf("en")
    ),
    "lernencloud" to FlavorConfig(
        "de.xikolo.lernencloud",
        "Lernen.cloud",
        "lernen.cloud"
    )
)
