# FitQuest

FitQuest è un'applicazione Android nativa sviluppata in Kotlin per il monitoraggio di attività sportive.

L'applicazione supporta:

- camminata;
- corsa;
- ciclismo;
- tracking GPS;
- visualizzazione del percorso su Google Maps;
- storico degli allenamenti;
- obiettivi settimanali;
- statistiche;
- dati meteo;
- persistenza locale tramite Room.

## Tecnologie principali

- Kotlin
- Jetpack Compose
- MVVM
- Repository Pattern
- Room
- Kotlin Coroutines / Flow
- Google Maps
- FusedLocationProviderClient
- Foreground Service
- Retrofit
- Moshi

## Configurazione Google Maps

Per abilitare la visualizzazione delle mappe, è necessario creare un file `secrets.properties` nella directory root contenente:

```properties
GOOGLE_MAPS_API_KEY=your_api_key_here
```

Il file non deve essere versionato.

## Progetto universitario

Corso di Programmazione Mobile
A.A. 2025/2026
Autore: Thomas Di Gregorio
