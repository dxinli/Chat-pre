package iuo.zmua.kit.config

import io.rsocket.kotlin.core.WellKnownMimeType
import kotlinx.serialization.Serializable

@Serializable
enum class TransportType { TCP, WS }

@Serializable
data class Config(
    val rSocket: RSocketConfig = RSocketConfig(),
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