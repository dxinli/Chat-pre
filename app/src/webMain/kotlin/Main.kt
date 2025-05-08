import component.App
import csstype.sx
import router.Router
import theme.ThemeModule
import iuo.zmua.app.koin.setUp
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import react.FC
import react.Props
import react.create
import react.createElement
import react.dom.client.createRoot
import react.router.RouterProvider
import web.cssom.pct
import web.html.HTML.div
import web.dom.document

val AppScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

fun main() {
//    val setUpCompleted = CompletableDeferred<Unit>()
//    AppScope.launch {
//        setUp() // 设置 koin
//        setUpCompleted.complete(Unit) // 设置完成后发送信号
//    }
    val root = document.createElement(div)
        .apply { sx { height = 100.pct } }
        .also { document.body.appendChild(it) }

    AppScope.launch {
        println("wait setup koin module completed")
//        setUpCompleted.await() // 等待设置完成
        println("creat react app")
        createRoot(root).render(App.create())
    }
}

//private val App = FC<Props> {
//    ThemeModule {
//        RouterProvider {
//            router = Router
//        }
//    }
//}
