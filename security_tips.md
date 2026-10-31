# Security Tips implementados

**Aplicación:** hansgpsmapas (`com.example.hansgpsmapas`)
**Autor:** Antonio Villalobos

## 1. Eliminar componentes exportados innecesarios

- **Problema:** el receiver `androidx.profileinstaller.ProfileInstallReceiver` estaba exportado y accesible a otras apps (Hallazgo 5).
- **Implementación:** en el manifest se agregó `tools:node="remove"` para ese receiver.

  ```xml
  <receiver android:name="androidx.profileinstaller.ProfileInstallReceiver" tools:node="remove" />
  ```
- **Mejora:** menor superficie de ataque. MobSF pasó de 1 receiver exportado a 0 (*Exported Receivers: 0*).

## 2. Permisos con mínimo privilegio

- **Problema:** `onRequestPermissionsResult` solo revisaba `grantResults[0]`. En Android 12+ el usuario puede conceder solo la ubicación aproximada, y la app lo trataba como un rechazo.
- **Implementación:** el método `hasLocationPermission()` acepta ubicación precisa **o** aproximada, y la respuesta del usuario se evalúa revisando todos los resultados. La app no usa la ubicación ni el mapa si el permiso no fue concedido.
- **Mejora:** respeta la decisión del usuario sobre cuánta precisión entregar y evita accesos a la ubicación sin autorización. La app declara solo los permisos que necesita (ubicación, red e internet).

## 3. Validación y límite de entradas del usuario

- **Problema:** los puntos del mapa se creaban sin validar coordenadas ni cantidad.
- **Implementación:** `isValidLatLng()` verifica que la latitud esté entre -90 y 90 y la longitud entre -180 y 180, y se limita a `MAX_MARKERS = 25` marcadores.
- **Mejora:** evita datos inválidos y el crecimiento descontrolado de objetos en memoria (riesgo de errores o denegación de servicio local).

## 4. Protección contra ataques de red (MITM)

- **Implementación:** la `network_security_config.xml` bloquea el tráfico sin cifrar y define como únicos anclajes de confianza los certificados del sistema (`<certificates src="system" />`), sin aceptar certificados instalados por el usuario.
- **Mejora:** un atacante en la red no puede interceptar comunicaciones HTTP ni usar una CA instalada manualmente. MobSF muestra *Secure* para el bloqueo de tráfico en claro.
- **Pendiente:** MobSF mantiene una advertencia por confiar en los certificados del sistema. El *certificate pinning* se evaluará cuando la app use un backend propio (ver `security_improvement_program.md`).

## 5. Restricción de la API Key en Google Cloud (recomendado, pendiente)

- **Descripción:** restringir la clave por aplicación Android (package name `com.example.hansgpsmapas` + huella SHA-1) y limitarla a *Maps SDK for Android*.
- **Estado:** **pendiente**. La clave pertenece a la cuenta de un tercero, que debe aplicar la restricción. No se declara como implementada.
- **Mejora esperada:** aunque alguien extraiga la clave del APK, no podría usarla desde otra aplicación.

## Tips evaluados que no aplican

- **Inyección SQL:** la app no usa bases de datos (ni SQLite ni Room), por lo que no existe superficie de ataque. Si se agrega persistencia, se usarán consultas parametrizadas.
- **Autenticación y autorización:** la app no tiene login, usuarios ni backend. Si se agrega, se usará un proveedor estándar (Firebase Auth / OAuth 2.0) y tokens guardados con `EncryptedSharedPreferences`.

## Resumen

| # | Security Tip | Estado | Evidencia |
|---|---|---|---|
| 1 | Eliminar receiver exportado | Implementado | Exported Receivers: 1 → 0 |
| 2 | Permisos con mínimo privilegio | Implementado | Código en `MainActivity.java` |
| 3 | Validación de entradas | Implementado | Código en `MainActivity.java` |
| 4 | Protección MITM | Implementado | Network Security: Secure |
| 5 | Restricción de API Key | Pendiente | Requiere acceso a Google Cloud |
