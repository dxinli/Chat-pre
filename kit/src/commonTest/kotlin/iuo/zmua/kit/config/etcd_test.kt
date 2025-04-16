package iuo.zmua.kit.config

import iuo.zmua.kit.encoding.ConfiguredYaml
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import kotlin.test.Test

class EtcdTest{

    @Test
    fun `test etcd watch`() = runTest {
        // 监听配置变化
        etcdClientInstance.await().watch("rSocket") { newValue ->
            println("配置更新: $newValue")
            // 触发重新加载配置逻辑
        }
    }

    @Test
    fun `test etcd watch WebSocket`() = runTest {
        // 监听配置变化
        etcdClientInstance.await() .watchWebSocket("rSocket") { newValue ->
            println("配置更新: $newValue")
            // 触发重新加载配置逻辑
        }
    }

    @Test
    fun `test etcd load`() = runTest {
        // 加载配置
        val config:RSocketConfig = ConfiguredYaml.decodeFromSource(etcdClientInstance.await().configLoad("rSocket"))
        println("配置加载: $config")
    }

    @Test
    fun `test etcd auth`() = runTest {
        // 测试 token manager
        TokenManager.init(etcdClientInstance.await() ,"root","sakura")
        val token = TokenManager.getToken()
        println("token: $token")
    }
}