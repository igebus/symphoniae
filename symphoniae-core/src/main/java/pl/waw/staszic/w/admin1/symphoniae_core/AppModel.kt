package pl.waw.staszic.w.admin1.symphoniae_core

import android.content.Context
import com.google.gson.JsonParser
import pl.waw.staszic.w.admin1.symphoniae_core.chooser.Chooser
import pl.waw.staszic.w.admin1.symphoniae_core.models.ModelManager
import pl.waw.staszic.w.admin1.symphoniae_core.models.ProgressListener
import pl.waw.staszic.w.admin1.symphoniae_core.morse_code.MorseHelper
import pl.waw.staszic.w.admin1.symphoniae_core.recognition.Recognizer
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

class AppModel<T : Context>(private val context : T, val languageChooser : Chooser) :
    ProgressListener where T : AppModelListener {
    private val loopThread = Thread(::loop, "Program loop")
    private val recognizer = Recognizer()
    private val morseHelper by lazy { MorseHelper(context) }
    private val modelManager by lazy { ModelManager( context) }
    val results = ConcurrentLinkedQueue<String>()
    val pastResults = LinkedList<String>()
    var translatedLength = 0
    private val languagesSize : Int by lazy { context.resources.getStringArray(R.array.languages).size }
    private var running = true

    fun start() {
        loopThread.start()
    }

    private fun loop() {
        while(running) {
            try {
                if(recognizer.isReady() && !results.isEmpty()) {
                    if(results.peek().isEmpty()) {
                        results.poll()
                    }
                    else {
                        morseHelper.translate(results.peek()) {
                            translatedLength++
                            if (translatedLength == results.peek().length) {
                                translatedLength = 0
                                pastResults.add(results.poll())
                            }
                            context.updateRecognizedText()
                        }
                    }
                }
                else {
                    Thread.sleep(50)
                }
            }
            catch(e : InterruptedException) {
                results.clear()
                pastResults.clear()
                translatedLength = 0
                context.updateRecognizedText()
            }
        }
    }

    fun destroy() {
        recognizer.stopRecognition()
        running = false
    }

    private fun setupRecognition(modelFile : String) {
        context.changeButtonState(RecordingState.LOADING)
        recognizer.stopRecognition()
        loopThread.interrupt()
        recognizer.setup(modelFile) { err ->
            if(err != null) {
                err.printStackTrace()
                context.exit()
            }
            else {
                context.changeButtonState(RecordingState.IDLE)
            }
        }
    }

    fun startRecognition() {
        context.changeButtonState(RecordingState.RECORDING)
        recognizer.startRecognition {
            val json = JsonParser.parseString(it).getAsJsonObject()
            if(json.has("text")) {
                results.add(json.get("text").getAsString())
                context.updateRecognizedText()
            }
        }
    }

    fun stopRecognition() {
        context.changeButtonState(RecordingState.IDLE)
        recognizer.stopRecognition()
    }

    fun changeLanguage() {
        val index = languageChooser.getSelected()
        if(modelManager.hasModel(index)) {
            this.onComplete()
        }
        else {
            context.confirmDownload(index)
        }
    }

    fun downloadModel(index : Int) {
        recognizer.stopRecognition()
        context.changeButtonState(RecordingState.LOADING)
        modelManager.downloadModel(index, this)
    }

    override fun initProgress(totalSize: Long) {}

    override fun updateProgress(progress: Long) {}

    override fun onDownloadStart() {}

    override fun onUnzipStart() {}

    override fun onComplete() {
        if(languageChooser.getPrevious() == languagesSize-1) {
            languageChooser.removeOption(languagesSize-1)
        }
        setupRecognition(modelManager.getModel(languageChooser.getSelected()).canonicalPath)
    }

    override fun onException(exception: Exception) {
        languageChooser.selectPrevious()
        context.changeButtonState(
            if(languageChooser.getPrevious() == languagesSize-1) RecordingState.DISABLED
            else RecordingState.IDLE
        )
        context.downloadException(exception)
    }

    enum class RecordingState {
        RECORDING, IDLE, LOADING, DISABLED
    }
}