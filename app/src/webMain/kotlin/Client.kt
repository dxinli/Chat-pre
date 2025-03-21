import iuo.zmua.client.Servers
import iuo.zmua.client.runClient
import kotlinx.browser.document
import react.create
import react.dom.client.createRoot

suspend fun main() {
    runClient(Servers.ALL, "Kolya", "JS")
    val container = document.createElement("div")
    document.body!!.appendChild(container)

    val welcome = Welcome.create {
        name = "Kotlin/JS"
    }
    createRoot(container).render(welcome)
}