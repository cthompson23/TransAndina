package com.transandina.flotilla.data.foto

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/** El archivo elegido no es JPG ni PNG (Figma `49:161`: "Formatos JPG o PNG"). */
class FormatoFotoNoPermitido : Exception()

/** Foto lista para subir: JPEG reducido y una miniatura para mostrar en el formulario. */
class FotoPreparada(
    val nombre: String?,
    val jpeg: ByteArray,
    val miniatura: Bitmap
)

/**
 * Lee una foto elegida por el usuario, la reduce a [LADO_MAXIMO] px y la
 * guarda como JPEG. Así ninguna foto se acerca al tope de 5 MB del bucket
 * (migración 202609152200), aunque la cámara la haya tomado más grande.
 *
 * Trabaja con archivos: llamarla fuera del hilo principal.
 */
object PreparadorFoto {

    const val LADO_MAXIMO = 1600
    const val TOPE_BYTES = 5 * 1024 * 1024
    private const val CALIDAD_JPEG = 80
    private const val LADO_MINIATURA = 240
    private val TIPOS_PERMITIDOS = setOf("image/jpeg", "image/png")

    fun preparar(context: Context, uri: Uri): FotoPreparada {
        val resolver = context.contentResolver
        if (resolver.getType(uri) !in TIPOS_PERMITIDOS) throw FormatoFotoNoPermitido()

        val original = decodificarReducida(resolver, uri)
        // Un PNG con transparencia quedaría con fondo negro en JPEG.
        val opaca = if (original.hasAlpha()) sobreFondoBlanco(original) else original

        val jpeg = ByteArrayOutputStream().use { salida ->
            opaca.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
            salida.toByteArray()
        }
        check(jpeg.size <= TOPE_BYTES) { "La foto sigue siendo muy pesada" }

        return FotoPreparada(
            nombre = nombreVisible(resolver, uri),
            jpeg = jpeg,
            miniatura = escalar(opaca, LADO_MINIATURA)
        )
    }

    private fun decodificarReducida(resolver: ContentResolver, uri: Uri): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder respeta la orientación EXIF de la cámara.
            val fuente = ImageDecoder.createSource(resolver, uri)
            return ImageDecoder.decodeBitmap(fuente) { decodificador, info, _ ->
                val (ancho, alto) = medidasReducidas(info.size.width, info.size.height)
                decodificador.setTargetSize(ancho, alto)
                decodificador.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }

        // Android 8: se lee primero el tamaño para no cargar la foto completa.
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, limites) }
        var muestreo = 1
        while (max(limites.outWidth, limites.outHeight) / (muestreo * 2) >= LADO_MAXIMO) {
            muestreo *= 2
        }
        val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
        val muestreada = resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, opciones) }
            ?: throw FormatoFotoNoPermitido()
        return escalar(muestreada, LADO_MAXIMO)
    }

    private fun medidasReducidas(ancho: Int, alto: Int): Pair<Int, Int> {
        val lado = max(ancho, alto)
        if (lado <= LADO_MAXIMO) return ancho to alto
        val escala = LADO_MAXIMO.toFloat() / lado
        return (ancho * escala).roundToInt().coerceAtLeast(1) to (alto * escala).roundToInt().coerceAtLeast(1)
    }

    private fun escalar(bitmap: Bitmap, ladoMaximo: Int): Bitmap {
        val lado = max(bitmap.width, bitmap.height)
        if (lado <= ladoMaximo) return bitmap
        val escala = ladoMaximo.toFloat() / lado
        return bitmap.scale(
            (bitmap.width * escala).roundToInt().coerceAtLeast(1),
            (bitmap.height * escala).roundToInt().coerceAtLeast(1)
        )
    }

    private fun sobreFondoBlanco(bitmap: Bitmap): Bitmap {
        val resultado = createBitmap(bitmap.width, bitmap.height)
        Canvas(resultado).apply {
            drawColor(Color.WHITE)
            drawBitmap(bitmap, 0f, 0f, null)
        }
        return resultado
    }

    private fun nombreVisible(resolver: ContentResolver, uri: Uri): String? =
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
}
