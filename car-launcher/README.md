# Car Launcher

Eigen startscherm (launcher) voor een Carlinkit T-Box (Android AI box) in de auto.

## Wat het doet

- **Startscherm** met grote klok, datum en grote tegels, donker thema voor in de auto.
- **Favoriete apps**: zet bijvoorbeeld calimoto, Kurviger of Spotify met één tik op het startscherm.
- **Home Assistant**: tegels voor lampen, schakelaars, poort/garagedeur (`cover`), sloten, scripts,
  scènes, knoppen en sensoren. Tik = schakelen/uitvoeren. Status wordt elke 15 s ververst.
- Home-knop brengt je altijd terug naar het startscherm; scherm blijft aan.

## APK downloaden

Elke push bouwt via GitHub Actions een APK: tabblad **Actions** → workflow *Car Launcher APK* →
laatste run → onderaan **Artifacts** → `car-launcher-apk` (zip met de `.apk`).

## Installeren op de T-Box

1. Zet de APK op een USB-stick of SD-kaart, of download hem in de browser van de box.
2. Open hem met de bestandsbeheerder en sta "installeren uit onbekende bronnen" toe.
3. Open *Car Launcher* → Instellingen → **Standaard startscherm kiezen** → kies *Car Launcher*.
   (Ondersteunt de box dit menu niet, druk dan op de Home-knop; Android vraagt dan welke launcher.)

Nieuwe versies kun je gewoon over de oude heen installeren (vaste ondertekeningssleutel).

## Home Assistant koppelen

1. In HA: Profiel → Beveiliging → **Langlevende toegangstokens** → token maken.
   Tip: maak hiervoor een aparte HA-gebruiker zonder beheerrechten.
2. In de app: Instellingen → adres + token → *Opslaan en verbinden*.
3. Vink de entiteiten aan die je op het startscherm wilt.

Het adres moet onderweg bereikbaar zijn (Nabu Casa of eigen domein met https), want de box zit
in de auto op 4G of op de hotspot van je telefoon.

## Bekende beperkingen

- Het token staat onversleuteld in de app-opslag van de box.
- `carlauncher-debug.keystore` staat bewust in de repo zodat updates over elkaar heen installeren;
  het is geen geheime sleutel. Bedoeld voor eigen gebruik, niet voor de Play Store.
