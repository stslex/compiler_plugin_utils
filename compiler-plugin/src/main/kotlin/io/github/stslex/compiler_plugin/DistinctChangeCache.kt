package io.github.stslex.compiler_plugin

import io.github.stslex.compiler_plugin.model.DistinctChangeConfig
import io.github.stslex.compiler_plugin.utils.RuntimeLogger
import org.jetbrains.kotlin.utils.addToStdlib.runIf

internal class DistinctChangeCache(
    private val config: DistinctChangeConfig
) {

    private val cache = mutableMapOf<String, Pair<List<Any?>, Any?>>()
    private val logger = runIf(config.logging) { RuntimeLogger.tag("DistinctChangeLogger") }

    @Suppress("UNCHECKED_CAST")
    internal operator fun <R> invoke(
        key: String,
        args: List<Any?>,
        body: () -> R,
    ): R {
        val entry = cache[key]

        logger?.i("name: ${config.name} key: $key, config:$config, entry: $entry, args: $args")

        config.action(
            name = config.name,
            isProcess = entry != null && entry.first == args
        )

        if (entry != null && entry.first == args) {
            logger?.i("${config.name} with key $key not change")
            return entry.second as R
        }

        val result = body()
        cache[key] = args to result

        return result
    }
}