package com.nova.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Typeface
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

abstract class NovaWidget : AppWidgetProvider() {
    abstract fun heading(): String
    abstract fun body(context: Context): String

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_card)
            views.setTextViewText(R.id.widget_heading, heading())
            val prefs = context.getSharedPreferences("nova", Context.MODE_PRIVATE)
            val fontPath = prefs.getString("font_path", null)
            val typeface = try {
                if (fontPath != null && File(fontPath).exists()) Typeface.createFromFile(fontPath)
                else Typeface.create("sans-serif", Typeface.BOLD)
            } catch (_: Exception) { Typeface.create("sans-serif", Typeface.BOLD) }
            val bitmap = renderText(body(context), typeface)
            views.setImageViewBitmap(R.id.widget_body_image, bitmap)
            manager.updateAppWidget(id, views)
        }
    }

    private fun renderText(value: String, typeface: Typeface): Bitmap {
        val width = 620
        val height = 150
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(244, 246, 252)
            textSize = 48f
            this.typeface = typeface
        }
        val maxWidth = width - 12f
        var line = ""
        var y = 57f
        value.split(" ").forEach { word ->
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > maxWidth && line.isNotEmpty()) {
                canvas.drawText(line, 2f, y, paint)
                line = word
                y += 55f
            } else line = candidate
        }
        if (line.isNotEmpty() && y < height) canvas.drawText(line, 2f, y, paint)
        return bitmap
    }
}

class ClockWidget : NovaWidget() {
    override fun heading() = "N O V A   •   T I M E"
    override fun body(context: Context) = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())
}
class DateWidget : NovaWidget() {
    override fun heading() = "TODAY"
    override fun body(context: Context) = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
}
class QuoteWidget : NovaWidget() {
    override fun heading() = "A THOUGHT TO KEEP"
    override fun body(context: Context) = "Small steps every day lead to beautiful places."
}
class WeatherWidget : NovaWidget() {
    override fun heading() = "WEATHER"
    override fun body(context: Context) = "Live weather setup is coming soon."
}
class BatteryWidget : NovaWidget() {
    override fun heading() = "BATTERY"
    override fun body(context: Context): String {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        return if (level >= 0) "$level% battery" else "Battery unavailable"
    }
}
class PrayerWidget : NovaWidget() {
    override fun heading() = "PRAYER TIMES"
    override fun body(context: Context) = "Prayer schedule setup is coming soon."
}
