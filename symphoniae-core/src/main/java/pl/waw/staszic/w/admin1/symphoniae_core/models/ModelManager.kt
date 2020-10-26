package pl.waw.staszic.w.admin1.symphoniae_core.models

import android.content.Context
import pl.waw.staszic.w.admin1.symphoniae_core.R
import java.io.File
import java.net.URL
import java.nio.file.Paths

class ModelManager(context : Context) {
    private val downloadURLs = context.resources.getStringArray(R.array.downloadURLs).let { array ->
        array.take(array.size-1).map { URL(it) }
    }
    private val modelNames = downloadURLs.map { url ->
        Paths.get(url.path).fileName.toString().let { str ->
            str.substring(0 until (str.length-4))
        }
    }
    private val modelsDirectory = context.filesDir.resolve("recognizer_models")

    fun getModel(index : Int) : File {
        return modelsDirectory.resolve(modelNames[index])
    }

    fun hasModel(index : Int) : Boolean {
        return getModel(index).exists()
    }

    fun downloadModel(index : Int, listener: ProgressListener) {
        Thread({
            download(
                downloadURLs[index],
                modelsDirectory.toPath(),
                listener
            )
        }, "Download").start()
    }
}