/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.common.internal.epl.agg.access.linear;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.agg.core.AggregationAccessorForgeGetCodegenContext;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

/**
 * Represents the aggregation accessor that provides the result for the "window" aggregation function.
 */
public class AggregationAccessorWindowNoEval {
    public static void getValueCodegen(AggregationAccessorWindowNoEvalForge forge, AggregationStateLinearForge accessStateFactory, AggregationAccessorForgeGetCodegenContext context) {
        CodegenExpression size = accessStateFactory.getAggregatorLinear().sizeCodegen();
        CodegenExpression iterator = accessStateFactory.getAggregatorLinear().iteratorCodegen(context.getClassScope(), context.getMethod(), context.getNamedMethods());

        EPTypeClass arrayType = JavaClassHelper.getArrayType(forge.getComponentType());
        context.getMethod().getBlock().ifCondition(equalsIdentity(size, constant(0))).blockReturn(constantNull())
                .declareVar(arrayType, "array", newArrayByLength(forge.getComponentType(), size))
                .declareVar(EPTypePremade.INTEGERPRIMITIVE.getEPType(), "count", constant(0))
                .declareVar(EPTypePremade.ITERATOR.getEPType(), "it", iterator)
                .whileLoop(exprDotMethod(ref("it"), "hasNext"))
                .declareVar(EventBean.EPTYPE, "bean", cast(EventBean.EPTYPE, exprDotMethod(ref("it"), "next")))
                .assignArrayElement(ref("array"), ref("count"), cast(forge.getComponentType(), exprDotUnderlying(ref("bean"))))
                .incrementRef("count")
                .blockEnd()
                .methodReturn(ref("array"));
    }

    public static void getEnumerableEventsCodegen(AggregationAccessorWindowNoEvalForge forge, AggregationStateLinearForge stateForge, AggregationAccessorForgeGetCodegenContext context) {
        context.getMethod().getBlock().ifCondition(equalsIdentity(stateForge.getAggregatorLinear().sizeCodegen(), constant(0)))
                .blockReturn(constantNull())
                .methodReturn(stateForge.getAggregatorLinear().collectionReadOnlyCodegen(context.getMethod(), context.getClassScope(), context.getNamedMethods()));
    }

    public static void getEnumerableScalarCodegen(AggregationAccessorWindowNoEvalForge forge, AggregationStateLinearForge stateForge, AggregationAccessorForgeGetCodegenContext context) {
        context.getMethod().getBlock().declareVar(EPTypePremade.INTEGERPRIMITIVE.getEPType(), "size", stateForge.getAggregatorLinear().sizeCodegen())
                .ifCondition(equalsIdentity(ref("size"), constant(0))).blockReturn(constantNull())
                .declareVar(EPTypePremade.LIST.getEPType(), "values", newInstance(EPTypePremade.ARRAYLIST.getEPType(), ref("size")))
                .declareVar(EPTypePremade.ITERATOR.getEPType(), "it", stateForge.getAggregatorLinear().iteratorCodegen(context.getClassScope(), context.getMethod(), context.getNamedMethods()))
                .whileLoop(exprDotMethod(ref("it"), "hasNext"))
                .declareVar(EventBean.EPTYPE, "bean", cast(EventBean.EPTYPE, exprDotMethod(ref("it"), "next")))
                .declareVar(forge.getComponentType(), "value", cast(forge.getComponentType(), exprDotUnderlying(ref("bean"))))
                .exprDotMethod(ref("values"), "add", ref("value"))
                .blockEnd()
                .methodReturn(ref("values"));
    }
}