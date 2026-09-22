package com.example.mqttpractica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mqttpractica.ui.theme.MqttpracticaTheme
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import java.util.UUID

class MainActivity : ComponentActivity() {
    private val brokerIp = "192.168.100.115"
    private val topic = "casa/led"
    private var client: com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread {
            try {
                client = Mqtt5Client.builder()
                    .identifier(UUID.randomUUID().toString())
                    .serverHost(brokerIp)
                    .serverPort(1883)
                    .buildBlocking()
                client?.connect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        setContent {
            MqttpracticaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MqttScreen(
                        onPublish = { message ->
                            Thread {
                                try {
                                    client?.publishWith()
                                        ?.topic(topic)
                                        ?.payload(message.toByteArray())
                                        ?.send()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }.start()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Thread {
            try {
                client?.disconnect()
            } catch (e: Exception) {}
        }.start()
    }
}

@Composable
fun MqttScreen(onPublish: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Control MQTT - Práctica", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onPublish("ENCENDIDO") },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(text = "ENCENDER", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onPublish("APAGADO") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = "APAGAR", style = MaterialTheme.typography.titleMedium)
        }
    }
}