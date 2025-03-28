package iuo.zmua.kit.config

import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.statement.*
import io.ktor.resources.*
import iuo.zmua.kit.ktor.httpClient
import iuo.zmua.kit.utils.Yaml
import iuo.zmua.kit.utils.decodeYaml

@Resource("/v1")
class ConsulApi() {
    @Resource("kv")
    class KV(val parent: ConsulApi = ConsulApi()){
        @Resource("{key}")
        class Key(val parent: KV = KV(),val key: String,val raw: Boolean? = false)
    }
}

suspend fun configLoad():Config {
    println("consul config load")
    val res : HttpResponse  = httpClient.get(ConsulApi.KV.Key(key="rSocket", raw = true)){
        url {
            host = "localhost"
            port = 8500
        }
    }
    if (res.status.value in 200..299) {
        val configStr = res.body<String>()
        val config:Config = Yaml.decodeYaml(configStr)
        return config
    }
    throw Exception("consul config load error")
}
