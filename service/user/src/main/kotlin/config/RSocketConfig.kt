package iuo.zmua.user.config

import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.core.WellKnownMimeType
import io.rsocket.kotlin.metadata.CompositeMetadata
import kotlinx.io.Buffer
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.ResolvableType
import org.springframework.core.annotation.Order
import org.springframework.core.codec.AbstractDataBufferDecoder
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.protobuf.KotlinSerializationProtobufDecoder
import org.springframework.http.codec.protobuf.KotlinSerializationProtobufEncoder
import org.springframework.lang.Nullable
import org.springframework.messaging.rsocket.MetadataExtractorRegistry
import org.springframework.messaging.rsocket.metadataToExtract
import org.springframework.util.MimeType
import java.nio.ByteBuffer

@Configuration
class RSocketConfig {

    @OptIn(ExperimentalMetadataApi::class)
    @Bean
    @Order(HIGHEST_PRECEDENCE)
    fun rSocketStrategiesCustomizer(): RSocketStrategiesCustomizer {
        return RSocketStrategiesCustomizer { strategies ->
            strategies.encoders { encoders ->
                encoders.add(KotlinSerializationProtobufEncoder())
            }
                .decoders { decoders ->
                    decoders.add(KotlinSerializationProtobufDecoder())
                    decoders.add(object : AbstractDataBufferDecoder<CompositeMetadata>(MimeType.valueOf(
                        WellKnownMimeType.MessageRSocketCompositeMetadata.toString()
                    )) {

                        override fun canDecode(elementType: ResolvableType, mimeType: MimeType?): Boolean =
                            CompositeMetadata::class.java.isAssignableFrom(elementType.toClass()) &&
                                super.canDecode(elementType, mimeType)

                        override fun decode(
                            dataBuffer: DataBuffer,
                            targetType: ResolvableType,
                            @Nullable mimeType: MimeType?,
                            @Nullable hints: MutableMap<String, Any>?
                        ): CompositeMetadata {
                            CompositeMetadata.Reader.run {
                                val len: Int = dataBuffer.readableByteCount()
                                val byteBuffer = ByteBuffer.allocate(len)
                                dataBuffer.toByteBuffer(byteBuffer)
                                val byteArray = ByteArray(byteBuffer.remaining()).apply {
                                    byteBuffer.get(this)
                                }
                                DataBufferUtils.release(dataBuffer)
                                Buffer().apply {
                                    write(byteArray)
                                    return read()
                                }
                            }
                        }
                    })
                }
                .metadataExtractorRegistry { registry: MetadataExtractorRegistry ->
                    registry.metadataToExtract<CompositeMetadata>(MimeType.valueOf(WellKnownMimeType.MessageRSocketCompositeMetadata.toString())){
                            compositeMetadata: CompositeMetadata, mutableMap: MutableMap<String, Any> ->
                        compositeMetadata.entries.forEach { entry ->
                            when (entry.mimeType) {
                                WellKnownMimeType.MessageRSocketRouting -> {
                                    mutableMap["route"] = entry.content.readByte().toString()
                                }
                                else -> {
                                    mutableMap[entry.mimeType.toString()] = entry.content.readByte().toString()
                                }
                            }
                        }
                    }
                }
        }
    }
}