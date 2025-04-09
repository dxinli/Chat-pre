package iuo.zmua.app.koin

import iuo.zmua.app.apiClient
import iuo.zmua.app.message.UserClient
import iuo.zmua.kit.config.RSocketConfig
import iuo.zmua.kit.config.configLoad
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

@OptIn(KoinInternalApi::class)
suspend fun setUp(vararg modules: Module) {
    val rSocketConfig:RSocketConfig = configLoad("rSocket")
    println(rSocketConfig)
    val apiClient = apiClient(rSocketConfig)
    val koinApp = startKoin {
        printLogger() // 添加 Koin 日志打印
        modules(module {
            single { apiClient }
            single { UserClient(get()) }
        })
    }
    val koin = koinApp.koin
    koin.instanceRegistry.instances.values.forEach {
        println("Registered component: ${it.beanDefinition.primaryType}")
    }
    println("koin module set up completed")
}