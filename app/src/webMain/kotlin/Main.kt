import component.App
import iuo.zmua.app.koin.setUp
import kotlinx.browser.document
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import react.create
import react.dom.client.createRoot

val AppScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

fun main() {
    val setUpCompleted = Channel<Unit>(Channel.RENDEZVOUS)
    AppScope.launch {
        setUp() // 设置 koin
        setUpCompleted.send(Unit) // 设置完成后发送信号
    }
    AppScope.launch {
        val container = document.getElementById("root") ?: error("Couldn't find root container!")
        document.body!!.appendChild(container)
        println("wait setup koin module completed")
        setUpCompleted.receive() // 等待设置完成
        println("creat react app")
        val welcome = Welcome.create {
            name = "Kotlin/JS"
        }
        createRoot(container).render(App.create())
    }
}