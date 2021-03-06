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
package com.espertech.esper.common.internal.epl.enummethod.dot;

import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.compile.stage2.StatementRawInfo;
import com.espertech.esper.common.internal.compile.stage3.StatementCompileTimeServices;
import com.espertech.esper.common.internal.epl.enummethod.cache.ExpressionResultCacheStackEntry;
import com.espertech.esper.common.internal.epl.enummethod.compile.EnumMethodCallStackHelperImpl;
import com.espertech.esper.common.internal.epl.enummethod.eval.EnumForge;
import com.espertech.esper.common.internal.epl.enummethod.eval.EnumForgeDesc;
import com.espertech.esper.common.internal.epl.enummethod.eval.EnumForgeDescFactory;
import com.espertech.esper.common.internal.epl.enummethod.eval.EnumForgeLambdaDesc;
import com.espertech.esper.common.internal.epl.expression.codegen.ExprForgeCodegenSymbol;
import com.espertech.esper.common.internal.epl.expression.core.*;
import com.espertech.esper.common.internal.epl.expression.dot.core.ExprDotEval;
import com.espertech.esper.common.internal.epl.expression.dot.core.ExprDotEvalVisitor;
import com.espertech.esper.common.internal.epl.expression.visitor.ExprNodeIdentifierCollectVisitor;
import com.espertech.esper.common.internal.epl.methodbase.*;
import com.espertech.esper.common.internal.epl.streamtype.StreamTypeService;
import com.espertech.esper.common.internal.epl.streamtype.StreamTypeServiceImpl;
import com.espertech.esper.common.internal.epl.util.EPLExpressionParamType;
import com.espertech.esper.common.internal.epl.util.EPLValidationUtil;
import com.espertech.esper.common.internal.rettype.EPChainableType;
import com.espertech.esper.common.internal.rettype.EPChainableTypeEventSingle;
import com.espertech.esper.common.internal.rettype.EPChainableTypeHelper;
import com.espertech.esper.common.internal.util.CollectionUtil;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;

public abstract class ExprDotForgeEnumMethodBase implements ExprDotForgeEnumMethod, ExpressionResultCacheStackEntry {

    protected EnumMethodDesc enumMethodDesc;
    protected String enumMethodUsedName;
    protected int streamCountIncoming;
    protected EnumForge enumForge;
    protected int enumEvalNumRequiredEvents;
    protected EPChainableType typeInfo;
    protected boolean cache;

    protected ExprDotForgeEnumMethodBase() {
    }

    public void initialize(DotMethodFP footprint, EnumMethodEnum enumMethod, String enumMethodUsedName, EventType inputEventType, EPTypeClass collectionComponentType, List<ExprNode> parameters, StreamTypeService streamTypeService, StatementRawInfo statementRawInfo, StatementCompileTimeServices services) throws ExprValidationException {
        // override as required
    }

    public abstract EnumForgeDescFactory getForgeFactory(DotMethodFP footprint, List<ExprNode> parameters, EnumMethodEnum enumMethod, String enumMethodUsedName, EventType inputEventType, EPTypeClass collectionComponentType, ExprValidationContext validationContext) throws ExprValidationException;

    public void visit(ExprDotEvalVisitor visitor) {
        visitor.visitEnumeration(enumMethodDesc.getEnumMethodName());
    }

    public ExprDotEval getDotEvaluator() {
        return new ExprDotForgeEnumMethodEval(this, enumForge.getEnumEvaluator(), enumEvalNumRequiredEvents);
    }

    public CodegenExpression codegen(CodegenExpression inner, EPTypeClass innerType, CodegenMethodScope parent, ExprForgeCodegenSymbol symbols, CodegenClassScope classScope) {
        return ExprDotForgeEnumMethodEval.codegen(this, inner, innerType, parent, symbols, classScope);
    }

    public void init(Integer streamOfProviderIfApplicable, EnumMethodDesc enumMethodDesc, String enumMethodUsedName, EPChainableType typeInfo, List<ExprNode> parameters, ExprValidationContext validationContext) throws ExprValidationException {

        final EventType eventTypeColl = EPChainableTypeHelper.getEventTypeMultiValued(typeInfo);
        final EventType eventTypeBean = EPChainableTypeEventSingle.fromInputOrNull(typeInfo);
        final EPTypeClass collectionComponentType = EPChainableTypeHelper.getCollectionOrArrayComponentTypeOrNull(typeInfo);

        this.enumMethodDesc = enumMethodDesc;
        this.enumMethodUsedName = enumMethodUsedName;
        this.streamCountIncoming = validationContext.getStreamTypeService().getEventTypes().length;

        if (eventTypeColl == null && collectionComponentType == null && eventTypeBean == null) {
            throw new ExprValidationException("Invalid input for built-in enumeration method '" + enumMethodUsedName + "', expecting collection of event-type or scalar values as input, received " + EPChainableTypeHelper.toTypeDescriptive(typeInfo));
        }

        // compile parameter abstract for validation against available footprints
        DotMethodFPProvided footprintProvided = DotMethodUtil.getProvidedFootprint(parameters);

        // validate parameters
        DotMethodInputTypeMatcher inputTypeMatcher = new DotMethodInputTypeMatcher() {
            public boolean matches(DotMethodFP footprint) {
                if (footprint.getInput() == DotMethodFPInputEnum.EVENTCOLL && eventTypeBean == null && eventTypeColl == null) {
                    return false;
                }
                if (footprint.getInput() == DotMethodFPInputEnum.SCALAR_ANY && collectionComponentType == null) {
                    return false;
                }
                return true;
            }
        };
        DotMethodFP footprint = DotMethodUtil.validateParametersDetermineFootprint(enumMethodDesc.getFootprints(), DotMethodTypeEnum.ENUM, enumMethodUsedName, footprintProvided, inputTypeMatcher);

        // validate input criteria met for this footprint
        if (footprint.getInput() != DotMethodFPInputEnum.ANY) {
            String message = "Invalid input for built-in enumeration method '" + enumMethodUsedName + "' and " + footprint.getParameters().length + "-parameter footprint, expecting collection of ";
            String received = " as input, received " + EPChainableTypeHelper.toTypeDescriptive(typeInfo);
            if (footprint.getInput() == DotMethodFPInputEnum.EVENTCOLL && eventTypeColl == null) {
                throw new ExprValidationException(message + "events" + received);
            }
            if (footprint.getInput().isScalar() && collectionComponentType == null) {
                throw new ExprValidationException(message + "values (typically scalar values)" + received);
            }
            if (footprint.getInput() == DotMethodFPInputEnum.SCALAR_NUMERIC && !JavaClassHelper.isNumeric(collectionComponentType)) {
                throw new ExprValidationException(message + "numeric values" + received);
            }
        }

        // manage context of this lambda-expression in regards to outer lambda-expression that may call this one.
        EnumMethodCallStackHelperImpl enumCallStackHelper = validationContext.getEnumMethodCallStackHelper();
        enumCallStackHelper.pushStack(this);

        try {
            // initialize
            EventType inputEventType = eventTypeBean == null ? eventTypeColl : eventTypeBean;
            initialize(footprint, enumMethodDesc.getEnumMethod(), enumMethodUsedName, inputEventType, collectionComponentType, parameters, validationContext.getStreamTypeService(), validationContext.getStatementRawInfo(), validationContext.getStatementCompileTimeService());

            // get-forge-desc-factory
            EnumForgeDescFactory forgeDescFactory = getForgeFactory(footprint, parameters, enumMethodDesc.getEnumMethod(), enumMethodUsedName, inputEventType, collectionComponentType, validationContext);

            // handle body and parameter list
            List<ExprDotEvalParam> bodiesAndParameters = new ArrayList<>();
            int count = 0;
            for (ExprNode node : parameters) {
                ExprDotEvalParam bodyAndParameter = getBodyAndParameter(forgeDescFactory, enumMethodUsedName, count++, node, validationContext, footprint);
                bodiesAndParameters.add(bodyAndParameter);
            }

            EnumForgeDesc forgeDesc = forgeDescFactory.makeEnumForgeDesc(bodiesAndParameters, streamCountIncoming, validationContext.getStatementCompileTimeService());
            this.enumForge = forgeDesc.getForge();
            this.typeInfo = forgeDesc.getType();
            this.enumEvalNumRequiredEvents = enumForge.getStreamNumSize();

            // determine the stream ids of event properties asked for in the evaluator(s)
            HashSet<Integer> streamsRequired = new HashSet<Integer>();
            ExprNodeIdentifierCollectVisitor visitor = new ExprNodeIdentifierCollectVisitor();
            for (ExprDotEvalParam desc : bodiesAndParameters) {
                desc.getBody().accept(visitor);
                for (ExprIdentNode ident : visitor.getExprProperties()) {
                    streamsRequired.add(ident.getStreamId());
                }
            }
            if (streamOfProviderIfApplicable != null) {
                streamsRequired.add(streamOfProviderIfApplicable);
            }

            // We turn on caching if the stack is not empty (we are an inner lambda) and the dependency does not include the stream.
            boolean isInner = !enumCallStackHelper.popLambda();
            if (isInner) {
                // If none of the properties that the current lambda uses comes from the ultimate parent(s) or subsequent streams, then cache.
                Deque<ExpressionResultCacheStackEntry> parents = enumCallStackHelper.getStack();
                boolean found = false;
                for (int req : streamsRequired) {
                    ExprDotForgeEnumMethodBase first = (ExprDotForgeEnumMethodBase) parents.getFirst();
                    int parentIncoming = first.streamCountIncoming - 1;
                    int selfAdded = streamCountIncoming;    // the one we use ourselfs
                    if (req > parentIncoming && req < selfAdded) {
                        found = true;
                    }
                }
                cache = !found;
            }
        } catch (ExprValidationException ex) {
            enumCallStackHelper.popLambda();
            throw ex;
        }
    }

    public EPChainableType getTypeInfo() {
        return typeInfo;
    }

    private ExprDotEvalParam getBodyAndParameter(EnumForgeDescFactory forgeDescFactory,
                                                 String enumMethodUsedName,
                                                 int parameterNum,
                                                 ExprNode parameterNode,
                                                 ExprValidationContext validationContext,
                                                 DotMethodFP footprint) throws ExprValidationException {

        // handle an expression that is a constant or other (not =>)
        if (!(parameterNode instanceof ExprLambdaGoesNode)) {

            // no node subtree validation is required here, the chain parameter validation has taken place in ExprDotNode.validate
            // validation of parameter types has taken place in footprint matching
            return new ExprDotEvalParamExpr(parameterNum, parameterNode, parameterNode.getForge());
        }

        ExprLambdaGoesNode goesNode = (ExprLambdaGoesNode) parameterNode;

        // Get secondary
        EnumForgeLambdaDesc lambdaDesc = forgeDescFactory.getLambdaStreamTypesForParameter(parameterNum);
        String[] additionalStreamNames = lambdaDesc.getStreamNames();
        EventType[] additionalEventTypes = lambdaDesc.getTypes();

        validateDuplicateStreamNames(validationContext.getStreamTypeService().getStreamNames(), goesNode.getGoesToNames());

        // add name and type to list of known types
        EventType[] addTypes = (EventType[]) CollectionUtil.arrayExpandAddElements(validationContext.getStreamTypeService().getEventTypes(), additionalEventTypes);
        String[] addNames = (String[]) CollectionUtil.arrayExpandAddElements(validationContext.getStreamTypeService().getStreamNames(), additionalStreamNames);

        StreamTypeServiceImpl types = new StreamTypeServiceImpl(addTypes, addNames, new boolean[addTypes.length], false, validationContext.getStreamTypeService().isOptionalStreams());

        // validate expression body
        ExprNode filter = goesNode.getChildNodes()[0];
        try {
            ExprValidationContext filterValidationContext = new ExprValidationContext(types, validationContext);
            filter = ExprNodeUtilityValidate.getValidatedSubtree(ExprNodeOrigin.DECLAREDEXPRBODY, filter, filterValidationContext);
        } catch (ExprValidationException ex) {
            throw new ExprValidationException("Failed to validate enumeration method '" + enumMethodUsedName + "' parameter " + parameterNum + ": " + ex.getMessage(), ex);
        }

        ExprForge filterForge = filter.getForge();
        EPLExpressionParamType expectedType = footprint.getParameters()[parameterNum].getParamType();
        // Lambda-methods don't use a specific expected return-type, so passing null for type is fine.
        EPLValidationUtil.validateParameterType(enumMethodUsedName, DotMethodTypeEnum.ENUM.getTypeName(), false, expectedType, null, filterForge.getEvaluationType(), parameterNum, filter);

        int numStreamsIncoming = validationContext.getStreamTypeService().getEventTypes().length;
        return new ExprDotEvalParamLambda(parameterNum, filter, filterForge,
            numStreamsIncoming, goesNode.getGoesToNames(), lambdaDesc);
    }

    private void validateDuplicateStreamNames(String[] streamNames, List<String> goesToNames) throws ExprValidationException {
        for (int nameIdx = 0; nameIdx < goesToNames.size(); nameIdx++) {
            for (int exist = 0; exist < streamNames.length; exist++) {
                if (streamNames[exist] != null && streamNames[exist].equalsIgnoreCase(goesToNames.get(nameIdx))) {
                    String message = "Failed to validate enumeration method '" + enumMethodUsedName + "', the lambda-parameter name '" + goesToNames.get(nameIdx) + "' has already been declared in this context";
                    throw new ExprValidationException(message);
                }
            }
        }
    }

    public String toString() {
        return this.getClass().getSimpleName() +
            " lambda=" + enumMethodDesc;
    }
}
