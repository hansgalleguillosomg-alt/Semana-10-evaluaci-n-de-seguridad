# hansgpsmapas

## Descripción

Aplicación Android en Java que muestra un mapa de Google Maps con la ubicación del usuario, permite agregar marcadores y tomar una foto con la cámara. El proyecto fue sometido a un análisis de seguridad con MobSF y se le aplicaron mejoras para proteger contra vulnerabilidades comunes (Actividad 2.5: Manejo de elementos de seguridad, Programación Android).

## Vulnerabilidades identificadas

- API Key de Google Maps expuesta en el código
- Aplicación depurable (`android:debuggable=true`)
- Firma con certificado de depuración
- Copias de seguridad habilitadas (`android:allowBackup=true`)
- Receiver exportado innecesario (`ProfileInstallReceiver`)

## Mejoras implementadas

- API Key fuera del código (`local.properties`)
- Respaldos de datos deshabilitados
- Comunicación segura: solo HTTPS (Network Security Config)
- Ofuscación y reducción del código con R8
- Compilación release firmada con certificado propio
- Eliminación del receiver exportado
- Permisos con mínimo privilegio (ubicación precisa o aproximada)
- Validación y límite de entradas (coordenadas y máximo de marcadores)

**Resultado en MobSF:** 45/100 (Grade B) → 85/100 (Grade A). Hallazgos High: 2 → 0.

## Documentación

- [Vulnerabilidades](vulnerabilities.md)
- [Best Practices](best_practices.md)
- [Security Tips](security_tips.md)
- [Security Improvement Program](security_improvement_program.md)

## Cómo ejecutar la aplicación de forma segura

1. Clonar el repositorio.
2. Importar el proyecto en Android Studio.
3. Obtener una API Key de Google Maps (Maps SDK for Android) y **restringirla** por package name y SHA-1 en Google Cloud Console.
4. Agregar la clave en el archivo `local.properties` (raíz del proyecto, no se sube a Git):
   ```
   MAPS_API_KEY=tu_api_key
   ```
   Hay un ejemplo en `local.properties.example`.
5. Ejecutar la aplicación en un dispositivo o emulador y aceptar el permiso de ubicación.
6. Para generar la versión release: *Build → Generate Signed App Bundle / APK*, con un keystore propio guardado **fuera** del repositorio.

> No subir nunca `local.properties`, archivos `.jks`/`.keystore` ni APK compilados al repositorio.

## Reporte de Vulnerabilidades

El reporte detallado de las pruebas de vulnerabilidad se encuentra en el archivo `vulnerability_report.pdf` e incluye:

- Análisis estático con MobSF de la app original (debug).
- Análisis dinámico con MobSF de la app original (debug): tráfico de red solo hacia servidores de Google, sin trackers, sin bases de datos y sin actividades exportadas. Las pruebas TLS/Cleartext de MobSF figuran como *Not Tested*.
- Análisis estático con MobSF de la versión corregida (release).

## Preguntas de cierre

**1. ¿Qué nuevos conocimientos y habilidades hemos adquirido en la protección de aplicaciones Android contra amenazas de seguridad?**

Aprendimos a analizar un APK con MobSF, tanto de forma estática como dinámica, y a leer sus hallazgos para decidir cuáles importan de verdad. También aprendimos la diferencia entre una compilación debug y una release, y por qué una app debug no se debería publicar. En la práctica aplicamos medidas concretas: sacar la API Key del código, desactivar los respaldos, bloquear el tráfico sin cifrar, ofuscar el código con R8 y firmar la app con un certificado propio. Otra cosa que vimos es que algunos hallazgos de las herramientas son falsos positivos que vienen del código de librerías, y que la API Key sigue dentro del APK aunque se saque del código, por eso hay que restringirla en Google Cloud.

**2. ¿Cómo podríamos utilizar los conocimientos adquiridos para mejorar la seguridad en aplicaciones móviles en diferentes entornos, como aplicaciones financieras, de salud o de comercio electrónico?**

En una app financiera o de comercio electrónico escanearíamos cada versión antes de publicarla, usaríamos solo conexiones HTTPS con certificate pinning, y pediríamos autenticación fuerte, por ejemplo con biometría. En una app de salud cuidaríamos especialmente los datos personales: cifrarlos en reposo, no guardarlos en los logs, pedir solo los permisos que hacen falta y respetar la normativa de protección de datos. En todos los casos mantendríamos las claves y secretos fuera del repositorio y haríamos revisiones periódicas de seguridad. En este proyecto no aplicamos el cifrado en reposo porque la app no guarda datos, pero ahora sabemos cómo hacerlo si llega a ser necesario.

**3. ¿Logramos implementar las mejores prácticas y los consejos de seguridad de manera efectiva? ¿Qué desafíos encontramos en el proceso y cómo los superamos?**

Sí, implementamos las mejoras principales y MobSF confirmó el resultado: el puntaje subió de 45 a 85 y los hallazgos graves pasaron de 2 a 0. Tuvimos varios desafíos. Al comienzo generamos un APK debug en vez del release firmado, y lo resolvimos repitiendo el proceso con el keystore propio. Después, la versión debug instalada en el emulador chocaba con la release por tener firmas distintas, y lo solucionamos desinstalándola. El análisis dinámico no lo pudimos ejecutar en nuestros equipos, así que lo hizo un amigo en el suyo, y las pruebas TLS de MobSF salieron como "Not Tested". Lo que no pudimos resolver nosotros fue restringir la API Key, porque pertenece a otra cuenta de Google Cloud; quedó documentada como acción pendiente. Para avanzar usamos apoyo de inteligencia artificial, como se detalla en el registro de uso de IA.