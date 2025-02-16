package io.github.stslex.compiler_plugin

import io.github.stslex.compiler_plugin.extensions.DistinctChangeIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
internal class WorkshopCompilerPlugin(
    override val supportsK2: Boolean = true
) : CompilerPluginRegistrar() {

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(DistinctChangeIrGenerationExtension(configuration))
    }
}