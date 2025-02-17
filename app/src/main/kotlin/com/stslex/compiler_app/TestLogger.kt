package com.stslex.compiler_app

import io.github.stslex.compiler_plugin.utils.Action

class TestLogger : Action {

    override fun invoke(isProcess: Boolean) {
        println("test logger procession: $isProcess")
    }
}