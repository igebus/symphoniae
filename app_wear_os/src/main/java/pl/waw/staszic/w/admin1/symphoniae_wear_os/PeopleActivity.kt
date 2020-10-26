package pl.waw.staszic.w.admin1.symphoniae_wear_os

/*
import android.os.Bundle
import android.support.wearable.activity.WearableActivity

class PeopleActivity : WearableActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)

        // Enables Always-on
        setAmbientEnabled()
    }
}
*/

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PeopleActivity : WearableActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)

        findViewById<ImageButton>(R.id.peopleBackButton).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                finish()
            }
        })
        findViewById<ImageButton>(R.id.addPersonButtonNav).setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                startActivity(Intent(this@PeopleActivity, AddPersonActivity::class.java))
            }
        })

        // Sample Data
        findViewById<ListView>(R.id.peopleVoicesList).adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
            arrayOf<String>(
                "Abundance",
                "Anxiety",
                "Bruxism",
                "Discipline",
                "Drug Addiction",
                "Abundance",
                "Anxiety",
                "Bruxism",
                "Discipline",
                "Drug Addiction",
                "Abundance",
                "Anxiety",
                "Bruxism",
                "Discipline",
                "Drug Addiction",
                "Abundance",
                "Anxiety",
                "Bruxism",
                "Discipline",
                "Drug Addiction",
                "Abundance",
                "Anxiety",
                "Bruxism",
                "Discipline",
                "Drug Addiction",
                "Abundance",
                "Anxiety",
                "Bruxism",
                "Discipline",
                "Drug Addiction",
                "Abundance",
                "Anxiety",
                "Bruxism",
                "Discipline",
                "Drug Addiction"
            )
        )
    }


}