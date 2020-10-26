package pl.waw.staszic.w.admin1.symphoniae_core.recognition

import org.kaldi.*
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

class Recognizer(private val sampleRate : Float = 16000.0f) {
    private val recorder = Recorder(sampleRate = sampleRate.toInt())
    private var model : Model? = null
    private var ready = AtomicBoolean(false)
    private val lock = ReentrantLock()

    fun setup(modelFile : String, resultCallback : (e : Exception?) -> Unit = {}) {
        Thread({
            lock.lock()
            try {
                stopRecognition()
                ready.set(false)
                Vosk.SetLogLevel(0)
                model = Model(modelFile)
                ready.set(true)
                resultCallback(null)
            } catch (e: Exception) {
                resultCallback(e)
            } finally {
                lock.unlock()
            }
        }, "Recognizer setup").start()
    }

    fun stopRecognition() {
        recorder.stopRecording()
    }

    fun startRecognition(resultsConsumer : (result : String) -> Unit) : Boolean {
        if(!ready.get()) return false
        lock.lock()
        try {
            val recognizer = KaldiRecognizer(model, sampleRate)
            recorder.startRecording { data, length ->
                if (recognizer.AcceptWaveform(data, length)) {
                    resultsConsumer(recognizer.Result())
                } else {
                    resultsConsumer(recognizer.PartialResult())
                }
            }
            return true
        } catch (e : IOException) {}
        finally {
            lock.unlock()
        }
        return false
    }

    fun isReady() : Boolean {
        return ready.get()
    }
}
