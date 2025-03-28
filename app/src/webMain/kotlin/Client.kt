import iuo.zmua.app.koin.client
import iuo.zmua.app.koin.setUp
import kotlinx.browser.document
import react.create
import react.dom.client.createRoot

fun main() {
    setUp(client) // 设置 koin
    val container = document.createElement("div")
    document.body!!.appendChild(container)

    val welcome = Welcome.create {
        name = "Kotlin/JS"
    }
    createRoot(container).render(welcome)
}