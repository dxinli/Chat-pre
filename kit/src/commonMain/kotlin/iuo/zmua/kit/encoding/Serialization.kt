package iuo.zmua.kit.encoding

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.*

//just stub
@ExperimentalSerializationApi
val ConfiguredProtoBuf = ProtoBuf

@ExperimentalSerializationApi
val ConfiguredJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

val ConfigYaml = Yaml.default