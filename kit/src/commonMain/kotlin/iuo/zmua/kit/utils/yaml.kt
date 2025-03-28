package iuo.zmua.kit.utils

object Yaml

expect inline fun <reified T> Yaml.decodeYaml(yamlString: String): T