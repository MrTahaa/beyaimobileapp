package com.example.beyazai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.beyazai.ui.theme.BeyazaiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeyazaiTheme(dynamicColor = false) {
                beyazai()
            }
        }
    }
}

@Composable
fun beyazai() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "selection") {

        // 1. Ana Seçim Ekranı
        composable("selection") {
            SelectionScreen(navController)
        }

        // 2. Görme Engelli Ekranı
        composable("impaired") {
            VisuallyImpairedScreen(navController)
        }

        // 3. Aile Giriş (Şifre İsteyen Eşleştirme) Ekranı
        composable("family") {
            FamilyLoginScreen(navController)
        }
        // ESKİ HALİ: composable("family_dashboard/{pairingCode}") { ... }
// YENİ HALİ:
        composable("family_dashboard") {
            FamilyScreen(navController)
        }
    }
}
