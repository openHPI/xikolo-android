package de.xikolo.utils

import java.math.BigInteger

object IdUtil {

    fun isUUID(id: String): Boolean {
        return id.matches("([a-z,0-9]){8}(-([a-z,0-9]){4}){3}-([a-z,0-9]){12}".toRegex())
    }

    fun base62ToUUID(base62Id: String): String? {
        return try {
            var number = BigInteger.valueOf(0)
            for (i in 0 until base62Id.length) {
                number = number
                    .multiply(BigInteger.valueOf(62))
                    .plus(BigInteger.valueOf(
                        "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(base62Id[i]).toLong()
                    ))
            }
            val uuid = number.toString(16)
            "${uuid.substring(0, 8)}-${uuid.substring(8, 12)}-${uuid.substring(12, 16)}-${uuid.substring(16, 20)}-${uuid.substring(20, 32)}"
        } catch (e: Exception) {
            null
        }
    }
}
