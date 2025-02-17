package io.github.stslex.compiler_plugin.model

import io.github.stslex.compiler_plugin.utils.Action

internal data class DistinctChangeConfig(
    val logging: Boolean,
    val action: Action
)