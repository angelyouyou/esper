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
package com.espertech.esper.common.internal.epl.resultset.order;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenSymbolProviderEmpty;
import com.espertech.esper.common.internal.bytecodemodel.core.*;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder;
import com.espertech.esper.common.internal.bytecodemodel.util.CodegenStackGenerator;
import com.espertech.esper.common.internal.epl.agg.core.AggregationGroupByRollupLevel;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.ref;
import static com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenNames.*;
import static com.espertech.esper.common.internal.epl.resultset.codegen.ResultSetProcessorCodegenNames.MEMBER_EXPREVALCONTEXT;
import static com.espertech.esper.common.internal.epl.resultset.order.OrderByProcessorCodegenNames.*;

public class OrderByProcessorCompiler {

    public static void makeOrderByProcessors(OrderByProcessorFactoryForge forge, CodegenClassScope classScope, List<CodegenInnerClass> innerClasses, List<CodegenTypedParam> providerExplicitMembers, CodegenCtor providerCtor, String providerClassName, String memberOrderByFactory) {
        if (forge == null) {
            return;
        }

        providerExplicitMembers.add(new CodegenTypedParam(OrderByProcessorFactory.EPTYPE, memberOrderByFactory));

        makeFactory(forge, classScope, innerClasses, providerClassName);
        makeService(forge, classScope, innerClasses, providerClassName);

        providerCtor.getBlock().assignRef(memberOrderByFactory, CodegenExpressionBuilder.newInstance(CLASSNAME_ORDERBYPROCESSORFACTORY, ref("this")));
    }

    private static void makeFactory(OrderByProcessorFactoryForge forge, CodegenClassScope classScope, List<CodegenInnerClass> innerClasses, String providerClassName) {
        CodegenMethod instantiateMethod = CodegenMethod.makeParentNode(OrderByProcessor.EPTYPE, OrderByProcessorCompiler.class, CodegenSymbolProviderEmpty.INSTANCE, classScope).addParam(ExprEvaluatorContext.EPTYPE, MEMBER_EXPREVALCONTEXT.getRef());
        forge.instantiateCodegen(instantiateMethod, classScope);

        List<CodegenTypedParam> ctorParams = Collections.singletonList(new CodegenTypedParam(providerClassName, "o"));
        CodegenCtor ctor = new CodegenCtor(OrderByProcessorCompiler.class, classScope, ctorParams);

        CodegenClassMethods methods = new CodegenClassMethods();
        CodegenStackGenerator.recursiveBuildStack(instantiateMethod, "instantiate", methods);
        CodegenInnerClass innerClass = new CodegenInnerClass(CLASSNAME_ORDERBYPROCESSORFACTORY, OrderByProcessorFactory.EPTYPE, ctor, Collections.emptyList(), methods);
        innerClasses.add(innerClass);
    }

    private static void makeService(OrderByProcessorFactoryForge forge, CodegenClassScope classScope, List<CodegenInnerClass> innerClasses, String providerClassName) {
        CodegenNamedMethods namedMethods = new CodegenNamedMethods();

        CodegenMethod sortPlainMethod = CodegenMethod.makeParentNode(EventBean.EPTYPEARRAY, forge.getClass(), CodegenSymbolProviderEmpty.INSTANCE, classScope).addParam(SORTPLAIN_PARAMS);
        forge.sortPlainCodegen(sortPlainMethod, classScope, namedMethods);

        CodegenMethod sortWGroupKeysMethod = CodegenMethod.makeParentNode(EventBean.EPTYPEARRAY, forge.getClass(), CodegenSymbolProviderEmpty.INSTANCE, classScope).addParam(SORTWGROUPKEYS_PARAMS);
        forge.sortWGroupKeysCodegen(sortWGroupKeysMethod, classScope, namedMethods);

        CodegenMethod sortRollupMethod = CodegenMethod.makeParentNode(EventBean.EPTYPEARRAY, forge.getClass(), CodegenSymbolProviderEmpty.INSTANCE, classScope).addParam(SORTROLLUP_PARAMS);
        forge.sortRollupCodegen(sortRollupMethod, classScope, namedMethods);

        CodegenMethod getSortKeyMethod = CodegenMethod.makeParentNode(EPTypePremade.OBJECT.getEPType(), forge.getClass(), CodegenSymbolProviderEmpty.INSTANCE, classScope).addParam(EventBean.EPTYPEARRAY, REF_EPS.getRef()).addParam(EPTypePremade.BOOLEANPRIMITIVE.getEPType(), REF_ISNEWDATA.getRef()).addParam(ExprEvaluatorContext.EPTYPE, REF_EXPREVALCONTEXT.getRef());
        forge.getSortKeyCodegen(getSortKeyMethod, classScope, namedMethods);

        CodegenMethod getSortKeyRollupMethod = CodegenMethod.makeParentNode(EPTypePremade.OBJECT.getEPType(), forge.getClass(), CodegenSymbolProviderEmpty.INSTANCE, classScope).addParam(EventBean.EPTYPEARRAY, REF_EPS.getRef()).addParam(EPTypePremade.BOOLEANPRIMITIVE.getEPType(), REF_ISNEWDATA.getRef()).addParam(ExprEvaluatorContext.EPTYPE, REF_EXPREVALCONTEXT.getRef()).addParam(AggregationGroupByRollupLevel.EPTYPE, REF_ORDERROLLUPLEVEL.getRef());
        forge.getSortKeyRollupCodegen(getSortKeyRollupMethod, classScope, namedMethods);

        CodegenMethod sortWOrderKeysMethod = CodegenMethod.makeParentNode(EventBean.EPTYPEARRAY, forge.getClass(), CodegenSymbolProviderEmpty.INSTANCE, classScope).addParam(EventBean.EPTYPEARRAY, REF_OUTGOINGEVENTS.getRef()).addParam(EPTypePremade.OBJECTARRAY.getEPType(), REF_ORDERKEYS.getRef()).addParam(ExprEvaluatorContext.EPTYPE, REF_EXPREVALCONTEXT.getRef());
        forge.sortWOrderKeysCodegen(sortWOrderKeysMethod, classScope, namedMethods);

        CodegenMethod sortTwoKeysMethod = CodegenMethod.makeParentNode(EventBean.EPTYPEARRAY, forge.getClass(), CodegenSymbolProviderEmpty.INSTANCE, classScope).addParam(SORTTWOKEYS_PARAMS);
        forge.sortTwoKeysCodegen(sortTwoKeysMethod, classScope, namedMethods);

        List<CodegenTypedParam> members = new ArrayList<>();
        List<CodegenTypedParam> ctorParams = new ArrayList<>();
        ctorParams.add(new CodegenTypedParam(providerClassName, "o"));
        CodegenCtor ctor = new CodegenCtor(OrderByProcessorCompiler.class, classScope, ctorParams);
        forge.ctorCodegen(ctor, members, classScope);

        CodegenClassMethods innerMethods = new CodegenClassMethods();
        CodegenStackGenerator.recursiveBuildStack(sortPlainMethod, "sortPlain", innerMethods);
        CodegenStackGenerator.recursiveBuildStack(sortWGroupKeysMethod, "sortWGroupKeys", innerMethods);
        CodegenStackGenerator.recursiveBuildStack(sortRollupMethod, "sortRollup", innerMethods);
        CodegenStackGenerator.recursiveBuildStack(getSortKeyMethod, "getSortKey", innerMethods);
        CodegenStackGenerator.recursiveBuildStack(getSortKeyRollupMethod, "getSortKeyRollup", innerMethods);
        CodegenStackGenerator.recursiveBuildStack(sortWOrderKeysMethod, "sortWOrderKeys", innerMethods);
        CodegenStackGenerator.recursiveBuildStack(sortTwoKeysMethod, "sortTwoKeys", innerMethods);
        for (Map.Entry<String, CodegenMethod> methodEntry : namedMethods.getMethods().entrySet()) {
            CodegenStackGenerator.recursiveBuildStack(methodEntry.getValue(), methodEntry.getKey(), innerMethods);
        }

        CodegenInnerClass innerClass = new CodegenInnerClass(OrderByProcessorCodegenNames.CLASSNAME_ORDERBYPROCESSOR, OrderByProcessor.EPTYPE, ctor, members, innerMethods);
        innerClasses.add(innerClass);
    }
}
