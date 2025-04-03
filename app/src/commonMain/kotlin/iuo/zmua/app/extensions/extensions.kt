package iuo.zmua.app.extensions

import iuo.zmua.app.ApiClient
import iuo.zmua.codec.Payload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
suspend inline fun <reified O> ApiClient.requestResponse(route: String, vararg data: Any): O =
    codec.decodeFromPayload<O>(
        rSocket.requestResponse(
            data.takeIf { it.isNotEmpty() }?.let {
                codec.encodeToPayload(route, it)
            } ?: Payload(route = route)
        )
    )

@OptIn(ExperimentalSerializationApi::class)
suspend fun ApiClient.fireAndForget(route: String, vararg data: Any) = rSocket.fireAndForget(
    data.takeIf { it.isNotEmpty() }?.let {
        codec.encodeToPayload(route, it)
    }?: Payload(route = route)
)

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified O> ApiClient.requestStream(route: String, vararg data: Any) : Flow<O> = rSocket.requestStream(
    data.takeIf { it.isNotEmpty() }?.let {
        codec.encodeToPayload(route, it)
    } ?: Payload(route = route)
).map {
    codec.decodeFromPayload(it)
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified O> ApiClient.requestChannel(route: String, vararg initData: Any, data: Flow<Any>) : Flow<O> = rSocket.requestChannel(
    initData.takeIf { it.isNotEmpty() }?.let {
        codec.encodeToPayload(route, it)
    } ?: Payload(route = route)
    ,data.map {
        codec.encodeToPayload(it)
    }
).map {
    codec.decodeFromPayload(it)
}