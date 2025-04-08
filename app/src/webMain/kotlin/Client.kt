import iuo.zmua.app.apiClient
import iuo.zmua.app.koin.setUp
import iuo.zmua.kit.config.RSocketConfig
import iuo.zmua.kit.config.configLoad
import kotlinx.browser.document
import kotlinx.coroutines.*
import org.kodein.di.DI
import org.kodein.di.singleton
import react.create
import react.dom.client.createRoot

val AppScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

fun main() {
    AppScope.launch {
        setUp() // 设置 koin
    }
    val container = document.createElement("div")
    document.body!!.appendChild(container)

    val welcome = Welcome.create {
        name = "Kotlin/JS"
    }
    createRoot(container).render(welcome)
}