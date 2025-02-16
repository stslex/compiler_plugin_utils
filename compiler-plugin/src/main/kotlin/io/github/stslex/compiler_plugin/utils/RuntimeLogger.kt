package io.github.stslex.compiler_plugin.utils

import java.util.logging.Level
import java.util.logging.Logger

internal class RuntimeLogger private constructor(tag: String) {

    private val logger = Logger.getLogger(tag)

    fun i(msg: String) {
        logger.log(Level.INFO, msg)
    }

    companion object {

        private const val TAG = "KotlinCompilerLogger"

        fun i(msg: String) {
            RuntimeLogger(TAG).i(msg)
        }

        fun tag(tag: String): RuntimeLogger = RuntimeLogger(tag)
    }

}