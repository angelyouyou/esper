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
package com.espertech.esper.common.internal.epl.lookup;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.common.internal.epl.index.base.EventTable;
import com.espertech.esper.common.internal.epl.virtualdw.VirtualDWView;

/**
 * Strategy for looking up, in some sort of table or index, or a set of events, potentially based on the
 * events properties, and returning a set of matched events.
 */
public interface SubordTableLookupStrategyFactory {
    EPTypeClass EPTYPE = new EPTypeClass(SubordTableLookupStrategyFactory.class);

    SubordTableLookupStrategy makeStrategy(EventTable[] eventTable, ExprEvaluatorContext exprEvaluatorContext, VirtualDWView vdw);

    LookupStrategyDesc getLookupStrategyDesc();
}
