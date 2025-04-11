package iuo.zmua.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import java.nio.file.Path

class RSocketApiProcessorProvider : SymbolProcessorProvider {
    init {
        println("RSocketApiProcessorProvider init")
    }
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        println("RSocketApiProcessorProvider")
        return RSocketApiProcessor(environment.codeGenerator,environment.logger)
    }
}

class RSocketApiProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
):SymbolProcessor{

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("processing RSocketApi")
        resolver.getDeclarationsFromPackage("iuo.zmua.api")
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.isAbstract() && it.simpleName.asString().endsWith("Api")}
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
        val serviceName = apiInterface.simpleName.asString()
            .removeSuffix("Api")
        val serverName = serviceName + "Server"
        logger.info("server name : $serverName")

        val annotation = apiInterface.annotations
            .firstOrNull { it.shortName.asString() == "RSocketApi" }

        val basePath = annotation?.arguments
            ?.first { it.name?.asString() == "path" }
            ?.value?.toString() ?: serviceName.replaceFirstChar { it.lowercase() }
        logger.info("basePath: $basePath")

        logger.info("type spec")
        val serverType = TypeSpec.interfaceBuilder(serverName)
            .addAnnotation(
                AnnotationSpec.builder(ClassNames.RSOCKET_EXCHANGE)
                    .addMember("%S", basePath)
                    .build()
            )
            .addSuperinterface(ClassName(apiInterface.packageName.asString(),apiInterface.simpleName.asString()))

        logger.info("func spec")
        apiInterface.getAllFunctions().filter { func -> func.parentDeclaration == apiInterface }.forEach { func ->
            val funcName = func.simpleName.asString()
            val funcAnnotation = func.annotations
               .firstOrNull { it.shortName.asString() == "RSocketApi" }
            val path = funcAnnotation?.arguments?.first { it.name?.asString() == "path" }
                ?.value?.toString()?:funcName
            serverType.addFunction(
                FunSpec.builder(funcName)
                    .addAnnotation(
                        AnnotationSpec.builder(ClassNames.RSOCKET_EXCHANGE)
                            .addMember("%S", path)
                            .build()
                    )
                    .addModifiers(KModifier.OVERRIDE)
                    .addModifiers(KModifier.ABSTRACT)
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
        FileSpec.builder("iuo.zmua.server", serverName)
            .addType(serverType.build())
            .build().apply {
                logger.info("Generated code:\n${toString()}")
                codeGenerator.createNewFile(
                    dependencies = Dependencies(false, apiInterface.containingFile!!),
                    packageName = "iuo.zmua.server",
                    fileName = serverName
                ).use {
                    it.bufferedWriter(Charsets.UTF_8).use {
                        it.write(toString())
                    }
                }
            }
    }

}
