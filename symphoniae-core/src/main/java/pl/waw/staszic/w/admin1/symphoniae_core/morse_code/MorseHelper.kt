package pl.waw.staszic.w.admin1.symphoniae_core.morse_code

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.VibrationEffect
import android.os.Vibrator
import pl.waw.staszic.w.admin1.symphoniae_core.R

class MorseHelper(context : Context, private val dotLength : Long = 100) {
    private val dashLength : Long = 3*dotLength
    private val emptyLength : Long = dotLength

    private val vibrator = context.getSystemService(VIBRATOR_SERVICE) as Vibrator
    private val codes =
        context.resources.getStringArray(R.array.morse_codes).map { str ->
            return@map str[0] to str.substring(1).flatMap { char ->
                return@flatMap when(char) {
                    '-' -> listOf(0, dashLength)
                    '.' -> listOf(0, dotLength)
                    '*', ' ' -> listOf(emptyLength, 0)
                    else -> listOf()
                }
            }.toLongArray()
        }.toMap()
    private val codeLengths = codes.mapValues {
        it.value.sum()
    }

    fun translate(text : CharSequence, charTranslatedCallback : () -> Unit) {
        try {
            for (it in text) {
                val ch: Char = Character.toLowerCase(it)
                if (codes.contains(ch)) {
                    vibrator.vibrate(VibrationEffect.createWaveform(codes[ch] ?: longArrayOf(), -1))
                    Thread.sleep(codeLengths[ch] ?: 0)
                }
                charTranslatedCallback()
            }
        } catch(e : InterruptedException) {
            vibrator.cancel()
            throw e
        }
    }
}