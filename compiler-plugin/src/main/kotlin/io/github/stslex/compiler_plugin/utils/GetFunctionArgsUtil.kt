import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

@OptIn(UnsafeDuringIrConstructionAPI::class)
internal fun IrPluginContext.buildArgsListExpression(
    function: IrSimpleFunction,
): IrExpression {
    val args = function.valueParameters.map { param ->
        IrGetValueImpl(
            startOffset = function.startOffset,
            endOffset = function.endOffset,
            type = param.type,
            symbol = param.symbol
        )
    }

    val candidateFunctions = referenceFunctions(
        CallableId(
            packageName = FqName("kotlin.collections"),
            callableName = Name.identifier("listOf")
        )
    )

    val varargSymbol = candidateFunctions.firstOrNull { candidate ->
        candidate.owner.valueParameters.size == 1 &&
                candidate.owner.valueParameters.first().isVararg
    }

    return if (varargSymbol != null) {
        buildArgsListExpressionWithVararg(function, varargSymbol, args)
    } else {
        buildArgsListExpressionWithCall(candidateFunctions, function, args)
    }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
private fun IrPluginContext.buildArgsListExpressionWithCall(
    candidateFunctions: Collection<IrSimpleFunctionSymbol>,
    function: IrSimpleFunction,
    args: List<IrGetValueImpl>
): IrCallImpl {
    val symbol = candidateFunctions.firstOrNull { candidate ->
        candidate.owner.valueParameters.size == args.size
    } ?: error("Cannot find kotlin.collections.listOf with ${args.size} parameter(s)")

    return IrCallImpl(
        startOffset = function.startOffset,
        endOffset = function.endOffset,
        type = irBuiltIns.listClass.owner.defaultType,
        symbol = symbol,
        typeArgumentsCount = 1,
        valueArgumentsCount = args.size
    ).apply {
        putTypeArgument(0, irBuiltIns.anyNType)
        args.forEachIndexed { index, arg ->
            putValueArgument(index, arg)
        }
    }
}

@OptIn(UnsafeDuringIrConstructionAPI::class)
private fun IrPluginContext.buildArgsListExpressionWithVararg(
    function: IrSimpleFunction,
    varargSymbol: IrSimpleFunctionSymbol,
    args: List<IrGetValueImpl>
): IrExpression {
    val varargExpression = IrVarargImpl(
        startOffset = function.startOffset,
        endOffset = function.endOffset,
        type = irBuiltIns.arrayClass.typeWith(irBuiltIns.anyNType),
        varargElementType = irBuiltIns.anyNType,
        elements = args
    )

    return IrCallImpl(
        startOffset = function.startOffset,
        endOffset = function.endOffset,
        type = irBuiltIns.listClass.owner.defaultType,
        symbol = varargSymbol,
        typeArgumentsCount = 1,
        valueArgumentsCount = 1
    ).apply {
        putTypeArgument(0, irBuiltIns.anyNType)
        putValueArgument(0, varargExpression)
    }
}