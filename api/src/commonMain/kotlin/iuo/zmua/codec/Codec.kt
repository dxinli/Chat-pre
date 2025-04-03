package iuo.zmua.codec

import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.payload.Payload
import iuo.zmua.kit.encoding.ConfiguredJson
import iuo.zmua.kit.encoding.ConfiguredProtoBuf
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.jvm.JvmName

class Codec (val wellKnownMimeType: WellKnownMimeType) {

    @ExperimentalSerializationApi
    inline fun <reified T> decodeFromPayload(payload: Payload): T = when(wellKnownMimeType){
        WellKnownMimeType.ApplicationJson -> ConfiguredJson.decodeFromPayload(payload)
        WellKnownMimeType.ApplicationProtoBuf -> ConfiguredProtoBuf.decodeFromPayload(payload)
        else -> throw IllegalArgumentException("Unsupported mimeType: $wellKnownMimeType")
    }

    @ExperimentalSerializationApi
    inline fun <reified T> encodeToPayload(route: String, value: T): Payload = when(wellKnownMimeType){
        WellKnownMimeType.ApplicationJson -> ConfiguredJson.encodeToPayload(route, value)
        WellKnownMimeType.ApplicationProtoBuf -> ConfiguredProtoBuf.encodeToPayload(route, value)
        else -> throw IllegalArgumentException("Unsupported mimeType: $wellKnownMimeType")
    }

    @ExperimentalSerializationApi
    inline fun <reified T> encodeToPayload(value: T): Payload = when(wellKnownMimeType){
        WellKnownMimeType.ApplicationJson -> ConfiguredJson.encodeToPayload(value)
        WellKnownMimeType.ApplicationProtoBuf -> ConfiguredProtoBuf.encodeToPayload(value)
        else -> throw IllegalArgumentException("Unsupported mimeType: $wellKnownMimeType")
    }

    @ExperimentalSerializationApi
    inline fun <reified I> decoding(payload: Payload, block: (I) -> Unit): Payload {
        decodeFromPayload<I>(payload).let(block)
        return Payload.Empty
    }

    @ExperimentalSerializationApi
    @JvmName("decoding2")
    inline fun <reified I, reified O> decoding(payload: Payload, block: (I) -> O): Payload =
        encodeToPayload(decodeFromPayload<I>(payload).let(block))

}