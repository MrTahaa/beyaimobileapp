package com.example.beyazai
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beyazai.ui.theme.BeyazBackground
import com.example.beyazai.ui.theme.BeyazNavy
import com.example.beyazai.ui.theme.BeyazSurface
import com.example.beyazai.ui.theme.BeyazSurfaceVariant
import com.example.beyazai.ui.theme.BeyazTeal
import com.example.beyazai.ui.theme.BeyazTextSecondary

@Composable
fun FamilyLoginScreen(navController: NavController) {
    val context = LocalContext.current
    // Kullanıcının gireceği 6 haneli kod hafızada tutuluyor
    var pairingCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeyazBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BeyazSurface,
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 0.dp,
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(BeyazSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Güvenlik",
                        modifier = Modifier.size(44.dp),
                        tint = BeyazTeal
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Cihaz Eşleştirme",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BeyazNavy,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Lütfen takip etmek istediğiniz cihazın 6 haneli güvenlik kodunu girin.",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    color = BeyazTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp, bottom = 28.dp)
                )

                OutlinedTextField(
                    value = pairingCode,
                    onValueChange = {
                        // Sadece 6 karaktere kadar izin ver
                        if (it.length <= 6) pairingCode = it
                    },
                    label = { Text("Eşleştirme Kodu") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = BeyazNavy,
                        unfocusedTextColor = BeyazNavy,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BeyazTeal,
                        focusedLabelColor = BeyazTeal,
                        unfocusedLabelColor = BeyazTextSecondary,
                        cursorColor = BeyazTeal,
                        unfocusedBorderColor = BeyazSurfaceVariant
                    ),
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
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BeyazTeal,
                        disabledContainerColor = BeyazSurfaceVariant,
                        disabledContentColor = BeyazTextSecondary
                    ),
                    enabled = pairingCode.length == 6 // 6 hane girilmeden buton basılamaz olsun
                ) {
                    Text(text = "Bağlan ve Konumu Bul", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
