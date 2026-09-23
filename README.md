# Práctica: Desarrollo de una App con MQTT

Aplicación móvil desarrollada para la asignatura de **Redes de Computadoras** (Grupo 9B), cuyo objetivo es establecer comunicación mediante el protocolo MQTT para enviar comandos de encendido y apagado, integrándose con **Node-RED** y **MQTT Explorer**.

## Arquitectura y Flujo de Comunicación
`APP (Android) → Broker MQTT → Node-RED → Dispositivo / LED`

## Tecnologías Utilizadas
* Android Studio (Kotlin & Jetpack Compose)
* Cliente MQTT (HiveMQ)
* Node-RED
* MQTT Explorer

## Configuración Importante
Para que la aplicación móvil pueda conectarse correctamente al broker MQTT que corre en tu PC, debes actualizar la variable `brokerIp` en el archivo `MainActivity.kt` utilizando la **dirección IP local de tu computadora**.

## Configuración del Broker MQTT (Mosquitto) y Firewall

Para permitir que dispositivos externos (como una aplicación móvil en la misma red Wi-Fi) se comuniquen con el broker MQTT corriendo en Windows, se realizaron las siguientes configuraciones:

### 1. Configuración de Mosquitto (`mosquitto.conf`)
Se modificó el archivo de configuración de Mosquitto (ubicado en `C:\Program Files\mosquitto\mosquitto.conf`) ejecutando el Bloc de notas como Administrador para agregar al final del archivo las siguientes líneas:

```text
listener 1883
allow_anonymous true