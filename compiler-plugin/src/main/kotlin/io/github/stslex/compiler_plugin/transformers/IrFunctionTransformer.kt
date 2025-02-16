package io.github.stslex.compiler_plugin.transformers

import buildArgsListExpression
import io.github.stslex.compiler_plugin.utils.CompileLogger.Companion.toCompilerLogger
import io.github.stslex.compiler_plugin.utils.buildLambdaForBody
import io.github.stslex.compiler_plugin.utils.buildSaveInCacheCall
import io.github.stslex.compiler_plugin.utils.fullyQualifiedName
import io.github.stslex.compiler_plugin.utils.readQualifier
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.createExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl

internal class IrFunctionTransformer(
    private val pluginContext: IrPluginContext,
    private val messageCollector: MessageCollector
) : IrElementTransformerVoidWithContext() {

    private val logger by lazy { messageCollector.toCompilerLogger() }

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        val qualifierArgs = pluginContext.readQualifier(declaration, logger)
            ?: return super.visitSimpleFunction(declaration)

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
        val memoizeCall = pluginContext.buildSaveInCacheCall(
            keyLiteral = keyLiteral,
            argsListExpr = argsListExpr,
            lambdaExpr = lambdaExpr,
            function = declaration,
            qualifierArgs = qualifierArgs,
            logger = logger
        )

        declaration.body = pluginContext.irFactory.createExpressionBody(memoizeCall)

        return super.visitSimpleFunction(declaration)
    }

}

