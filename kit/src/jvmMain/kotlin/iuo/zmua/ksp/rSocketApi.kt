package iuo.zmua.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo

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

    // 用于标记是否已经完成文件生成
    private var filesGenerated = false
    // 用于记录已经处理过的符号
    private val processedSymbols = mutableSetOf<KSClassDeclaration>()

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("processing RSocketApi")
        // 如果文件已经生成，跳过本轮处理
        if (filesGenerated) {
            logger.info("Files already generated, skipping this round of processing.")
            return emptyList()
        }
        resolver.getDeclarationsFromPackage("iuo.zmua.api")
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.isAbstract() && it.simpleName.asString().endsWith("Api")}
            .filter { it !in processedSymbols }
            .forEach {
                generateServerInterface(it)
                processedSymbols.add(it)
            }
        // 标记文件已经生成
        filesGenerated = true
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
            .addModifiers()
            .addAnnotation(
                AnnotationSpec.builder(ClassNames.RSOCKET_EXCHANGE)
                    .addMember("%S", basePath)
                    .build()
            )
            .addSuperinterface(ClassName(apiInterface.packageName.asString(),apiInterface.simpleName.asString()))
        serverType.modifiers.removeIf(Modifier.PUBLIC::equals)

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
                    .addParameters(
                        func.parameters.map { param ->
                            ParameterSpec.builder(param.name?.asString()?:"param", buildTypeName(param.type.resolve()))
                               .build()
                        }
                    )
                    .returns(func.returnType?.resolve()?.let { buildTypeName(it) } ?: Unit::class.asTypeName())
                    .build()
            )
        }

        logger.info("file spec and write")
        FileSpec.builder("iuo.zmua.server", serverName)
            .addType(serverType.build())
            .build().apply {
                logger.info("Generated code:\n${toString()}")
                writeTo(codeGenerator, false)
            }
    }

    private fun buildTypeName(it: KSType): TypeName {
        val type = ClassName(
            it.declaration.packageName.asString(),
            it.declaration.simpleName.asString()
        )
        val typeName =  if(it.arguments.isNotEmpty()){
            val typeArgs = it.arguments.mapNotNull { it.type?.resolve()?.let { resolved ->
                ClassName(resolved.declaration.packageName.asString(), resolved.declaration.simpleName.asString())
            } }
            type.parameterizedBy(typeArgs)
        }else
            type

        return if (it.isMarkedNullable) {
            typeName.copy(nullable = true)
        } else {
            typeName
        }
    }

}
