package pl.waw.staszic.w.admin1.symphoniae

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*
import kotlin.math.*

class SoundGraph(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var graphResolution = 1.0f
    private var length = 0
    private var data = DoubleArray(0)
    private var offset = 0
    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SoundGraph, 0, 0).apply {
                try {
                    graphResolution = max(0.1f, abs(getFloat(R.styleable.SoundGraph_graphResolution, 1.0f)))
                    length = max(1, getInteger(R.styleable.SoundGraph_dataLength, 0))
                    data = DoubleArray(length)
                    offset = length
                    linePaint.color = getColor(R.styleable.SoundGraph_lineColor, 0xff0080ff.toInt())
                    linePaint.strokeWidth = max(0.1f, abs(getFloat(R.styleable.SoundGraph_lineWidth, 1.0f)))
                } finally {
                    recycle()
                }
            }
    }

    override fun onDraw(canvas: Canvas) {
        var drawn = DoubleArray(floor(canvas.width/graphResolution).toInt())
        var rotatedData = ArrayList<Double>()
        for(i in offset until data.size) {
            rotatedData.add(data[i])
        }
        for(i in 0 until offset) {
            rotatedData.add(data[i])
        }
        for(i in rotatedData.indices) {
            val s = i*drawn.size/data.size.toDouble()
            val e = (i+1)*drawn.size/data.size.toDouble()
            for(j in max(floor(s).toInt(), 0) until min(ceil(e).toInt(), drawn.size)) {
                drawn[j] += (min((j+1).toDouble(), e) - max(j.toDouble(), s))*rotatedData[i]
            }
        }
        for(i in drawn.indices) {
            drawn[i] *= (canvas.height/2.0/0x7fff)
        }
        for(i in 1 until drawn.size) {
            canvas.drawLine(
                ((i-1)*graphResolution).toFloat(), (drawn[i-1]+canvas.height/2.0).toFloat(),
                (i*graphResolution).toFloat(),     (drawn[i]+canvas.height/2.0).toFloat(),
                linePaint
            )
        }
    }

    fun append(value : Double) {
        data[--offset] = value
        if(offset <= 0) offset = data.size
    }

    fun clear() {
        for(i in data.indices) {
            data[i] = 0.0
        }
        offset = data.size
    }
}