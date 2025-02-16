package io.github.stslex.compiler_plugin

import io.github.stslex.compiler_plugin.utils.RuntimeLogger

internal object DistinctChangeCache {

    private val cache = mutableMapOf<String, Pair<List<Any?>, Any?>>()
    private val logger = RuntimeLogger.tag("DistinctChangeLogger")

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <R> invoke(
        key: String,
        args: List<Any?>,
        body: () -> R,
        config: DistinctChangeConfig
    ): R {
        val entry = cache[key]

        if (config.logging) {
            logger.i("key: $key, config:$config, entry: $entry, args: $args")
        }

        if (entry != null && entry.first == args) {
            return entry.second as R
        }

        val result = body()
        cache[key] = args to result
        return result
    }
}