package iuo.zmua.app.koin

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

val config = module {}

val client = module {}

fun setUp(vararg modules: Module) = startKoin {
    // declare used modules
    modules(config,*modules)
}