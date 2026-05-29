package com.example.brawloverlaynotes

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.brawloverlaynotes.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val templates = arrayOf(
        "Gem Grab", "Solo Showdown", "Duo Showdown", "Brawl Ball",
        "Hot Zone", "Knockout", "Bounty", "Wipeout", "Своя заметка"
    )

    private var bgAlpha = 70
    private var textSize = 16

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, templates)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerTemplate.adapter = adapter

        binding.spinnerTemplate.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    val selected = templates[position]
                    if (selected != "Своя заметка") {
                        binding.etNote.setText(getTemplateText(selected))
                    }
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

        binding.seekBarAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                bgAlpha = progress
                binding.tvAlphaLabel.text = "Прозрачность фона: $bgAlpha%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.seekBarSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textSize = progress + 10
                binding.tvSizeLabel.text = "Размер текста: ${textSize}sp"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnShow.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
                Toast.makeText(this, "Дайте разрешение на отображение поверх других приложений", Toast.LENGTH_LONG).show()
            } else {
                val noteText = binding.etNote.text.toString().ifBlank { "Заметка пуста" }
                startOverlayService(noteText)
            }
        }

        binding.btnHide.setOnClickListener {
            stopOverlayService()
        }
    }

    private fun getTemplateText(name: String): String {
        return when (name) {
            "Gem Grab" -> "💎 Gem Grab\n1. Контроль центра\n2. Отступайте при 3+ кристаллах\n3. Добивайте врагов"
            "Solo Showdown" -> "🏆 Solo Showdown\n1. Собирайте кубы в начале\n2. Не лезьте в драку\n3. Прячьтесь в кустах"
            "Duo Showdown" -> "🤝 Duo Showdown\n1. Держитесь рядом с напарником\n2. Собирайте кубы вместе\n3. Возрождайте партнёра"
            "Brawl Ball" -> "⚽ Brawl Ball\n1. Контроль мяча\n2. Пасы через стены\n3. Защита своих ворот"
            "Hot Zone" -> "🔥 Hot Zone\n1. Встаньте в зоне\n2. Выталкивайте врагов\n3. Лечитесь у ящиков"
            "Knockout" -> "💥 Knockout\n1. Не умирайте первым\n2. Используйте укрытия\n3. Фокусите одного врага"
            "Bounty" -> "⭐ Bounty\n1. Добивайте врагов\n2. Не умирайте со звёздами\n3. Контроль карты"
            "Wipeout" -> "🎯 Wipeout\n1. Агрессия на флангах\n2. Фокусите слабых\n3. Не теряйте преимущество"
            else -> ""
        }
    }

    private fun startOverlayService(noteText: String) {
        val intent = Intent(this, OverlayService::class.java).apply {
            putExtra("note_text", noteText)
            putExtra("bg_alpha", bgAlpha)
            putExtra("text_size", textSize)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "Overlay показан", Toast.LENGTH_SHORT).show()
    }

    private fun stopOverlayService() {
        stopService(Intent(this, OverlayService::class.java))
        Toast.makeText(this, "Overlay скрыт", Toast.LENGTH_SHORT).show()
    }
}
