# Práctica: Desarrollo de una App con MQTT

Aplicación móvil desarrollada para la asignatura de **Redes de Computadoras** (Grupo 9A), cuyo objetivo es establecer comunicación mediante el protocolo MQTT para enviar comandos de encendido y apagado, integrándose con **Node-RED** y **MQTT Explorer**.

## Arquitectura y Flujo de Comunicación
`APP (Android) → Broker MQTT → Node-RED → Dispositivo / LED`

## Tecnologías Utilizadas
* Android Studio (Kotlin & Jetpack Compose)
* Cliente MQTT (HiveMQ)
* Node-RED
* MQTT Explorer

## Configuración Importante
Para que la aplicación móvil pueda conectarse correctamente al broker MQTT que corre en tu PC, debes actualizar la variable `brokerIp` en el archivo `MainActivity.kt` utilizando la **dirección IP local de tu computadora** 