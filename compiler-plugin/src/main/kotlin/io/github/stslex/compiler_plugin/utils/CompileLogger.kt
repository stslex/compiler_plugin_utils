package io.github.stslex.compiler_plugin.utils

import org.jetbrains.kotlin.backend.common.toLogger
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

internal class CompileLogger(
    private val messageCollector: MessageCollector
) {

    fun i(msg: String) {
        messageCollector.toLogger()
        messageCollector.report(CompilerMessageSeverity.INFO, msg)
    }

    companion object {

        fun MessageCollector.toCompilerLogger(): CompileLogger = CompileLogger(this)
    }
}