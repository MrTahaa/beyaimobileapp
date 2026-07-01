package com.example.beyazai

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beyazai.ui.theme.BeyazNavy
import com.example.beyazai.ui.theme.BeyazNavyLight
import com.example.beyazai.ui.theme.BeyazSky
import com.example.beyazai.ui.theme.BeyazTeal
import java.util.Locale

@Composable
fun SelectionScreen(navController: NavController) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale("tr", "TR"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.setLanguage(Locale("tr"))
                }

                val yonerge = "Akıllı Görüş uygulamasına hoş geldiniz. Görme engelli girişi için ekranın üst yarısına, aile paneli için ekranın alt yarısına dokunun."
                textToSpeech?.speak(yonerge, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
        tts = textToSpeech

        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    // Ekranı tam ortadan ikiye bölen yapı (Asla bozulmamalı)
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // EKRANIN ÜST YARISI (%50) - GÖRME ENGELLİ
        Button(
            onClick = {
                tts?.stop()
                navController.navigate("impaired")
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RectangleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BeyazNavy)
        ) {
            SelectionActionContent(
                title = "Görme Engelli\nGirişi",
                subtitle = "Yüksek kontrast takip modu",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Görme Engelli İkonu",
                        modifier = Modifier.size(76.dp),
                        tint = Color.White
                    )
                },
                background = Brush.verticalGradient(
                    colors = listOf(BeyazNavy, BeyazNavyLight)
                )
            )
        }

        // EKRANIN ALT YARISI (%50) - AİLE PANELİ
        Button(
            onClick = {
                tts?.stop()
                navController.navigate("family")
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RectangleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BeyazTeal)
        ) {
            SelectionActionContent(
                title = "Aile Paneli\nGirişi",
                subtitle = "Konum ve uyarı takibi",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Aile Paneli İkonu",
                        modifier = Modifier.size(76.dp),
                        tint = Color.White
                    )
                },
                background = Brush.verticalGradient(
                    colors = listOf(BeyazTeal, BeyazSky)
                )
            )
        }
    }
}

@Composable
private fun SelectionActionContent(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    background: Brush
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .background(Color.White.copy(alpha = 0.15f), RectangleShape),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = title,
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 38.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.24f),
                        offset = Offset(1f, 3f),
                        blurRadius = 6f
                    )
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.82f),
                textAlign = TextAlign.Center
            )
        }
    }
}
