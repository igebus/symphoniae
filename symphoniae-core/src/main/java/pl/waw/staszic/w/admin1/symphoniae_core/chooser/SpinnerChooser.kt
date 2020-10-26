package pl.waw.staszic.w.admin1.symphoniae_core.chooser

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import pl.waw.staszic.w.admin1.symphoniae_core.R
import java.util.concurrent.atomic.AtomicInteger

class SpinnerChooser(private val spinner : Spinner, private val layoutResource : Int, startingElement : Int, private val runOnUiThread : (() -> Unit) -> Unit, private val selectionCallback : () -> Unit) : Chooser, AdapterView.OnItemSelectedListener {
    private val internalSelectCounter = AtomicInteger(0)
    private var previousChoice = startingElement
    private var activeChoice = startingElement
    init {
        runOnUiThread {
            spinner.onItemSelectedListener = this
            val arraySize = spinner.context.resources.getStringArray(R.array.languages).size
            selectItem((startingElement % arraySize + arraySize) % arraySize)
        }
    }

    override fun selectItem(index: Int) {
        runOnUiThread {
            internalSelectCounter.incrementAndGet()
            spinner.setSelection(index)
        }
    }

    override fun getSelected() : Int {
        return activeChoice
    }

    override fun getPrevious() : Int {
        return previousChoice
    }

    override fun selectPrevious() {
        selectItem(previousChoice)
    }

    override fun removeOption(index: Int) {
        runOnUiThread {
            val elements = Array(spinner.adapter.count - 1) {
                spinner.adapter.getItem(
                    if (it >= index) it + 1
                    else it
                ) as String
            }
            spinner.adapter = ArrayAdapter<String>(
                spinner.context,
                layoutResource,
                elements
            )
            if (index <= activeChoice) {
                selectItem(activeChoice - 1)
            } else {
                selectItem(activeChoice)
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if(previousChoice == position) return
        previousChoice = activeChoice
        activeChoice = position
        if(internalSelectCounter.getAndDecrement() == 0) {
            internalSelectCounter.incrementAndGet()
            selectionCallback()
        }
    }
}