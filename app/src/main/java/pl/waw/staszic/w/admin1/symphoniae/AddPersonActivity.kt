package pl.waw.staszic.w.admin1.symphoniae

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AddPersonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_person)
        findViewById<ImageButton>(R.id.addPersonButtonBack).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                finish()
            }
        })

    }
}