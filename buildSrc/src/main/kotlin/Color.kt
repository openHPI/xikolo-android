/**
 * Code snippets extracted from:
 * android.graphics.Color
 * androidx.core.graphics.ColorUtils
 */
object Color {
    private const val BLACK: Int = 0x000000
    private const val WHITE: Int = 0xFFFFFF

    fun darken(color: String): String {
        return toString(
            blendARGB(
                toInt(color),
                BLACK,
                0.4f
            )
        )
    }

    fun lighten(color: String): String {
        return toString(
            blendARGB(
                toInt(color),
                WHITE,
                0.3f
            )
        )
    }

    private fun blendARGB(
        color1: Int,
        color2: Int,
        ratio: Float
    ): Int {
        val inverseRatio = 1 - ratio
        val a: Float = alpha(color1) * inverseRatio + alpha(color2) * ratio
        val r: Float = red(color1) * inverseRatio + red(color2) * ratio
        val g: Float = green(color1) * inverseRatio + green(color2) * ratio
        val b: Float = blue(color1) * inverseRatio + blue(color2) * ratio
        return argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
    }

    private fun alpha(color: Int): Int {
        return color ushr 24
    }

    private fun red(color: Int): Int {
        return color shr 16 and 0xFF
    }

    private fun green(color: Int): Int {
        return color shr 8 and 0xFF
    }

    private fun blue(color: Int): Int {
        return color and 0xFF
    }

    private fun argb(
        alpha: Int,
        red: Int,
        green: Int,
        blue: Int
    ): Int {
        return alpha shl 24 or (red shl 16) or (green shl 8) or blue
    }

    private fun toInt(color: String): Int {
        return Integer.decode("0x${color.substring(1)}")
    }

    private fun toString(color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }
}
