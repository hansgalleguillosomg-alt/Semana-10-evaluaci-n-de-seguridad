# Vulnerabilities Report

**Aplicación:** hansgpsmapas (`com.example.hansgpsmapas`) v1.0
**Herramienta:** MobSF v4.6.1 (análisis estático)
**Archivo analizado:** `app-debug.apk` (SHA256: `484190120e92ae25c3cc8f69fe6b9c9bf266342e64aa19afe5e00d2720a21023`)
**Fecha del escaneo:** 27/10/2026
**Autor:** Claudio Veliz

## Summary

- Puntaje de seguridad MobSF: **45/100 (MEDIUM RISK), Grade B**
- Resumen de MobSF: High: 2 | Medium: 3 | Info: 0 | Secure: 1 | Hotspot: 1
- Vulnerabilidades documentadas en este informe: **5**
  - Critical/High: 3
  - Medium: 1
  - Low: 1

> Nota: la API Key aparece truncada en este documento a propósito, para no exponerla en el repositorio.

## Detailed Findings

### 1. API Key de Google Maps hardcodeada en el código

- **Description:** MobSF detectó la clave `google_maps_key` (`AIzaSyBQ…xopeU`) en texto plano dentro de los recursos de la app (`res/values/google_maps_api.xml`). Al estar en el APK y en el repositorio, cualquier persona puede extraerla decompilando la aplicación o leyendo el código fuente.
- **Severity:** High
- **Impact:** Un tercero podría usar la clave para consumir el servicio de Google Maps a nombre del propietario, generando cobros inesperados, agotamiento de cuota o abuso de la cuenta de Google Cloud.
- **Steps to Reproduce:**
  1. Generar el APK con *Build → Build APK(s)* en Android Studio.
  2. Cargar `app-debug.apk` en MobSF.
  3. Abrir la sección **Hardcoded Secrets / Possible Secrets** del informe estático.
  4. Observar `"google_maps_key" : "AIzaSy…"`.
- **Remediation:**
  - Mover la clave a `local.properties` (excluido de Git) e inyectarla con `manifestPlaceholders`.
  - Restringir la clave en Google Cloud Console por package name y huella SHA-1, y limitarla a *Maps SDK for Android*.
  - Revocar la clave expuesta y generar una nueva.

### 2. Aplicación en modo depuración (`android:debuggable=true`)

- **Description:** El manifest declara `android:debuggable=true` (MobSF: *Debug Enabled For App*). Esto permite conectar un depurador a la aplicación en ejecución.
- **Severity:** High
- **Impact:** Un atacante con acceso al dispositivo podría adjuntar un depurador, inspeccionar la memoria, leer datos internos y modificar el comportamiento de la app en tiempo de ejecución.
- **Steps to Reproduce:**
  1. Cargar el APK en MobSF.
  2. Revisar **Manifest Analysis**, hallazgo n.º 1: *Debug Enabled For App [android:debuggable=true]*.
- **Remediation:**
  - Distribuir siempre una compilación **release** (`debuggable=false`, valor por defecto en release).
  - No publicar nunca APK de tipo debug.

### 3. Aplicación firmada con certificado de depuración

- **Description:** MobSF indica que la app está firmada con el certificado `CN=Android Debug, O=Android, C=US`, el cual es genérico y generado automáticamente por Android Studio.
- **Severity:** High
- **Impact:** Cualquier desarrollador posee un certificado equivalente, por lo que no se puede garantizar la autenticidad ni la integridad de la app. No es aceptable para producción ni para publicar en Google Play.
- **Steps to Reproduce:**
  1. Abrir en MobSF la sección **Certificate Analysis**.
  2. Observar el hallazgo *Application signed with debug certificate* y el campo *X.509 Subject*.
- **Remediation:**
  - Crear un keystore propio y firmar la versión release con esa clave.
  - Guardar el keystore y sus contraseñas fuera del repositorio.

### 4. Copia de seguridad habilitada (`android:allowBackup=true`)

- **Description:** El manifest permite respaldar los datos de la aplicación mediante `adb backup`.
- **Severity:** Medium
- **Impact:** Si el usuario tiene la depuración USB activa, es posible extraer los datos privados de la app del dispositivo. Aunque hoy la app guarda pocos datos, es un riesgo si en el futuro almacena información sensible.
- **Steps to Reproduce:**
  1. Abrir **Manifest Analysis** en MobSF.
  2. Observar el hallazgo *Application Data can be Backed up [android:allowBackup=true]*.
- **Remediation:**
  - Establecer `android:allowBackup="false"` en `<application>`.
  - Definir `dataExtractionRules` / `fullBackupContent` para excluir datos sensibles.

### 5. Broadcast Receiver exportado (`ProfileInstallReceiver`)

- **Description:** El componente `androidx.profileinstaller.ProfileInstallReceiver` está exportado (`android:exported=true`). Aunque está protegido por el permiso `android.permission.DUMP`, MobSF recomienda verificar el nivel de protección de ese permiso.
- **Severity:** Low
- **Impact:** Componente expuesto a otras aplicaciones del dispositivo. El riesgo es bajo porque el componente proviene de la librería AndroidX y está protegido por un permiso del sistema, pero amplía la superficie de ataque.
- **Steps to Reproduce:**
  1. Abrir **Manifest Analysis** en MobSF.
  2. Observar el hallazgo n.º 3 sobre *Broadcast Receiver … is Protected by a permission*.
- **Remediation:**
  - Mantener actualizada la librería `androidx.profileinstaller`.
  - Si no se usa, quitar el componente con `tools:node="remove"` en el manifest.

## Observaciones adicionales (informativas)

- **Permisos:** la app usa 4 de los 25 permisos que MobSF clasifica como habitualmente abusados por malware (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_NETWORK_STATE`, `INTERNET`). Son necesarios para la función de mapa, pero deben solicitarse con el principio de mínimo privilegio.
- **Network Security:** el informe no muestra ninguna configuración de red definida (sección *Network Security* vacía). Se recomienda definir una `network_security_config.xml` que bloquee el tráfico sin cifrar.
- **Código:** la sección *Code Analysis* no reportó hallazgos.

## Resultado tras las correcciones (re-escaneo con `app-release.apk`)

| Hallazgo | Estado después de corregir |
|---|---|
| 1. API Key hardcodeada | Corregido en el código: ya no se detecta ningún secreto en MobSF (0 secretos). La clave se lee desde `local.properties`. Pendiente: restringirla en Google Cloud. |
| 2. `android:debuggable=true` | Corregido: sin hallazgo en la versión release. |
| 3. Certificado de depuración | Corregido: APK firmado con keystore propio (`CN=hans galleguillos`). |
| 4. `allowBackup=true` | Corregido: `allowBackup="false"` y `data_extraction_rules.xml`. |
| 5. Receiver exportado | Corregido: componente eliminado (Exported Receivers: 0). |

**App Security Score: 45/100 (Grade B) → 85/100 (Grade A, Low Risk). Hallazgos High: 2 → 0.**

### Hallazgos que permanecen (residuales)

- **Network Security (Warning):** la configuración confía en los certificados del sistema. Mejora futura: certificate pinning si la app llega a consumir un backend propio.
- **Code Analysis (Info/Warning):** MobSF reporta uso de logs y de un generador aleatorio no criptográfico. Los archivos señalados están en `defpackage/*`, es decir, código de librerías (Google Play Services / AndroidX) ofuscado por R8, no código propio de la app.
- **Behaviour Analysis:** MobSF lista comportamientos como lectura de SMS o contactos en archivos `defpackage/*`. Corresponden al código interno de las librerías incluidas: la app no declara esos permisos (solo ubicación, red e internet), por lo que se consideran falsos positivos para esta aplicación.
