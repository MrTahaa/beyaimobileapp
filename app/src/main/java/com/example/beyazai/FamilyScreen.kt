package com.example.beyazai

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.beyazai.ui.theme.BeyazBackground
import com.example.beyazai.ui.theme.BeyazDivider
import com.example.beyazai.ui.theme.BeyazError
import com.example.beyazai.ui.theme.BeyazNavy
import com.example.beyazai.ui.theme.BeyazSky
import com.example.beyazai.ui.theme.BeyazSuccess
import com.example.beyazai.ui.theme.BeyazSuccessContainer
import com.example.beyazai.ui.theme.BeyazSurface
import com.example.beyazai.ui.theme.BeyazSurfaceVariant
import com.example.beyazai.ui.theme.BeyazTealDark
import com.example.beyazai.ui.theme.BeyazTextPrimary
import com.example.beyazai.ui.theme.BeyazTextSecondary
import com.example.beyazai.ui.theme.BeyazWarning
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
    val logsPerPage = 5
    var currentLogPage by remember { mutableStateOf(0) }

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

    val totalLogPages = maxOf(1, (canliLoglar.size + logsPerPage - 1) / logsPerPage)
    val visibleLoglar = canliLoglar
        .drop(currentLogPage * logsPerPage)
        .take(logsPerPage)

    LaunchedEffect(canliLoglar.size) {
        val lastPage = maxOf(0, totalLogPages - 1)
        if (currentLogPage > lastPage) {
            currentLogPage = lastPage
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BeyazBackground)) {
        // ÜST KISIM: HARİTA
        Box(modifier = Modifier.weight(0.45f)) {
            GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState) {
                Marker(state = MarkerState(position = cihazKonumu), title = "Cihaz Konumu")
            }

            // Şık Çıkış Butonu
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BeyazError),
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Çıkış",
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Çıkış", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // ALT KISIM: TELEMETRİ VE LOG PANELI
        Surface(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxWidth(),
            color = BeyazSurface,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
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
                        color = BeyazNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Yeşil "Canlı" Rozeti
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BeyazSuccessContainer)
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(BeyazSuccess, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Canlı", color = BeyazTealDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 1.dp,
                    color = BeyazDivider
                )

                Text(
                    text = "Akıllı Baston Sistem Logları",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BeyazTextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${canliLoglar.size} kayıt",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BeyazTextSecondary
                    )
                    Text(
                        text = "Sayfa ${currentLogPage + 1} / $totalLogPages",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BeyazNavy
                    )
                }

                // Log Listesi - her sayfada en fazla 5 kayıt
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    userScrollEnabled = false,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(visibleLoglar) { log -> // BURASI ARTIK canliLoglar DEĞİŞKENİNİ KULLANIYOR
                        // İçinde "Dikkat" geçen logları uyarı olarak renklendiriyoruz
                        val isWarning = log.second.contains("Dikkat")

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = BeyazSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isWarning) BeyazWarning.copy(alpha = 0.14f) else BeyazSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (isWarning) BeyazWarning else BeyazSky,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = log.first, // Saat
                                        fontSize = 12.sp,
                                        color = BeyazTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = log.second, // Log metni
                                        fontSize = 14.sp,
                                        color = BeyazTextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { if (currentLogPage > 0) currentLogPage-- },
                        enabled = currentLogPage > 0,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BeyazNavy),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Önceki sayfa",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Önceki", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { if (currentLogPage < totalLogPages - 1) currentLogPage++ },
                        enabled = currentLogPage < totalLogPages - 1,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BeyazNavy),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Sonraki", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Sonraki sayfa",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
