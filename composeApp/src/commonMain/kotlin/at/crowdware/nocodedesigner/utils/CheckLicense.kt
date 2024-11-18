package at.crowdware.nocodedesigner.utils

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class LicenseManager {
    suspend fun checkLicense(licenseKey: String): LicenseStatus {
        val client = HttpClient()
        return client.get("YOUR_SERVER_URL/check_license") {
            parameter("licenseKey", licenseKey)
        }.body()
    }
}

data class LicenseStatus(
    val isValid: Boolean = false,
    val licenseType: String = "",
    val daysRemaining: Int = 0
)

// Beispiel zum Verwenden:

suspend fun test() {
    val manager = LicenseManager()
    val licenseKey = "dein-lizenz-schluessel"
    val status = manager.checkLicense(licenseKey)
    println("License Status: ${status.isValid}, Type: ${status.licenseType}, Days Left: ${status.daysRemaining}")
}
