package iuo.zmua.kit.config

import com.charleskorn.kaml.Yaml
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.rsocket.kotlin.core.WellKnownMimeType
import iuo.zmua.kit.http.httpClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

@Serializable
enum class TransportType { TCP, WS }

@Serializable
data class Config(
    val rsocket: RSocketConfig = RSocketConfig(),
)

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

suspend fun configLoad():Config {
    println("consul config load")
    val res = httpClient.post(EtcdApi.KV.Range()){
        url{
           host = "localhost"
           port = 2380
        }
        contentType(ContentType.Application.Json)
        setBody(mapOf("key" to "rSocket"))
    }
    if (res.status.value in 200..299) {
        val configStr = res.body<String>()
        println(configStr)
        return Config()
    }
    throw Exception("etcd config load error")
}

suspend fun main() {
    configLoad()
}