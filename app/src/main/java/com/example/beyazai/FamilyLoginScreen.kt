package com.example.beyazai
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun FamilyLoginScreen(navController: NavController) {
    val context = LocalContext.current
    // Kullanıcının gireceği 6 haneli kod hafızada tutuluyor
    var pairingCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Güvenlik",
            modifier = Modifier.size(80.dp),
            tint = Color(0xFF43A047)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Cihaz Eşleştirme",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "Lütfen takip etmek istediğiniz cihazın 6 haneli güvenlik kodunu girin.",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        OutlinedTextField(
            value = pairingCode,
            onValueChange = {
                // Sadece 6 karaktere kadar izin ver
                if (it.length <= 6) pairingCode = it
            },
            label = { Text("Eşleştirme Kodu") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val sharedPrefs = context.getSharedPreferences("BeyazAI_Prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("kayitli_kod", pairingCode).apply() // Kodu hafızaya atıyoruz
                navController.navigate("family_dashboard") {
                    popUpTo("family") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
            enabled = pairingCode.length == 6 // 6 hane girilmeden buton basılamaz olsun
        ) {
            Text(text = "Bağlan ve Konumu Bul", fontSize = 18.sp)
        }
    }
}