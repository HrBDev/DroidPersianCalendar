package com.byagowi.persiancalendar.ui.calendar.calendarpager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.utils.Jdn
import com.byagowi.persiancalendar.utils.appTheme
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isHighTextContrastEnabled
import com.byagowi.persiancalendar.utils.isNonArabicScriptSelected
import com.byagowi.persiancalendar.utils.resolveColor
import kotlin.math.min

class DayView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    private val colorHoliday = context.resolveColor(R.attr.colorHoliday)
    private val colorHolidaySelected = context.resolveColor(R.attr.colorHolidaySelected)

    // private val colorTextHoliday = context.resolveColor(R.attr.colorTextHoliday)
    private val colorTextDay = context.resolveColor(R.attr.colorTextDay)
    private val colorTextDaySelected = context.resolveColor(R.attr.colorTextDaySelected)

    // private val colorTextToday = context.resolveColor(R.attr.colorTextToday)
    private val colorTextDayName = context.resolveColor(R.attr.colorTextDayName)
    private val colorEventLine = context.resolveColor(R.attr.colorEventLine)

    private val halfEventBarWidth = context.resources
        .getDimensionPixelSize(R.dimen.day_item_event_bar_width) / 2
    private val appointmentYOffset = context.resources
        .getDimensionPixelSize(R.dimen.day_item_appointment_y_offset)
    private val eventYOffset = context.resources
        .getDimensionPixelSize(R.dimen.day_item_event_y_offset)
    private val eventBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = context.resources
            .getDimensionPixelSize(R.dimen.day_item_event_bar_thickness).toFloat()
    }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.resolveColor(R.attr.colorSelectDay)
    }
    private val todayPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = context.resources
            .getDimensionPixelSize(R.dimen.day_item_today_indicator_thickness).toFloat()
        color = context.resolveColor(R.attr.colorCurrentDay)
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setTextTypeface(typeface: Typeface) {
        textPaint.typeface = typeface
    }

    private val bounds = Rect()
    private val drawingRect = RectF()
    private var text = ""
    private var today: Boolean = false
    private var dayIsSelected: Boolean = false
    private var hasEvent: Boolean = false
    private var hasAppointment: Boolean = false
    private var holiday: Boolean = false
    private var textSize: Int = 0
    var jdn: Jdn? = null
        private set
    var dayOfMonth = -1
        private set
    private var isNumber: Boolean = false
    private var header = ""

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height
        val radius = min(width, height) / 2

        val isModernTheme = appTheme == R.style.ModernTheme
        getDrawingRect(bounds)
        drawingRect.set(bounds)
        drawingRect.inset(radius * 0.1f, radius * 0.1f)
        val yOffsetToApply = if (isModernTheme) (-height * .07f).toInt() else 0

        if (dayIsSelected) {
            if (isModernTheme) {
                canvas.drawRoundRect(drawingRect, 0f, 0f, selectedPaint)
            } else {
                canvas.drawCircle(
                    width / 2f, height / 2f, (radius - 5).toFloat(),
                    selectedPaint
                )
            }
        }

        if (today) {
            if (isModernTheme) {
                canvas.drawRoundRect(drawingRect, 0f, 0f, todayPaint)
            } else {
                canvas.drawCircle(
                    width / 2f, height / 2f, (radius - 5).toFloat(),
                    todayPaint
                )
            }
        }

        val color: Int = if (isNumber) {
            if (holiday)
                if (dayIsSelected) colorHolidaySelected else colorHoliday
            else
                if (dayIsSelected) colorTextDaySelected else colorTextDay
            //            if (today && !selected) {
            //                color = resource.colorTextToday;
            //            }
        } else {
            colorTextDayName
        }

        eventBarPaint.color = if (dayIsSelected && !isModernTheme) color else colorEventLine

        // a11y improvement
        if (isHighTextContrastEnabled && holiday)
            eventBarPaint.color = color

        if (hasEvent) {
            canvas.drawLine(
                width / 2f - halfEventBarWidth,
                (height - eventYOffset + yOffsetToApply).toFloat(),
                width / 2f + halfEventBarWidth,
                (height - eventYOffset + yOffsetToApply).toFloat(), eventBarPaint
            )
        }

        if (hasAppointment) {
            canvas.drawLine(
                width / 2f - halfEventBarWidth,
                (height - appointmentYOffset + yOffsetToApply).toFloat(),
                width / 2f + halfEventBarWidth,
                (height - appointmentYOffset + yOffsetToApply).toFloat(),
                eventBarPaint
            )
        }

        // TODO: Better to not change resource's paint objects, but for now
        textPaint.color = color
        textPaint.textSize = textSize.toFloat()

        if (isModernTheme) {
            textPaint.isFakeBoldText = today
            textPaint.textSize = textSize * .8f
        }

        val xPos = (width - textPaint.measureText(text).toInt()) / 2
        val textToMeasureHeight =
            if (isNumber) text else if (isNonArabicScriptSelected()) "Y" else "شچ"
        textPaint.getTextBounds(textToMeasureHeight, 0, textToMeasureHeight.length, bounds)
        var yPos = (height + bounds.height()) / 2
        yPos += yOffsetToApply
        canvas.drawText(text, xPos.toFloat(), yPos.toFloat(), textPaint)

        textPaint.color = if (dayIsSelected) colorTextDaySelected else colorTextDay
        textPaint.textSize = textSize / 2f
        if (header.isNotEmpty()) {
            val headerXPos = (width - textPaint.measureText(header).toInt()) / 2F
            canvas.drawText(header, headerXPos, yPos * 0.87f - bounds.height(), textPaint)
        }
    }

    private fun setAll(
        text: String, isToday: Boolean, isSelected: Boolean,
        hasEvent: Boolean, hasAppointment: Boolean, isHoliday: Boolean,
        textSize: Int, jdn: Jdn?, dayOfMonth: Int, isNumber: Boolean,
        header: String
    ) {
        this.text = text
        this.today = isToday
        this.dayIsSelected = isSelected
        this.hasEvent = hasEvent
        this.hasAppointment = hasAppointment
        this.holiday = isHoliday
        this.textSize = textSize
        this.jdn = jdn
        this.dayOfMonth = dayOfMonth
        this.isNumber = isNumber
        this.header = header
        postInvalidate()
    }

    fun setDayOfMonthItem(
        isToday: Boolean, isSelected: Boolean,
        hasEvent: Boolean, hasAppointment: Boolean, isHoliday: Boolean,
        textSize: Int, jdn: Jdn, dayOfMonth: Int, header: String
    ) = setAll(
        formatNumber(dayOfMonth), isToday, isSelected, hasEvent, hasAppointment,
        isHoliday, textSize, jdn, dayOfMonth, true, header
    )

    fun setNonDayOfMonthItem(text: String, textSize: Int) = setAll(
        text, isToday = false, isSelected = false, hasEvent = false, hasAppointment = false,
        isHoliday = false, textSize = textSize, jdn = null, dayOfMonth = -1,
        isNumber = false, header = ""
    )
}
