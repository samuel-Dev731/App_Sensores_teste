package com.exemplo.testesensores

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val sensoresParaTestar = listOf(
            SensorDef("🚀 Acelerômetro", Sensor.TYPE_ACCELEROMETER, "Mede aceleração linear (inclinação)."),
            SensorDef("🔄 Giroscópio", Sensor.TYPE_GYROSCOPE, "Mede velocidade de rotação angular."),
            SensorDef("🧭 Magnetômetro", Sensor.TYPE_MAGNETIC_FIELD, "Mede campos magnéticos (Bússola)."),
            SensorDef("📱 Proximidade", Sensor.TYPE_PROXIMITY, "Detecta objetos perto da tela."),
            SensorDef("🌡️ Temperatura", Sensor.TYPE_AMBIENT_TEMPERATURE, "Mede a temperatura ambiente."),
            SensorDef("📉 Barômetro", Sensor.TYPE_PRESSURE, "Mede a pressão atmosférica (altitude)."),
            SensorDef("☀️ Luz", Sensor.TYPE_LIGHT, "Mede a iluminação do ambiente (Lux)."),
            SensorDef("💧 Umidade", Sensor.TYPE_RELATIVE_HUMIDITY, "Mede a umidade relativa do ar."),
            SensorDef("🚶 Contador de Passos", Sensor.TYPE_STEP_COUNTER, "Conta os passos dados desde o boot.")
        )

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SensorListScreen(sensorManager, sensoresParaTestar)
                }
            }
        }
    }
}

data class SensorDef(val nome: String, val tipo: Int, val descricao: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorListScreen(sensorManager: SensorManager, listaSensores: List<SensorDef>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teste de Todos os Sensores", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(listaSensores) { sensorDef -> SensorCard(sensorManager, sensorDef) }
        }
    }
}

@Composable
fun SensorCard(sensorManager: SensorManager, sensorDef: SensorDef) {
    val sensor: Sensor? = sensorManager.getDefaultSensor(sensorDef.tipo)
    val existe = sensor != null
    var valoresTexto by remember { mutableStateOf("Aguardando dados...") }

    if (sensor != null) {
        DisposableEffect(sensorDef.tipo) {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let { valoresTexto = it.values.joinToString("   ") { valor -> String.format("%.2f", valor) } }
                }
                override fun onAccuracyChanged(s: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val corDoCard = if (existe) Color(0xFF1E1E1E) else Color(0xFF2C2C2C)
    val corDoTextoPrincipal = if (existe) Color(0xFF64B5F6) else Color.Gray
    val corDoTextoSecundario = if (existe) Color.White else Color.DarkGray

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = corDoCard)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(sensorDef.nome, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = corDoTextoPrincipal)
                if (!existe) Text("Não disponível", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
            }
            Text(sensorDef.descricao, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (existe) "Valores: $valoresTexto" else "Hardware ausente neste dispositivo.",
                fontSize = 14.sp, fontWeight = FontWeight.Medium, color = corDoTextoSecundario
            )
        }
    }
}
