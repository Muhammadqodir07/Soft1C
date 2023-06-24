package com.example.soft1c.utils

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.soft1c.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun setLocale(lang: String) {
        LocaleHelper().setLocale(this@MainActivity, lang)
        recreate()
    }

    override fun attachBaseContext(base: Context) {
        LocaleHelper().setLocale(base, LocaleHelper().getLanguage(base))
        super.attachBaseContext(LocaleHelper().onAttach(base))
    }
}