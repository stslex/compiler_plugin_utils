package io.github.stslex.compiler_plugin.transformers

import buildArgsListExpression
import io.github.stslex.compiler_plugin.DistinctUntilChangeFun.Companion.SINGLETON_ALLOW
import io.github.stslex.compiler_plugin.utils.CompileLogger.Companion.toCompilerLogger
import io.github.stslex.compiler_plugin.utils.buildLambdaForBody
import io.github.stslex.compiler_plugin.utils.buildSaveInCacheCall
import io.github.stslex.compiler_plugin.utils.fullyQualifiedName
import io.github.stslex.compiler_plugin.utils.generateFields
import io.github.stslex.compiler_plugin.utils.getQualifierValue
import io.github.stslex.compiler_plugin.utils.readQualifier
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.jvm.ir.fileParentOrNull
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.createExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.util.parentClassOrNull

internal class IrFunctionTransformer(
    private val pluginContext: IrPluginContext,
    private val messageCollector: MessageCollector
) : IrElementTransformerVoidWithContext() {

    private val logger by lazy { messageCollector.toCompilerLogger() }

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        val qualifierArgs = pluginContext.readQualifier(declaration, logger)
            ?: return super.visitSimpleFunction(declaration)

        val isSingletonAllow = declaration.getQualifierValue("singletonAllow", SINGLETON_ALLOW)

        if (isSingletonAllow.not() && declaration.parentClassOrNull == null) {
            error("singleton is not allowed for ${declaration.name} in ${declaration.fileParentOrNull}")
        }

        val originalBody = declaration.body ?: return super.visitSimpleFunction(declaration)

        logger.i("fullyQualifiedName: ${declaration.fullyQualifiedName}")
        val keyLiteral = IrConstImpl.string(
            startOffset = declaration.startOffset,
            endOffset = declaration.endOffset,
            type = pluginContext.irBuiltIns.stringType,
            value = declaration.fullyQualifiedName
        )

        val argsListExpr = pluginContext.buildArgsListExpression(declaration)
        val lambdaExpr = pluginContext.buildLambdaForBody(originalBody, declaration)

        val backingField = pluginContext.generateFields(declaration, logger)

        logger.i("backingField = $backingField")
        val memoizeCall = pluginContext.buildSaveInCacheCall(
            keyLiteral = keyLiteral,
            argsListExpr = argsListExpr,
            lambdaExpr = lambdaExpr,
            function = declaration,
            backingField = backingField,
            logger = logger,
            qualifierArgs = qualifierArgs
        )

        declaration.body = pluginContext.irFactory.createExpressionBody(memoizeCall)

        return super.visitSimpleFunction(declaration)
    }

}
