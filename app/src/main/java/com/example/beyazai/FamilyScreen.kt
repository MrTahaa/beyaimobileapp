package com.example.beyazai

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.database.FirebaseDatabase

@Composable
fun FamilyScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("BeyazAI_Prefs", Context.MODE_PRIVATE) }

    // Uygulama açılınca hafızadaki kodu al
    var savedCode by remember { mutableStateOf(sharedPrefs.getString("kayitli_kod", "")) }

    if (savedCode.isNullOrEmpty()) {
        // Eğer kod yoksa kullanıcıyı giriş ekranına geri at
        LaunchedEffect(Unit) {
            navController.navigate("family") {
                popUpTo("family_dashboard") { inclusive = true }
            }
        }
    } else {
        // Kod varsa haritayı göster
        MapDisplay(pairingCode = savedCode!!, onLogout = {
            sharedPrefs.edit().remove("kayitli_kod").apply()
            savedCode = ""
            navController.navigate("selection")
        })
    }
}

@Composable
fun MapDisplay(pairingCode: String, onLogout: () -> Unit) {
    var cihazKonumu by remember { mutableStateOf(LatLng(41.6771, 26.5557)) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.6771, 26.5557), 15f)
    }

    // 1. ARTIK FAKE LOG YOK, CANLI FİREBASE LİSTESİ VAR!
    val canliLoglar = remember { mutableStateListOf<Pair<String, String>>() }

    // HARİTA KONUM DİNLEYİCİSİ (Aynı kaldı)
    LaunchedEffect(pairingCode) {
        val ref = FirebaseDatabase.getInstance().getReference("$pairingCode/konum")
        ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val lat = snapshot.child("lat").getValue(Double::class.java) ?: 41.6771
                val lng = snapshot.child("lng").getValue(Double::class.java) ?: 26.5557
                cihazKonumu = LatLng(lat, lng)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    // 2. YENİ EKLENEN: FİREBASE LOG DİNLEYİCİSİ
    LaunchedEffect(pairingCode) {
        val logRef = FirebaseDatabase.getInstance().getReference("$pairingCode/loglar")
        logRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                canliLoglar.clear() // Yeni veri gelince eski listeyi temizle
                for (child in snapshot.children) {
                    val saat = child.child("saat").getValue(String::class.java) ?: ""
                    val mesaj = child.child("mesaj").getValue(String::class.java) ?: ""
                    canliLoglar.add(Pair(saat, mesaj))
                }
                // En yeni loglar en üstte görünsün diye listeyi ters çeviriyoruz
                canliLoglar.reverse()
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    LaunchedEffect(cihazKonumu) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(cihazKonumu, 15f), 1000)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F6FA))) {
        // ÜST KISIM: HARİTA (%60)
        Box(modifier = Modifier.weight(0.6f)) {
            GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState) {
                Marker(state = MarkerState(position = cihazKonumu), title = "Cihaz Konumu")
            }

            // Şık Çıkış Butonu
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Çıkış",
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Çıkış", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // ALT KISIM: TELEMETRİ VE LOG PANELI (%40)
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            // Başlık ve Canlı Durum İndikatörü
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Takip Kodu: $pairingCode",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color(0xFF2F3640)
                )

                // Yeşil "Canlı" Rozeti
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Canlı", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFECDDCD), thickness = 1.dp)

            Text(
                text = "Akıllı Baston Sistem Logları",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Log Listesi (Scroll edilebilir) - ARTIK CANLI LOGLARI OKUYOR
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(canliLoglar) { log -> // BURASI ARTIK canliLoglar DEĞİŞKENİNİ KULLANIYOR
                    // İçinde "Dikkat" geçen logları uyarı olarak renklendiriyoruz
                    val isWarning = log.second.contains("Dikkat")

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isWarning) Color(0xFFFF9800) else Color(0xFF2196F3),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = log.first, // Saat
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = log.second, // Log metni
                                    fontSize = 14.sp,
                                    color = Color(0xFF2F3640),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}