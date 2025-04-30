import csstype.sx
import io.github.aerialist7.router.Router
import io.github.aerialist7.theme.ThemeModule
import iuo.zmua.app.koin.setUp
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.router.RouterProvider
import web.cssom.pct
import web.html.HTML

val AppScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

fun main() {
    val setUpCompleted = Channel<Unit>(Channel.RENDEZVOUS)
    AppScope.launch {
        setUp() // 设置 koin
        setUpCompleted.send(Unit) // 设置完成后发送信号
    }
    AppScope.launch {
        val root = web.dom.document.createElement(HTML.div)
            .apply { sx { height = 100.pct } }
            .also { web.dom.document.body.appendChild(it) }
        println("wait setup koin module completed")
        setUpCompleted.receive() // 等待设置完成
        println("creat react app")
        createRoot(root).render(App.create())
    }
}

private val App = FC<Props> {
    ThemeModule {
        RouterProvider {
            router = Router
        }
    }
}