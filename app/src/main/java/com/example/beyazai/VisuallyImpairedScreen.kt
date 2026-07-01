package com.example.beyazai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale

@Composable
fun VisuallyImpairedScreen(navController: NavController) {
    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var anlikEnlem by remember { mutableStateOf(0.0) }
    var anlikBoylam by remember { mutableStateOf(0.0) }

    // --- RASPBERRY PI CANLI LOG SİSTEMİ ---
    val canliLoglar = remember { mutableStateListOf<String>() }
    var aktifLogIndeksi by remember { mutableStateOf(0) }
    var kaydirmaMiktari by remember { mutableStateOf(0f) }

    // Firebase'den veri gelene kadar veya liste boşken çökmemesi için emniyet subabı
    val mevcutLoglar = if (canliLoglar.isEmpty()) listOf("Henüz sistem uyarısı bulunmuyor.") else canliLoglar
    // ---------------------------------------

    val sabitKod = "448816"
    val database = FirebaseDatabase.getInstance("https://beyazai-default-rtdb.europe-west1.firebasedatabase.app/")

    var hasLocationPermission by remember {
        mutableStateOf(
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // YENİ EKLENEN: FİREBASE LOG DİNLEYİCİSİ
    LaunchedEffect(sabitKod) {
        val logRef = database.getReference("$sabitKod/loglar")
        logRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                canliLoglar.clear()
                for (child in snapshot.children) {
                    val mesaj = child.child("mesaj").getValue(String::class.java) ?: ""
                    canliLoglar.add(mesaj)
                }
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
    }

    // Liste boyutu anlık güncellenirken indeks taşmalarını önleme koruması
    LaunchedEffect(canliLoglar.size) {
        if (aktifLogIndeksi >= mevcutLoglar.size) {
            aktifLogIndeksi = if (mevcutLoglar.isNotEmpty()) mevcutLoglar.size - 1 else 0
        }
    }

    DisposableEffect(context) {
        var textToSpeech: TextToSpeech? = null
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("tr", "TR")
                val msg = "Sistem aktif. Konum verisi alınıyor. Güvenlik kodu: ${sabitKod.toCharArray().joinToString(" ")}"
                textToSpeech?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
        tts = textToSpeech
        onDispose { textToSpeech?.stop(); textToSpeech?.shutdown() }
    }

    DisposableEffect(hasLocationPermission) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        var locationCallback: LocationCallback? = null

        if (hasLocationPermission) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        anlikEnlem = location.latitude
                        anlikBoylam = location.longitude

                        val ref = database.getReference("$sabitKod/konum")
                        ref.setValue(mapOf("lat" to location.latitude, "lng" to location.longitude))
                    }
                }
            }

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
            }
        }

        onDispose {
            locationCallback?.let {
                fusedLocationClient.removeLocationUpdates(it)
            }
        }
    }

    // ARAYÜZ VE GESTURE KONTROLLERİ
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            // 1. ÇİFT DOKUNUŞ (ANA MENÜYE DÖNÜŞ)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        tts?.speak("Ana menüye dönülüyor", TextToSpeech.QUEUE_FLUSH, null, null)
                        navController.navigate("selection") {
                            popUpTo("impaired") { inclusive = true }
                        }
                    }
                )
            }
            // 2. SAĞA/SOLA KAYDIRMA (CANLI LOG OKUMA)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (kaydirmaMiktari > 100) {
                            // Sağa Kaydırma (Eski/Önceki Log)
                            if (aktifLogIndeksi > 0) {
                                aktifLogIndeksi--
                                tts?.speak(mevcutLoglar[aktifLogIndeksi], TextToSpeech.QUEUE_FLUSH, null, null)
                            } else {
                                tts?.speak("İlk uyarıdasınız.", TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        } else if (kaydirmaMiktari < -100) {
                            // Sola Kaydırma (Yeni/Sonraki Log)
                            if (aktifLogIndeksi < mevcutLoglar.size - 1) {
                                aktifLogIndeksi++
                                tts?.speak(mevcutLoglar[aktifLogIndeksi], TextToSpeech.QUEUE_FLUSH, null, null)
                            } else {
                                tts?.speak("Son uyarıdasınız.", TextToSpeech.QUEUE_FLUSH, null, null)
                            }
                        }
                        kaydirmaMiktari = 0f // Kaydırma işlemi bitince sıfırla
                    }
                ) { change, dragAmount ->
                    change.consume()
                    kaydirmaMiktari += dragAmount.x // Parmağın X eksenindeki hareketini hesapla
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Konum İkonu",
                tint = if (anlikEnlem != 0.0) Color(0xFF00E676) else Color.Gray,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = if (hasLocationPermission) "Gözetim Modu Aktif" else "Konum İzni Bekleniyor...",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFFFFD600), RoundedCornerShape(12.dp))
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "KOD: $sabitKod",
                    color = Color(0xFFFFD600),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = if (anlikEnlem != 0.0) "GPS BULUNDU" else "UYDU ARANIYOR...",
                    color = if (anlikEnlem != 0.0) Color(0xFF00E676) else Color(0xFFFF5252),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (anlikEnlem != 0.0) "Enlem: $anlikEnlem\nBoylam: $anlikBoylam" else "-- / --",
                    color = Color.LightGray,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }

            // ARAYÜZE LOGLARI GÖRENLER İÇİN BİLGİ KUTUSU
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Son Uyarı: ${mevcutLoglar[aktifLogIndeksi]}",
                color = Color(0xFF4FC3F7),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Ana menü: Çift Dokun\nLog Okuma: Sağa/Sola Kaydır",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}