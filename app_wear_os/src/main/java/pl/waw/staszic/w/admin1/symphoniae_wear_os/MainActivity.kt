package pl.waw.staszic.w.admin1.symphoniae_wear_os

/*
import android.os.Bundle
import android.support.wearable.activity.WearableActivity

class MainActivity : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enables Always-on
        setAmbientEnabled()
    }
}
*/

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.*
import android.support.wearable.activity.WearableActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import pl.waw.staszic.w.admin1.symphoniae_core.*
import pl.waw.staszic.w.admin1.symphoniae_core.chooser.SpinnerChooser

class MainActivity : WearableActivity(), AppModelListener {
    private val appModel by lazy {
        AppModel(this,
            SpinnerChooser(
                language,
                R.layout.support_simple_spinner_dropdown_item,
                -1,
                { task: () -> Unit -> runOnUiThread(task) },
                ::changeLanguage
            )
        )
    }
    private var recordingState : AppModel.RecordingState = AppModel.RecordingState.LOADING

    private val micButton: ImageButton by lazy { findViewById<ImageButton>(R.id.mic_button) }
    private val progressBar: ProgressBar by lazy { findViewById<ProgressBar>(R.id.progress_bar) }
    private val recognizedText: TextView by lazy { findViewById<TextView>(R.id.recognized_text) }
    private val language: Spinner by lazy { findViewById<Spinner>(R.id.language) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            //TODO Czy wartość requestCode ma znaczenie?
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 123)
        }

        findViewById<ImageButton>(R.id.personbtn).setOnClickListener {
            startActivity(Intent(this@MainActivity, PeopleActivity::class.java))
        }
        findViewById<ImageButton>(R.id.vibrations_button).setOnClickListener {
            //TODO wyłączanie i włączanie wibracji (jak powinno się zachować?)
        }
        micButton.setOnClickListener {
            when(recordingState) {
                AppModel.RecordingState.IDLE -> appModel.startRecognition()
                AppModel.RecordingState.RECORDING -> appModel.stopRecognition()
                AppModel.RecordingState.LOADING -> alertLoading()
                AppModel.RecordingState.DISABLED -> alertDisabled()
            }
        }

        changeButtonState(AppModel.RecordingState.DISABLED)
        appModel.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        appModel.destroy()
    }

    override fun exit() {
        finishAffinity()
    }

    private fun changeLanguage() {
        appModel.changeLanguage()
    }

    fun alertLoading(view : View? = null) {
        Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show()
    }

    private fun alertDisabled(view : View? = null) {
        Toast.makeText(this, R.string.disabled, Toast.LENGTH_SHORT).show()
    }

    override fun confirmDownload(index : Int) {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setPositiveButton(R.string.confirm_download) { _, _ ->
                    appModel.downloadModel(index)
                }.setNegativeButton(R.string.reject_download) { _, _ ->
                    appModel.languageChooser.selectPrevious()
                }.setTitle(R.string.title_model_download)
                .setMessage(
                    String.format(
                        resources.getString(R.string.message_model_download),
                        resources.getStringArray(R.array.languages)[index]
                    )
                )
                .setCancelable(false)
                .show()
        }
    }

    override fun downloadException(exception: java.lang.Exception) {
        exception.printStackTrace()
        runOnUiThread {
            AlertDialog.Builder(this)
                .setNeutralButton(R.string.error_ok) { _, _ -> }
                .setTitle(R.string.title_download_error)
                .setMessage(R.string.message_download_error)
                .show()
        }
    }

    override fun updateRecognizedText() {
        runOnUiThread {
            val builder = SpannableStringBuilder()
            var translatedSpan = 0
            while(appModel.pastResults.size > 2) {
                appModel.pastResults.pop();
            }
            appModel.pastResults.forEach {
                builder.append("$it\n")
                translatedSpan += it.length + 1
            }
            appModel.results.forEach {
                builder.append("$it\n")
            }
            builder.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.translatedText)),
                0, translatedSpan + appModel.translatedLength,
                0
            )
            recognizedText.setText(builder, TextView.BufferType.SPANNABLE)
        }
    }

    override fun changeButtonState(state : AppModel.RecordingState) {
        runOnUiThread {
            when (state) {
                AppModel.RecordingState.IDLE -> {
                    changeButtonBackground(micButton, 0)
                    progressBar.visibility = View.GONE
                    micButton.visibility = View.VISIBLE
                    language.isEnabled = true
                }
                AppModel.RecordingState.RECORDING -> {
                    changeButtonBackground(micButton, 0xffff0000.toInt())
                    progressBar.visibility = View.GONE
                    micButton.visibility = View.VISIBLE
                    language.isEnabled = true
                }
                AppModel.RecordingState.LOADING -> {
                    micButton.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                    language.isEnabled = false
                }
                AppModel.RecordingState.DISABLED -> {
                    changeButtonBackground(micButton, 0xff808080.toInt())
                    progressBar.visibility = View.GONE
                    micButton.visibility = View.VISIBLE
                    language.isEnabled = true
                }
            }
        }
        recordingState = state
    }

    //TODO Chwilowe rozwiązanie, żeby było widać kiedy mikrofon jest włączony
    private fun changeButtonBackground(button : ImageButton, color : Int) {
        val drawable : Drawable = DrawableCompat.wrap(button.background)
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_OVER)
        DrawableCompat.setTint(drawable, color)
    }
}
