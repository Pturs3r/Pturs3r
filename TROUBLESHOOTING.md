# AdGuard Home blokkeert geen ads via Tailscale — diagnose & fix

Symptoom: advertenties verschijnen nog steeds op de telefoon (bv. `racing.nl`)
terwijl AdGuard Home op de NAS zou moeten filteren. De telefoon is via
Tailscale met het thuisnetwerk verbonden.

Hypothese: de telefoon gebruikt AdGuard niet daadwerkelijk als DNS-resolver.

> **Vuistregel:** komt *álle* reclame nog door, dan is het een DNS-routingprobleem
> (de query bereikt AdGuard niet). Blijven er slechts 1–2 ads hangen, dan zijn dat
> meestal first-party ads die niet via DNS te blokkeren zijn — dat is verwacht.

---

## Diagnose — voer uit in volgorde

### Stap 1 — AdGuard Query Log: komen er queries vanaf het 100.x-IP?
AdGuard Home → **Query Log**. Bezoek op de telefoon `racing.nl` en filter op het
Tailscale-IP (100.x) van de telefoon (Tailscale-app toont dit adres).

| Bevinding | Betekenis |
|---|---|
| Geen enkele query vanaf 100.x | DNS bereikt AdGuard niet → ga naar stap 2/4/5 |
| Queries als "Processed" i.p.v. "Blocked" | DNS werkt, maar filterlijsten staan uit/leeg |
| Ad-domeinen "Blocked" maar toch ads | Waarschijnlijk first-party (zie onderaan) |

### Stap 2 — Luistert AdGuard op 0.0.0.0:53?
AdGuard → **Settings → DNS settings → Listen interfaces** → moet **All interfaces**
(0.0.0.0) zijn, niet alleen het LAN-IP — anders luistert hij niet op de
Tailscale-interface.

Verificatie via SSH op de NAS:
```bash
sudo ss -tulnp | grep ':53'
```
Verwacht: `0.0.0.0:53` (of expliciet het 100.x-adres). Zie je alleen
`192.168.x.x:53` → dit is de oorzaak.

### Stap 3 — Access settings (allowlist blokkeert 100.x?)
AdGuard → **Settings → DNS settings → Access settings**.
- **Allowed clients** niet leeg en alleen `192.168.x.x`? Dan worden 100.x-clients
  geweigerd. Voeg `100.0.0.0/8` (of het telefoon-IP) toe, of maak de lijst leeg.
- Controleer dat 100.x niet bij **Disallowed clients** staat.

### Stap 4 — Tailscale DNS (MagicDNS)
Tailscale admin console → **DNS**:
- **Nameservers**: het **Tailscale-IP van de NAS** (100.x) toegevoegd als custom
  nameserver — niet het LAN-IP (dat is buitenshuis onbereikbaar).
- **Override local DNS**: moet **AAN** staan. Staat dit uit, dan gebruikt de
  telefoon zijn eigen DNS en omzeilt AdGuard volledig.

Verificatie: Tailscale-app op de telefoon toont "Using Tailscale DNS".

### Stap 5 — Omzeilt de telefoon AdGuard?
- **Android**: Instellingen → Netwerk → **Private DNS** → **Uit** (of Automatisch).
  Een hostname hier (bv. `dns.google`) stuurt alle DNS langs AdGuard heen.
- **iOS**: Instellingen → naam → **iCloud → Private Relay → UIT**. Geen ander
  DNS-profiel actief onder Wi-Fi/VPN.
- **Browser**: Chrome/Firefox **Secure DNS / DNS-over-HTTPS** uitzetten.

---

## Meest waarschijnlijke oorzaak

Komt *álle* reclame door, dan zit het bijna altijd in **stap 4 of 5**:
1. Tailscale **"Override local DNS" staat uit** → DNS gaat nooit naar de NAS.
2. **Private DNS (Android) / Private Relay (iOS) / DoH in de browser** → DNS gaat
   versleuteld rechtstreeks naar Google/Cloudflare, langs AdGuard heen.

## Fix + verificatie
1. Pas de **eerst gevonden** oorzaak aan (begin praktisch bij stap 4 en 5).
2. **Verifieer in de Query Log**: bezoek `racing.nl` opnieuw. Je moet nu zien:
   - queries vanaf het 100.x-IP van de telefoon, én
   - ad/tracker-domeinen (`doubleclick.net`, `googlesyndication.com`) als **Blocked**.
3. Eindtest op de telefoon: open
   <https://d3ward.github.io/toolz/adblock> — een hoog blokkeerpercentage
   bevestigt dat AdGuard nu de DNS afhandelt.

## Let op — first-party ads
Ads die `racing.nl` vanaf het eigen domein serveert, zijn niet via DNS te
blokkeren. Blijven 1–2 ads hangen terwijl de Query Log wél blokkeert, dan is dat
verwacht gedrag en geen routingprobleem.
