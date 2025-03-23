package iuo.zmua.api.utils

import io.rsocket.kotlin.*
import io.rsocket.kotlin.metadata.*
import io.rsocket.kotlin.payload.*
import kotlinx.io.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.*
import kotlin.jvm.*

//just stub
@ExperimentalSerializationApi
val ConfiguredProtoBuf = ProtoBuf

@ExperimentalSerializationApi
val ConfiguredJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

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

//@ExperimentalSerializationApi
@OptIn(ExperimentalMetadataApi::class)
inline fun <reified T> Json.encodeToPayload(route: String, value: T): Payload = buildPayload {
    data(encodeToString(value).encodeToByteArray())
    metadata(RoutingMetadata(route))
}


@ExperimentalSerializationApi
inline fun <reified T> ProtoBuf.encodeToPayload(value: T): Payload = buildPayload {
    data(encodeToByteArray(value))
}

//@ExperimentalSerializationApi
inline fun <reified T> Json.encodeToPayload(value: T): Payload = buildPayload {
    data(encodeToString(value).encodeToByteArray())
}

@ExperimentalSerializationApi
inline fun <reified I, reified O> ProtoBuf.decoding(payload: Payload, block: (I) -> O): Payload =
    encodeToPayload(decodeFromPayload<I>(payload).let(block))

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified I, reified O> Json.decoding(payload: Payload, block: (I) -> O): Payload =
    encodeToPayload(decodeFromPayload<I>(payload).let(block))

@ExperimentalSerializationApi
@JvmName("decoding2")
inline fun <reified I> ProtoBuf.decoding(payload: Payload, block: (I) -> Unit): Payload {
    decodeFromPayload<I>(payload).let(block)
    return Payload.Empty
}

@ExperimentalSerializationApi
@JvmName("decoding2")
inline fun <reified I> Json.decoding(payload: Payload, block: (I) -> Unit): Payload {
    decodeFromPayload<I>(payload).let(block)
    return Payload.Empty
}

@OptIn(ExperimentalMetadataApi::class)
fun Payload(route: String, data: Buffer = Buffer()): Payload = buildPayload {
    data(data)
    metadata(RoutingMetadata(route))
}

@OptIn(ExperimentalMetadataApi::class)
fun Payload.route(): String = metadata?.read(RoutingMetadata)?.tags?.first() ?: error("No route provided")