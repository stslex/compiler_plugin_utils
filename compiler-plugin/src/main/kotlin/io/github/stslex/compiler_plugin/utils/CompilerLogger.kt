package io.github.stslex.compiler_plugin.utils

/**Use for any action in runtime plugin*/
public fun interface Action {

    /**
     * @param isProcess show that function block will process
     **/
    public operator fun invoke(isProcess: Boolean)

}
