package iuo.zmua.app.koin

import iuo.zmua.app.apiClient
import iuo.zmua.app.message.UserClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

suspend fun setUp(vararg modules: Module) {
    val apiClient = apiClient()

    val koinApp = startKoin {
        printLogger() // 添加 Koin 日志打印
        modules(module {
            single { apiClient }
            single { UserClient(get()) }
        })
    }
}