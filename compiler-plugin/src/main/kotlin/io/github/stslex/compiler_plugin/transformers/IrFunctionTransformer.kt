package io.github.stslex.compiler_plugin.transformers

import buildArgsListExpression
import io.github.stslex.compiler_plugin.DistinctUntilChangeFun
import io.github.stslex.compiler_plugin.utils.buildLambdaForBody
import io.github.stslex.compiler_plugin.utils.buildSaveInCacheCall
import io.github.stslex.compiler_plugin.utils.fullyQualifiedName
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.createExpressionBody
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.name.FqName

internal class IrFunctionTransformer(
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    private val IrSimpleFunction.isAnnotationValid: Boolean
        get() = DistinctUntilChangeFun::class.qualifiedName
            ?.let { qualifier -> hasAnnotation(FqName(qualifier)) }
            ?: false

    override fun visitSimpleFunction(declaration: IrSimpleFunction): IrStatement {
        if (declaration.isAnnotationValid.not()) {
            return super.visitSimpleFunction(declaration)
        }
        val originalBody = declaration.body ?: return super.visitSimpleFunction(declaration)


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
        )

        declaration.body = pluginContext.irFactory.createExpressionBody(memoizeCall)

        return super.visitSimpleFunction(declaration)
    }
}

