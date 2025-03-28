package iuo.zmua.kit

import io.rsocket.kotlin.*
import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.metadata.*
import io.rsocket.kotlin.payload.*
import kotlinx.io.*
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.*
import kotlin.jvm.*

//just stub
@ExperimentalSerializationApi
private val ConfiguredProtoBuf = ProtoBuf

@ExperimentalSerializationApi
private val ConfiguredJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

class Serialization(val mimeType: WellKnownMimeType) {

    @ExperimentalSerializationApi
    val protoBuf = ConfiguredProtoBuf

    @ExperimentalSerializationApi
    val json = ConfiguredJson

    @ExperimentalSerializationApi
    inline fun <reified T> decodeFromPayload(payload: Payload): T = when (mimeType) {
        WellKnownMimeType.ApplicationProtoBuf -> protoBuf.decodeFromByteArray(payload.data.readByteArray())
        WellKnownMimeType.ApplicationJson -> json.decodeFromString(payload.data.readString())
        else -> error("Invalid mimeType")
    }

    @ExperimentalSerializationApi
    @OptIn(ExperimentalMetadataApi::class)
    inline fun <reified T> encodeToPayload(route: String, value: T): Payload = when (mimeType) {
        WellKnownMimeType.ApplicationProtoBuf -> buildPayload {
            data(protoBuf.encodeToByteArray(value))
            metadata(RoutingMetadata(route))
        }
        WellKnownMimeType.ApplicationJson -> buildPayload {
            data(json.encodeToString(value).encodeToByteArray())
            metadata(RoutingMetadata(route))
        }
        else -> error("Invalid mimeType")
    }

    @ExperimentalSerializationApi
    inline fun <reified T> encodeToPayload(value: T): Payload = when (mimeType) {
        WellKnownMimeType.ApplicationProtoBuf -> buildPayload {
            data(protoBuf.encodeToByteArray(value))
        }
        WellKnownMimeType.ApplicationJson -> buildPayload {
            data(json.encodeToString(value).encodeToByteArray())
        }
        else -> error("Invalid mimeType")
    }

    @ExperimentalSerializationApi
    inline fun <reified I, reified O> decoding(payload: Payload, block: (I) -> O): Payload =
        encodeToPayload(decodeFromPayload<I>(payload).let(block))

    @ExperimentalSerializationApi
    @JvmName("decoding2")
    inline fun <reified I> decoding(payload: Payload, block: (I) -> Unit): Payload {
        decodeFromPayload<I>(payload).let(block)
        return Payload.Empty
    }
}

@OptIn(ExperimentalMetadataApi::class)
fun Payload(route: String, data: Buffer = Buffer()): Payload = buildPayload {
    data(Buffer())
    compositeMetadata {
        add(RoutingMetadata("api.v1.user.getMe"))
    }
}

@OptIn(ExperimentalMetadataApi::class)
fun Payload.route(): String = metadata?.read(RoutingMetadata)?.tags?.first() ?: error("No route provided")