package com.konovus.apitesting.util

import android.graphics.Canvas
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.view.isVisible
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF


class MpMarker(
    private val chart: LineChart,
    private val mpMarkerTv: TextView,
    private val scrollView: ScrollView
    ): IMarker {

    private var mOffset: MPPointF? = null

    override fun getOffset(): MPPointF {

        return mOffset ?: MPPointF((-(chart.width / 2)).toFloat(), (-chart.height).toFloat())
    }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        return chart.centerOffsets
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null)
        mpMarkerTv.text = e.y.toDouble().toNDecimals(2).toString()
        mpMarkerTv.isVisible = true
    }

    override fun draw(canvas: Canvas?, posX: Float, posY: Float) {
        val pos = IntArray(2)
        chart.getLocationOnScreen(pos)

        mpMarkerTv.translationX = posX - 50
        mpMarkerTv.translationY = (pos[1] - 100 + scrollView.scrollY).toFloat()
    }
}

