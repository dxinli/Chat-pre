package iuo.zmua.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import java.nio.file.Path

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class RSocketApi(val path: String)

class RSocketApiProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RSocketApiProcessor(environment.logger)
    }
}

class RSocketApiProcessor(
    private val logger: KSPLogger
):SymbolProcessor{

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation("iuo.zmua.ksp.RSocketApi")
            .filterIsInstance<KSClassDeclaration>()
            .forEach { generateServerInterface(it) }
        return emptyList()
    }

    private object ClassNames {
        val RSOCKET_EXCHANGE = ClassName(
            "org.springframework.messaging.rsocket.service",
            "RSocketExchange"
        )
    }

    private fun generateServerInterface(apiInterface: KSClassDeclaration) {
        val annotation = apiInterface.annotations
            .first { it.shortName.asString() == "RSocketApi" }

        val basePath = annotation.arguments
            .first { it.name?.asString() == "path" }
            .value.toString()
        logger.info("basePath: $basePath")
        val serverName = apiInterface.simpleName.asString()
            .removeSuffix("Api") + "Server"
        logger.info("server name : $serverName")

        logger.info("type spec")
        val serverType = TypeSpec.interfaceBuilder(serverName)
            .addAnnotation(
                AnnotationSpec.builder(ClassNames.RSOCKET_EXCHANGE)
                    .addMember("%S", basePath)
                    .build()
            )
            .addSuperinterface(ClassName(apiInterface.packageName.asString(),apiInterface.simpleName.asString()))

        logger.info("func spec")
        apiInterface.getAllFunctions().forEach { func ->
            serverType.addFunction(
                FunSpec.builder(func.simpleName.asString())
                    .addAnnotation(
                        AnnotationSpec.builder(ClassNames.RSOCKET_EXCHANGE)
                            .addMember("%S", func.simpleName.asString())
                            .build()
                    )
                    .addModifiers(KModifier.OVERRIDE)
                    .apply { if (Modifier.SUSPEND in func.modifiers) addModifiers(KModifier.SUSPEND) }
                    .returns(func.returnType?.resolve()?.let {
                        ClassName(
                            it.declaration.packageName.asString(),
                            it.declaration.simpleName.asString()
                        )
                    } ?: Unit::class.asTypeName())
                    .build()
            )
        }

        logger.info("file spec and write")
        FileSpec.builder(apiInterface.packageName.asString(), serverName)
            .addType(serverType.build())
            .build()
            .writeTo(Path.of("iuo.zmua.server"))

    }

}