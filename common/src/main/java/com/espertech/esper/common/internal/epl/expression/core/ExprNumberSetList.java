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
package com.espertech.esper.common.internal.epl.expression.core;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.client.type.EPTypePremade;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenBlock;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethod;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.type.*;
import com.espertech.esper.common.internal.util.JavaClassHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpressionBuilder.*;

/**
 * Expression for use within crontab to specify a list of values.
 */
public class ExprNumberSetList extends ExprNodeBase implements ExprForge, ExprEvaluator {
    private static final Logger log = LoggerFactory.getLogger(ExprNumberSetList.class);
    private final static String METHOD_HANDLEEXPRNUMBERSETLISTADD = "handleExprNumberSetListAdd";
    private final static String METHOD_HANDLEEXPRNUMBERSETLISTEMPTY = "handleExprNumberSetListEmpty";

    private transient ExprEvaluator[] evaluators;

    public ExprEvaluator getExprEvaluator() {
        return this;
    }

    public EPTypeClass getEvaluationType() {
        return ListParameter.EPTYPE;
    }

    public ExprForge getForge() {
        return this;
    }

    public void toPrecedenceFreeEPL(StringWriter writer, ExprNodeRenderableFlags flags) {
        String delimiter = "";

        writer.append('[');
        Iterator<ExprNode> it = Arrays.asList(this.getChildNodes()).iterator();
        do {
            ExprNode expr = it.next();
            writer.append(delimiter);
            expr.toEPL(writer, ExprPrecedenceEnum.MINIMUM, flags);
            delimiter = ",";
        }
        while (it.hasNext());
        writer.append(']');
    }

    public ExprPrecedenceEnum getPrecedence() {
        return ExprPrecedenceEnum.UNARY;
    }

    public ExprForgeConstantType getForgeConstantType() {

        int max = -1;
        for (ExprNode child : this.getChildNodes()) {
            if (child.getForge().getForgeConstantType().ordinal() > max) {
                max = child.getForge().getForgeConstantType().ordinal();
            }
        }
        if (max == -1) {
            return ExprForgeConstantType.COMPILETIMECONST;
        }
        return ExprForgeConstantType.values()[max];
    }

    public boolean equalsNode(ExprNode node, boolean ignoreStreamPrefix) {
        return node instanceof ExprNumberSetList;
    }

    public ExprNode validate(ExprValidationContext validationContext) throws ExprValidationException {
        // all nodes must either be int, frequency or range
        evaluators = ExprNodeUtilityQuery.getEvaluatorsNoCompile(this.getChildNodes());
        for (int i = 0; i < this.getChildNodes().length; i++) {
            EPType type = this.getChildNodes()[i].getForge().getEvaluationType();
            if (FrequencyParameter.EPTYPE.equals(type) || RangeParameter.EPTYPE.equals(type)) {
                continue;
            }
            if (!(JavaClassHelper.isNumericNonFP(type))) {
                throw new ExprValidationException("Frequency operator requires an integer-type parameter");
            }
        }
        return null;
    }

    public Object evaluate(EventBean[] eventsPerStream, boolean isNewData, ExprEvaluatorContext exprEvaluatorContext) {
        List<NumberSetParameter> parameters = new ArrayList<NumberSetParameter>();
        for (ExprEvaluator child : evaluators) {
            Object value = child.evaluate(eventsPerStream, isNewData, exprEvaluatorContext);
            handleExprNumberSetListAdd(value, parameters);
        }
        handleExprNumberSetListEmpty(parameters);
        return new ListParameter(parameters);
    }

    public CodegenExpression evaluateCodegen(EPTypeClass requiredType, CodegenMethodScope codegenMethodScope, ExprForgeCodegenSymbol exprSymbol, CodegenClassScope codegenClassScope) {
        CodegenMethod methodNode = codegenMethodScope.makeChild(ListParameter.EPTYPE, ExprNumberSetList.class, codegenClassScope);
        CodegenBlock block = methodNode.getBlock()
                .declareVar(EPTypePremade.LIST.getEPType(), "parameters", newInstance(EPTypePremade.ARRAYLIST.getEPType()));
        int count = -1;
        for (ExprNode node : getChildNodes()) {
            count++;
            ExprForge forge = node.getForge();
            EPTypeClass evaluationType = (EPTypeClass) forge.getEvaluationType();
            String refname = "value" + count;
            block.declareVar(evaluationType, refname, forge.evaluateCodegen(requiredType, methodNode, exprSymbol, codegenClassScope))
                    .staticMethod(ExprNumberSetList.class, METHOD_HANDLEEXPRNUMBERSETLISTADD, ref(refname), ref("parameters"));
        }
        block.staticMethod(ExprNumberSetList.class, METHOD_HANDLEEXPRNUMBERSETLISTEMPTY, ref("parameters"))
                .methodReturn(newInstance(ListParameter.EPTYPE, ref("parameters")));
        return localMethod(methodNode);
    }

    public ExprNodeRenderable getForgeRenderable() {
        return this;
    }

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     *
     * @param parameters params
     */
    public static void handleExprNumberSetListEmpty(List<NumberSetParameter> parameters) {
        if (parameters.isEmpty()) {
            log.warn("Empty list of values in list parameter, using upper bounds");
            parameters.add(new IntParameter(Integer.MAX_VALUE));
        }
    }

    /**
     * NOTE: Code-generation-invoked method, method name and parameter order matters
     *
     * @param value      value
     * @param parameters params
     */
    public static void handleExprNumberSetListAdd(Object value, List<NumberSetParameter> parameters) {
        if (value == null) {
            log.info("Null value returned for lower bounds value in list parameter, skipping parameter");
            return;
        }
        if ((value instanceof FrequencyParameter) || (value instanceof RangeParameter)) {
            parameters.add((NumberSetParameter) value);
            return;
        }

        int intValue = ((Number) value).intValue();
        parameters.add(new IntParameter(intValue));
    }
}
