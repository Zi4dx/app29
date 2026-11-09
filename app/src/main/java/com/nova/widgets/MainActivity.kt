package com.nova.widgets

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.*
import java.io.File
import java.io.FileOutputStream

class MainActivity : Activity() {
    private val requestFont = 401
    private lateinit var container: LinearLayout
    private val bg = Color.rgb(12, 16, 28)
    private val panel = Color.rgb(25, 32, 49)
    private val white = Color.rgb(244, 246, 252)
    private val muted = Color.rgb(157, 170, 192)
    private val gold = Color.rgb(198, 177, 125)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(26, 34, 26, 28)
            setBackgroundColor(bg)
        }
        scroll.addView(container)
        setContentView(scroll)
        render()
    }

    private fun text(value: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 7, 0, 7)
        }

    private fun button(value: String, action: () -> Unit): Button =
        Button(this).apply {
            text = value
            setOnClickListener { action() }
        }

    private fun render() {
        container.removeAllViews()
        container.addView(text("N O V A   /   W I D G E T   S T U D I O", 12f, gold, true))
        container.addView(text("Your screen,\nyour atmosphere.", 31f, white, true))
        container.addView(text("Premium widgets with your own typography.", 15f, muted))
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 15, 18, 15)
            setBackgroundColor(panel)
        }
        card.addView(text("FONT LIBRARY", 12f, gold, true))
        card.addView(text("Import your own .ttf or .otf font, preview it, then apply it to Nova widgets.", 14f, white))
        val current = getSharedPreferences("nova", MODE_PRIVATE).getString("font_path", null)
        val currentName = current?.let { File(it).name } ?: "Default system font"
        card.addView(text("Selected: $currentName", 13f, muted))
        card.addView(button("＋  Import a font (.TTF / .OTF)") { pickFont() })
        val fonts = File(filesDir, "fonts")
        val fontFiles = fonts.listFiles()?.filter { it.isFile && (it.extension.equals("ttf", true) || it.extension.equals("otf", true)) } ?: emptyList()
        if (fontFiles.isNotEmpty()) {
            card.addView(text("YOUR FONTS", 11f, gold, true))
            fontFiles.forEach { file ->
                val item = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 8, 0, 8)
                }
                val sample = text("Aa  ${file.name}", 20f, white)
                try { sample.typeface = Typeface.createFromFile(file) } catch (_: Exception) {}
                item.addView(sample)
                item.addView(button(if (file.absolutePath == current) "Selected ✓" else "Use this font") {
                    getSharedPreferences("nova", MODE_PRIVATE).edit().putString("font_path", file.absolutePath).apply()
                    updateAllWidgets()
                    Toast.makeText(this, "Font applied to Nova widgets", Toast.LENGTH_SHORT).show()
                    render()
                })
                card.addView(item)
            }
        } else {
            card.addView(text("No custom fonts yet. Import a font file to begin.", 13f, muted))
        }
        container.addView(card)
        container.addView(text("WIDGET COLLECTION", 12f, gold, true))
        listOf(
            "CLOCK" to "Elegant time display",
            "DATE" to "Daily date and day",
            "QUOTES" to "Daily inspiration",
            "WEATHER" to "Weather widget — live provider setup pending",
            "BATTERY" to "Device battery percentage",
            "PRAYER" to "Prayer widget — calculation setup pending"
        ).forEach { (title, desc) ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(16, 10, 16, 10)
                setBackgroundColor(panel)
            }
            item.addView(text(title, 12f, gold, true))
            item.addView(text(desc, 14f, white))
            val lp = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 8, 0, 0) }
            container.addView(item, lp)
        }
        container.addView(text("To add: long-press your Android home screen → Widgets → Nova Widgets.", 13f, muted))
    }

    private fun pickFont() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "font/ttf", "font/otf", "application/x-font-ttf",
                "application/x-font-opentype", "application/octet-stream"
            ))
        }
        startActivityForResult(intent, requestFont)
    }

    @Deprecated("Deprecated in Android API; retained for broad device compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != requestFont || resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        try {
            val displayName = "NovaFont_${System.currentTimeMillis()}.${guessExtension(uri)}"
            val dir = File(filesDir, "fonts").apply { mkdirs() }
            val target = File(dir, displayName)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(target).use { output -> input.copyTo(output) }
            } ?: throw Exception("Cannot open selected file")
            // Validate the font before saving it as the active choice.
            Typeface.createFromFile(target)
            getSharedPreferences("nova", MODE_PRIVATE).edit().putString("font_path", target.absolutePath).apply()
            updateAllWidgets()
            Toast.makeText(this, "Font imported and applied", Toast.LENGTH_LONG).show()
            render()
        } catch (e: Exception) {
            Toast.makeText(this, "Could not import this font. Choose a valid TTF or OTF file.", Toast.LENGTH_LONG).show()
        }
    }

    private fun guessExtension(uri: Uri): String {
        val name = uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase()
        return if (name == "otf") "otf" else "ttf"
    }

    private fun updateAllWidgets() {
        val manager = android.appwidget.AppWidgetManager.getInstance(this)
        val classes = listOf(ClockWidget::class.java, DateWidget::class.java, QuoteWidget::class.java,
            WeatherWidget::class.java, BatteryWidget::class.java, PrayerWidget::class.java)
        classes.forEach { cls ->
            val ids = manager.getAppWidgetIds(android.content.ComponentName(this, cls))
            if (ids.isNotEmpty()) {
                val intent = Intent(this, cls).setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    .putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                sendBroadcast(intent)
            }
        }
    }
}
