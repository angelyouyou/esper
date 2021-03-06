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
package com.espertech.esper.common.internal.epl.expression.time.adder;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.bytecodemodel.model.expression.CodegenExpression;

import java.util.Calendar;

public interface TimePeriodAdder {
    EPTypeClass EPTYPE = new EPTypeClass(TimePeriodAdder.class);

    double compute(Double value);

    void add(Calendar cal, int value);

    boolean isMicroseconds();

    CodegenExpression computeCodegen(CodegenExpression doubleValue);
}
