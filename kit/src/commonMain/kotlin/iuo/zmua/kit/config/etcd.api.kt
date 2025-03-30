package iuo.zmua.kit.config

import io.ktor.resources.*

@Resource("/v3")
class EtcdApi() {
    @Resource("/kv")
    class KV(val parent: EtcdApi = EtcdApi()){
        @Resource("/range")
        class Range(val parent: KV = KV())
    }
}
