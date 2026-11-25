package com.cookly.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

/** Errores de red con su mensaje en español, listo para mostrar. */
enum class ApiError(val message: String) {
    NOT_FOUND("No encontramos este código en Open Food Facts."),
    NO_CONNECTION("Sin conexión a internet. Revisa tu red e intenta de nuevo."),
    TIMEOUT("La consulta tardó demasiado. Intenta de nuevo."),
    TOO_MANY_REQUESTS("Demasiadas búsquedas seguidas, espera un minuto."),
    SERVER("El servidor respondió con un error."),
    DECODING("No pudimos leer la respuesta del servidor."),
}

/** Resultado de una petición: o salió bien con un valor, o falló con un ApiError. */
sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}

/**
 * Módulo de RED: consume la API REST pública de Open Food Facts con Ktor.
 * Ktor es multiplataforma: este mismo código corre en Android y en Desktop.
 * (En el proyecto Android usábamos Retrofit; en iOS, URLSession.)
 */
class FoodApi(private val client: HttpClient = defaultClient()) {

    /** GET https://world.openfoodfacts.org/api/v2/product/{codigo}.json */
    suspend fun fetchProduct(barcode: String): ApiResult<OffProduct> {
        return try {
            val response = client.get("$BASE_URL/api/v2/product/$barcode.json") {
                parameter("fields", FIELDS)
                header(HttpHeaders.UserAgent, USER_AGENT)
            }
            when (response.status.value) {
                in 200..299 -> {
                    val body: OffResponse = response.body()
                    val product = body.product
                    if (body.status == 1 && product != null) ApiResult.Success(product)
                    else ApiResult.Failure(ApiError.NOT_FOUND)
                }
                404 -> ApiResult.Failure(ApiError.NOT_FOUND)
                429 -> ApiResult.Failure(ApiError.TOO_MANY_REQUESTS)
                else -> ApiResult.Failure(ApiError.SERVER)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpRequestTimeoutException) {
            ApiResult.Failure(ApiError.TIMEOUT)
        } catch (e: JsonConvertException) {
            ApiResult.Failure(ApiError.DECODING)
        } catch (e: Exception) {
            ApiResult.Failure(ApiError.NO_CONNECTION)
        }
    }

    companion object {
        private const val BASE_URL = "https://world.openfoodfacts.org"
        /** Open Food Facts pide que cada app se identifique con un User-Agent propio. */
        private const val USER_AGENT = "Cookly-KMP/1.0 (ronalvaldez@unicauca.edu.co, msolarteastaiza@unicauca.edu.co)"
        /** Solo pedimos los campos que usamos: respuesta más liviana. */
        private const val FIELDS = "code,product_name,brands,quantity,image_front_small_url,nutriscore_grade,nova_group,categories_tags,ingredients_text,allergens"

        fun defaultClient(): HttpClient = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true   // ignora campos que no usamos
                    coerceInputValues = true   // si llega null donde hay valor por defecto, usa el defecto
                    isLenient = true
                })
            }
            install(HttpTimeout) { requestTimeoutMillis = 20_000 }
        }
    }
}
