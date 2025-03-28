package iuo.zmua.kit.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import iuo.zmua.kit.config.configLoad

suspend fun main() {
    val rSocketConfig = configLoad()
    println(rSocketConfig)
}

actual inline fun <reified T> Yaml.decodeYaml(yamlString: String): T {
    val mapper = ObjectMapper(YAMLFactory())
    return mapper.readValue(yamlString, T::class.java)
}