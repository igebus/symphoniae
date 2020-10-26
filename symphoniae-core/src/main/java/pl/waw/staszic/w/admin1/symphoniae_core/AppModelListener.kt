package pl.waw.staszic.w.admin1.symphoniae_core

import java.lang.Exception

interface AppModelListener {
    fun changeButtonState(state : AppModel.RecordingState)
    fun updateRecognizedText()
    fun exit()
    fun confirmDownload(index : Int)
    fun downloadException(exception: Exception)
}