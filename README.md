# Argument Portal

Tato aplikace vznikla jako softwarová aplikace v rámci bakalářské práce na FEL ČVUT (2026). Jedná se o aplikaci, která využívá prvky konverzační umělé inteligence a pokročilou správu terminologie.

Systém uživatelům umožňuje sémanticky ukotvovat debaty pomocí významových slovníků, automaticky detekovat argumentační fauly a asistovaně generovat nové argumenty.

## Architektura systému

Projekt se skládá ze čtyř hlavních komponent:

1. AP Server (Backend) - Hlavní backendová služba v Javě, která vystavuje REST API a řídí komunikaci s externími službami.
2. AP web application (Frontend) - Hlavní klientské rozhraní napsané v Reactu, které slouží pro komplexní práci s debatami a pro analýzu textu.
3. AP Extension (Frontend) - Rozšíření prohlížeče, zajišťující přenos informací mez platgormou Kialo a aplikací AP.
4. Fallacy detector - Služba napsaná v Pythonu, která slouží pro analýzu textu na přítomnost argumentačního faulu.

Projekt dále obsahuje:

1. Keycloak - Autentizační komponenta zajišťující SSO a přihlášení přes OAuth 2.0.
2. TermIt - Systém pro správu slovníků a sémantickou anotaci textu. Vyvinutý byl KBSS ČVUT.

Základní konfigurace TermIt a Keycloak, společně s deployment scripty byly převzaty z jednoho z projektů od skupiny KBSS ČVUT.

## Deployment
- Vytvořte .env soubor z .env.example a doplňte svá hesla
  - Postupujte podle https://developers.google.com/identity/protocols/oauth2 a vygenerovaný Client ID a Client secret do .env proměnných GOOGLE_SECRET_KEY a GOOGLE_CLIENT_ID. 
    -  Redirect adresa bude: http://localhost:1234/termit/sluzby/auth/realms/termit/broker/google/endpoint
  - Získejte API key na https://openai.com/api a vložte do .env proměnné OPENAI_API_KEY
- Stáhněte model.safetensors z "https://drive.google.com/drive/u/2/folders/1bAIetvpFpkq0zIOuoUn2xTVmc7sMIrb4"
- Vložte model.safetensors do /fallacy-detector/checkpoint-1344 folder
- spusťte "docker compose up --build"
- Otevřete http://localhost:5173

## Spuštění doplňku
Stáhněte .zip soubor "AP-extension.zip" a rozbalte ho. Otevřete chrome://extensions a zvolte "Načíst rozbalené". Zvolte složku, kterou jste rozbalili. Program poté otevřete na stránce Kialo.com
přes tlačítko puzzle v pravém horním rohu obrazovky Chrome.