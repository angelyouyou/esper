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

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenClassScope;
import com.espertech.esper.common.internal.bytecodemodel.base.CodegenMethodScope;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;
import com.espertech.esper.common.internal.rettype.EPChainableType;
import com.espertech.esper.common.internal.rettype.EPChainableTypeHelper;
import com.espertech.esper.common.internal.util.CollectionUtil;
import com.espertech.esper.common.internal.util.JavaClassHelper;

import java.util.Collection;

public class ExprDotStaticMethodWrapArrayScalar implements ExprDotStaticMethodWrap {

    private final String methodName;
    private final EPTypeClass arrayType;

    public ExprDotStaticMethodWrapArrayScalar(String methodName, EPTypeClass arrayType) {
        this.methodName = methodName;
        this.arrayType = arrayType;
    }

    public EPChainableType getTypeInfo() {
        EPTypeClass component = JavaClassHelper.getArrayComponentType(arrayType);
        return EPChainableTypeHelper.collectionOfSingleValue(component);
    }

    public Collection convertNonNull(Object result) {
        return CollectionUtil.arrayToCollectionAllowNull(result);
    }

    public CodegenExpression codegenConvertNonNull(CodegenExpression result, CodegenMethodScope codegenMethodScope, CodegenClassScope codegenClassScope) {
        return CollectionUtil.arrayToCollectionAllowNullCodegen(codegenMethodScope, arrayType, result, codegenClassScope);
    }
}
