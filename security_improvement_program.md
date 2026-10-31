# App Security Improvement Program

**Aplicación:** hansgpsmapas (`com.example.hansgpsmapas`)
**Responsable:** Diego Chihuiahuan

## 1. Objetivo

Evaluar y mejorar de forma continua la seguridad de la aplicación, detectando vulnerabilidades en cada versión antes de publicarla y verificando que las correcciones realmente reduzcan el riesgo.

## 2. Mejoras realizadas hasta ahora

| Área | Antes | Después |
|---|---|---|
| App Security Score (MobSF) | 45/100 (Grade B) | 85/100 (Grade A) |
| Hallazgos High | 2 | 0 |
| Secretos en el código | 2 | 0 |
| Componentes exportados | 1 | 0 |
| Compilación | Debug, firmada con certificado de depuración | Release, certificado propio, R8 activo |
| Respaldos de datos | Permitidos | Deshabilitados |
| Tráfico de red | Sin política declarada | Solo HTTPS (cleartext bloqueado) |

Detalle en [`vulnerabilities.md`](vulnerabilities.md), [`best_practices.md`](best_practices.md) y [`security_tips.md`](security_tips.md).

## 3. Proceso de revisión periódica

| Frecuencia | Actividad | Herramienta | Responsable |
|---|---|---|---|
| En cada release | Escaneo estático del APK release y comparación con la versión anterior | MobSF | Desarrollador |
| En cada release | Verificar que no haya secretos en el repositorio (`git grep`, revisión de `.gitignore`) | Git / revisión manual | Desarrollador |
| Mensual | Revisar actualizaciones de dependencias (Maps, Location, AndroidX) y vulnerabilidades conocidas | Android Studio / Gradle, SBOM de MobSF | Desarrollador |
| Trimestral | Análisis dinámico en emulador y pruebas del tráfico de red | MobSF (dinámico), OWASP ZAP | Desarrollador |
| Trimestral | Revisar permisos solicitados y justificar cada uno | Revisión del manifest | Desarrollador |
| Ante un incidente | Rotar claves expuestas y publicar una versión corregida | Google Cloud Console | Dueño de la cuenta de Google Cloud |

## 4. Métricas clave

| Métrica | Valor actual | Meta |
|---|---|---|
| App Security Score (MobSF) | 85/100 | ≥ 90/100 |
| Hallazgos High | 0 | 0 |
| Hallazgos Medium / Warning | 2 | ≤ 1 |
| Secretos en el repositorio | 0 | 0 |
| Componentes exportados innecesarios | 0 | 0 |
| Permisos peligrosos sin justificar | 0 (2 de ubicación, justificados) | 0 |
| API Key con restricciones en Google Cloud | No | Sí |
| Dependencias con vulnerabilidades conocidas | Por medir | 0 |

## 5. Plan de acción futuro

| Prioridad | Acción | Plazo estimado | Responsable |
|---|---|---|---|
| Alta | Restringir la API Key por package name + SHA-1 (debug y release) y limitarla a Maps SDK for Android | Noviembre 2026 | Dueño de la cuenta Google Cloud |
| Media | Repetir el análisis dinámico con MobSF sobre la versión release y ejecutar las pruebas TLS/Cleartext que quedaron como *Not Tested* | Noviembre 2026 | Desarrollador |
| Media | Configurar alerta de presupuesto/cuota en Google Cloud para detectar uso anómalo de la clave | Noviembre 2026 | Dueño de la cuenta Google Cloud |
| Media | Ejecutar un escaneo con OWASP ZAP o SonarQube y comparar con MobSF | Diciembre 2026 | Desarrollador |
| Media | Revisar y eliminar el uso de logs que expongan información de ubicación | Diciembre 2026 | Desarrollador |
| Baja | Evaluar *certificate pinning* si se agrega un backend propio | Primer semestre 2027 | Desarrollador |
| Baja | Usar `EncryptedSharedPreferences` / `EncryptedFile` si se guardan marcadores o fotos | Cuando se agregue persistencia | Desarrollador |
| Baja | Integrar Firebase App Check para validar que las solicitudes vienen de la app legítima | Primer semestre 2027 | Desarrollador |
| Baja | Automatizar el escaneo de seguridad en CI (GitHub Actions) | Primer semestre 2027 | Desarrollador |

## 6. Criterio de éxito

El programa se considera efectivo si cada versión liberada mantiene **0 hallazgos High**, **0 secretos en el repositorio** y un puntaje de MobSF **igual o mayor** al de la versión anterior.
