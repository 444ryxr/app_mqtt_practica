package com.example.mqttpractica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF1D4ED8),
                    onPrimary = Color.White,
                    background = Color(0xFFF1F5F9),
                    surface = Color.White,
                    onSurface = Color(0xFF1E293B),
                    error = Color(0xFFEF4444)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf("connect") }
    var brokerIp by remember { mutableStateOf("192.168.100.") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var mqttClient by remember { mutableStateOf<com.hivemq.client.mqtt.mqtt5.Mqtt5Client?>(null) }
    val coroutineScope = rememberCoroutineScope()

    when (currentScreen) {
        "connect" -> {
            ConnectView(
                ipValue = brokerIp,
                onIpChange = { brokerIp = it },
                isLoading = isLoading,
                errorMessage = errorMessage,
                onConnectClick = {
                    if (brokerIp.isBlank()) {
                        errorMessage = "Ingresa una dirección IP válida"
                        return@ConnectView
                    }
                    isLoading = true
                    errorMessage = ""

                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val client = Mqtt5Client.builder()
                                .identifier(UUID.randomUUID().toString())
                                .serverHost(brokerIp.trim())
                                .serverPort(1883)
                                .build()

                            client.toBlocking().connect()
                            mqttClient = client

                            withContext(Dispatchers.Main) {
                                isLoading = false
                                currentScreen = "control"
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isLoading = false
                                errorMessage = "No se pudo conectar al broker. Revisa la IP."
                            }
                        }
                    }
                }
            )
        }
        "control" -> {
            ControlView(
                brokerIp = brokerIp,
                onDisconnect = {
                    coroutineScope.launch(Dispatchers.IO) {
                        try { mqttClient?.toBlocking()?.disconnect() } catch (_: Exception) {}
                    }
                    currentScreen = "connect"
                },
                onSendCommand = { command ->
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            mqttClient?.toBlocking()?.publishWith()
                                ?.topic("Conexion")
                                ?.payload(command.toByteArray(StandardCharsets.UTF_8))
                                ?.send()
                        } catch (_: Exception) {}
                    }
                }
            )
        }
    }
}

@Composable
fun ConnectView(
    ipValue: String,
    onIpChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String,
    onConnectClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Control MQTT",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ingresa la IP local de tu PC para iniciar",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = ipValue,
                    onValueChange = onIpChange,
                    label = { Text("IP del Broker (Ej. 192.168.1.50)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onConnectClick,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Conectar", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ControlView(
    brokerIp: String,
    onDisconnect: () -> Unit,
    onSendCommand: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Estado: Conectado", fontSize = 13.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold)
                Text(text = "IP: $brokerIp", fontSize = 12.sp, color = Color.Gray)
            }
            TextButton(onClick = onDisconnect) {
                Text(text = "Cambiar IP", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Panel LED",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { onSendCommand("ENCENDIDO") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
            ) {
                Text(text = "ENCENDER", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSendCommand("APAGADO") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
            ) {
                Text(text = "APAGAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            text = "Redes de Computadoras • 9B",
            fontSize = 11.sp,
            color = Color.Gray
        )
    }
}