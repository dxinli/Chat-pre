package iuo.zmua.util

import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.metadata.RoutingMetadata
import io.rsocket.kotlin.metadata.compositeMetadata
import io.rsocket.kotlin.metadata.metadata
import io.rsocket.kotlin.metadata.read
import io.rsocket.kotlin.payload.Payload
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import iuo.zmua.kit.encoding.ConfiguredJson
import iuo.zmua.kit.encoding.ConfiguredProtoBuf
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.jvm.JvmName

sealed interface Serializer

@ExperimentalSerializationApi
@JvmName("defDecoding")
inline fun <reified I> Serializer.decoding(payload: Payload, block: (I) -> Unit): Payload {
    decodeFromPayload<I>(payload).let(block)
    return Payload.Empty
}

@ExperimentalSerializationApi
inline fun <reified I, reified O> Serializer.decoding(payload: Payload, block: (I) -> O): Payload =
    encodeToPayload(decodeFromPayload<I>(payload).let(block))

@ExperimentalSerializationApi
inline fun <reified T> Serializer.encodeToPayload(value: T): Payload = throw UnsupportedOperationException("Not implemented")

@ExperimentalSerializationApi
inline fun <reified T> Serializer.encodeToPayload(route: String, value: T): Payload = throw UnsupportedOperationException("Not implemented")

@ExperimentalSerializationApi
inline fun <reified T> Serializer.decodeFromPayload(payload: Payload): T = throw UnsupportedOperationException("Not implemented")

class JsonSerializer(
    private val json: Json,
): Serializer,SerialFormat by json

@OptIn(ExperimentalSerializationApi::class)
class ProtoBufSerializer @OptIn(ExperimentalSerializationApi::class) constructor(
    private val protoBuf: ProtoBuf,
): Serializer,SerialFormat by protoBuf

@OptIn(ExperimentalMetadataApi::class)
fun Payload(route: String, data: Buffer = Buffer()): Payload = buildPayload {
    data(Buffer())
    compositeMetadata {
        add(RoutingMetadata("api.v1.user.getMe"))
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun Serializer(wellKnownMimeType: WellKnownMimeType): Serializer = when(wellKnownMimeType){
    WellKnownMimeType.ApplicationJson -> JsonSerializer(ConfiguredJson)
    WellKnownMimeType.ApplicationProtoBuf -> ProtoBufSerializer(ConfiguredProtoBuf)
    else -> throw IllegalArgumentException("Unsupported mimeType: $wellKnownMimeType")
}

@OptIn(ExperimentalMetadataApi::class)
fun Payload.route(): String = metadata?.read(RoutingMetadata)?.tags?.first() ?: error("No route provided")

@ExperimentalSerializationApi
inline fun <reified T> ProtoBuf.decodeFromPayload(payload: Payload): T = decodeFromByteArray(payload.data.readByteArray())

@ExperimentalSerializationApi
inline fun <reified T> Json.decodeFromPayload(payload: Payload): T = decodeFromString(payload.data.readString())

@ExperimentalSerializationApi
@OptIn(ExperimentalMetadataApi::class)
inline fun <reified T> ProtoBuf.encodeToPayload(route: String, value: T): Payload = buildPayload {
    data(encodeToByteArray(value))
    metadata(RoutingMetadata(route))
}

@ExperimentalSerializationApi
@OptIn(ExperimentalMetadataApi::class)
inline fun <reified T> Json.encodeToPayload(route: String, value: T): Payload = buildPayload {
    data(encodeToString(value).encodeToByteArray())
    metadata(RoutingMetadata(route))
}

@ExperimentalSerializationApi
inline fun <reified T> ProtoBuf.encodeToPayload(value: T): Payload = buildPayload {
    data(encodeToByteArray(value))
}

@ExperimentalSerializationApi
inline fun <reified T> Json.encodeToPayload(value: T): Payload = buildPayload {
    data(encodeToString(value).encodeToByteArray())
}

@ExperimentalSerializationApi
inline fun <reified I, reified O> ProtoBuf.decoding(payload: Payload, block: (I) -> O): Payload =
    encodeToPayload(decodeFromPayload<I>(payload).let(block))

@ExperimentalSerializationApi
inline fun <reified I, reified O> Json.decoding(payload: Payload, block: (I) -> O): Payload =
    encodeToPayload(decodeFromPayload<I>(payload).let(block))

@ExperimentalSerializationApi
@JvmName("jsonDecoding")
inline fun <reified I> Json.decoding(payload: Payload, block: (I) -> Unit): Payload {
    decodeFromPayload<I>(payload).let(block)
    return Payload.Empty
}

@ExperimentalSerializationApi
@JvmName("protobufDecoding")
inline fun <reified I> ProtoBuf.decoding(payload: Payload, block: (I) -> Unit): Payload {
    decodeFromPayload<I>(payload).let(block)
    return Payload.Empty
}