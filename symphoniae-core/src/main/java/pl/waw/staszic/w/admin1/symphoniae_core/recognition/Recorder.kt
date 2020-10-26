package pl.waw.staszic.w.admin1.symphoniae_core.recognition

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.util.concurrent.locks.ReentrantLock

class Recorder(private val sampleRate: Int = 8000, private val pollInterval : Int = 25) {
    private var recorder : AudioRecord? = null
    private val recorderLock = ReentrantLock()

    fun isRecording() : Boolean {
        recorderLock.lock()
        try {
            return recorder != null
        } finally {
            recorderLock.unlock()
        }
    }

    fun startRecording(consumer : (data : ShortArray, length : Int) -> Unit) {
        recorderLock.lock()
        try {
            if (recorder != null) return
            recorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                sampleRate * 2
            )
            recorder?.startRecording()
            Thread({
                handleMicInput(consumer)
            }, "Recording").start()
        } finally {
            recorderLock.unlock()
        }
    }

    fun stopRecording() {
        recorderLock.lock()
        try {
            recorder?.stop()
            recorder?.release()
            recorder = null
        } finally {
            recorderLock.unlock()
        }
    }

    private fun handleMicInput(consumer : (data : ShortArray, length : Int) -> Unit) {
        var shouldContinue = true
        var t = 0L
        var buffer : ShortArray = ShortArray(sampleRate)
        var readSize : Int? = null
        while(true) {
            t = System.currentTimeMillis()
            recorderLock.lock()
            try {
                if(recorder == null) {
                    shouldContinue = false
                }
                else {
                    readSize = recorder?.read(buffer, 0, buffer.size, AudioRecord.READ_NON_BLOCKING)
                }
            } finally {
                recorderLock.unlock()
            }
            if(!shouldContinue) break
            consumer(buffer, readSize ?: 0)
            t = System.currentTimeMillis()-t
            if(t >= pollInterval) continue
            Thread.sleep(pollInterval-t)
        }
    }
}