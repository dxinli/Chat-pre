package iuo.zmua.kit.config

import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.rsocket.kotlin.core.WellKnownMimeType
import iuo.zmua.kit.encoding.ConfiguredYaml
import iuo.zmua.kit.http.httpClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.Buffer
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
enum class TransportType { TCP, WS }

@Serializable
data class RSocketConfig(
    val transportType: TransportType = TransportType.WS,
    val target: TransportTargetConfig = TransportTargetConfig(),
    val connector: RSocketConnectorConfig = RSocketConnectorConfig(),
)

@Serializable
data class TransportTargetConfig(
    val host: String = "localhost",
    val port: Int = 8080,
    val path: String = "/rsocket",
)

@Serializable
data class RSocketConnectorConfig(
    val dataMimeType: WellKnownMimeType = WellKnownMimeType.ApplicationProtoBuf,
    val metadataMimeType: WellKnownMimeType = WellKnownMimeType.MessageRSocketCompositeMetadata
)

@OptIn(ExperimentalEncodingApi::class)
suspend inline fun <reified T> configLoad(keyStr: String):T? {
    println("etcd config load")
    val key = Base64.encode(keyStr.encodeToByteArray())
    val res = httpClient.post(EtcdApi.KV.Range()){
        url{
           host = "localhost"
           port = 2379
        }
        contentType(ContentType.Application.Json)
        setBody(mapOf("key" to key))
    }
    if (res.status.value in 200..299) {
        val config = res.body<JsonObject>()
        config["kvs"]?.let { jsonElement ->
            val value = jsonElement.jsonArray[0].jsonObject["value"]?.jsonPrimitive?.content
            value?.let {
                val base64 = Base64.decode(it)
                return ConfiguredYaml.decodeFromSource(Buffer().write(base64))
            }
        }
        return null
    }
    throw Exception("etcd config load error")
}
