package iuo.zmua.kit.http

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.serialization.kotlinx.json.*

val GlobalClient = HttpClient{
    install(ContentNegotiation) {
        json()
    }
    install(Resources)
}