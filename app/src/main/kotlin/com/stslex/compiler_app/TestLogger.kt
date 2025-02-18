package com.stslex.compiler_app

import io.github.stslex.compiler_plugin.utils.Action

class TestLogger : Action {

    override fun invoke(
        name: String,
        isProcess: Boolean
    ) {
        println("test action $name procession: $isProcess")
    }
}