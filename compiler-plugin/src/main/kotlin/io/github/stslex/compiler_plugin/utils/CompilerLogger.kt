package io.github.stslex.compiler_plugin.utils

/**
 * Use for any action in runtime plugin
 * @suppress shouldn't have properties in it's constructor - cause compiling crush
 **/
public fun interface Action {

    /**
     * @param name setted at [io.github.stslex.compiler_plugin.DistinctUntilChangeFun] property name
     * @param isProcess show that function block will process
     **/
    public operator fun invoke(
        name: String,
        isProcess: Boolean
    )

}
