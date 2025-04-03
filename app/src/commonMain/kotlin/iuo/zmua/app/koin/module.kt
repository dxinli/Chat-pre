package iuo.zmua.app.koin

import iuo.zmua.app.apiClient
import iuo.zmua.app.message.UserClient
import iuo.zmua.kit.config.RSocketConfig
import iuo.zmua.kit.config.configLoad
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

suspend fun setUp(vararg modules: Module) {
    val rSocketConfig = configLoad("rSocket")?:RSocketConfig()
    val apiClient = apiClient(rSocketConfig)
    startKoin {
        module {
            single { apiClient }
            single { UserClient(get()) }
        }
    }
}