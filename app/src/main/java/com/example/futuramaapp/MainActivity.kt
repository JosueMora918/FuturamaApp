package com.example.futuramaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.futuramaapp.ui.theme.FuturamaTriviaTheme

// Simplemente manda a llamar a la app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FuturamaTriviaTheme {
                FuturamaTriviaApp()
            }
        }
    }
}
