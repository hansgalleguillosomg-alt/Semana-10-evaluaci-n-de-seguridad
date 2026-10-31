# Best Practices implementadas

**Aplicación:** hansgpsmapas (`com.example.hansgpsmapas`)
**Autor:** Antonio Villalobos

Cada práctica se relaciona con un hallazgo de [`vulnerabilities.md`](vulnerabilities.md) y con la evidencia del re-escaneo en MobSF (ver `vulnerability_report.pdf`).

## 1. Sacar la API Key del código fuente

- **Problema que resolvía:** la clave de Google Maps estaba escrita en `res/values/google_maps_api.xml` y se subiría al repositorio (Hallazgo 1).
- **Qué se cambió:**
  - Se eliminó `google_maps_api.xml`.
  - La clave se guarda en `local.properties` (excluido por `.gitignore`) como `MAPS_API_KEY`.
  - `app/build.gradle.kts` la lee y la inyecta al manifest:

  ```kotlin
  manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
  ```
  ```xml
  <meta-data android:name="com.google.android.geo.API_KEY" android:value="${MAPS_API_KEY}" />
  ```
- **Cómo mejora la seguridad:** el secreto ya no queda en el repositorio ni en el historial de Git. MobSF pasó de detectar 2 secretos a 0.
- **Limitación:** la clave sigue dentro del APK compilado (es inevitable con Google Maps). Por eso se recomienda restringirla en Google Cloud (ver `security_tips.md`).

## 2. Deshabilitar los respaldos de datos

- **Problema que resolvía:** `android:allowBackup=true` permitía extraer los datos de la app con `adb backup` (Hallazgo 4).
- **Qué se cambió:** `android:allowBackup="false"` y `android:dataExtractionRules="@xml/data_extraction_rules"`, que excluye todos los dominios del respaldo en la nube y de la transferencia entre dispositivos.
- **Cómo mejora la seguridad:** los datos privados de la app no pueden copiarse fuera del dispositivo. El hallazgo desapareció del informe.

## 3. Comunicación de red segura (solo HTTPS)

- **Problema que resolvía:** no había una política de red declarada, por lo que el tráfico sin cifrar no estaba bloqueado de forma explícita.
- **Qué se cambió:** se creó `res/xml/network_security_config.xml` con `cleartextTrafficPermitted="false"` y se referenció en el manifest con `android:networkSecurityConfig`.
- **Cómo mejora la seguridad:** cualquier intento de comunicación HTTP sin cifrar es bloqueado, lo que protege los datos en tránsito. MobSF lo registra como **Secure**: *"Base config is configured to disallow clear text traffic to all domains"*.

## 4. Ofuscación y reducción del código con R8

- **Problema que resolvía:** el build release tenía `optimization.enable = false`, dejando el código legible y sin reducir.
- **Qué se cambió:** `optimization { enable = true }` en el build type `release`.
- **Cómo mejora la seguridad:** dificulta la ingeniería inversa (nombres de clases y métodos ofuscados, código no usado eliminado) y reduce la superficie de ataque. El tamaño del APK bajó de 14,95 MB a 1,86 MB (parte de esa reducción se debe también a que ya no es un APK debug).

## 5. Compilación release firmada con un certificado propio

- **Problema que resolvía:** el APK estaba firmado con el certificado de depuración y con `debuggable=true` (Hallazgos 2 y 3).
- **Qué se cambió:** se generó un keystore propio (almacenado fuera del repositorio, y excluido con `*.jks` en `.gitignore`) y se compiló el APK en variante `release`.
- **Cómo mejora la seguridad:** el APK ya no es depurable ni usa una firma genérica que cualquiera posee. Ambos hallazgos de severidad alta desaparecieron.

## 6. Cifrado de datos sensibles (evaluación)

La app **no almacena datos sensibles** de forma persistente: no usa base de datos, preferencias ni archivos, y la foto de la cámara solo se muestra en memoria. Por eso no se implementó cifrado en reposo. Si en el futuro se guardaran marcadores o fotos, se usarían `EncryptedSharedPreferences` / `EncryptedFile` de Jetpack Security (ver plan de acción).

## Resumen

| # | Best Practice | Hallazgo asociado | Evidencia |
|---|---|---|---|
| 1 | API Key fuera del código | 1 | Secretos: 2 → 0 |
| 2 | Respaldos deshabilitados | 4 | Hallazgo eliminado |
| 3 | Solo HTTPS | Red | Network Security: Secure |
| 4 | R8 activado | Código legible | Código ofuscado (`defpackage/*`) |
| 5 | Release con certificado propio | 2 y 3 | Hallazgos High: 2 → 0 |
