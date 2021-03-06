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
package com.espertech.esper.common.internal.epl.join.queryplan;

import com.espertech.esper.common.client.EventType;
import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.context.util.AgentInstanceContext;
import com.espertech.esper.common.internal.epl.index.base.EventTable;
import com.espertech.esper.common.internal.epl.join.exec.base.JoinExecTableLookupStrategy;
import com.espertech.esper.common.internal.epl.join.exec.base.TableLookupExecNode;
import com.espertech.esper.common.internal.epl.join.exec.base.TableLookupExecNodeTableLocking;
import com.espertech.esper.common.internal.epl.join.strategy.ExecNode;
import com.espertech.esper.common.internal.epl.virtualdw.VirtualDWView;
import com.espertech.esper.common.internal.util.IndentWriter;
import com.espertech.esper.common.internal.view.core.Viewable;

import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Specifies exection of a table lookup using the supplied plan for performing the lookup.
 */
public class TableLookupNode extends QueryPlanNode {
    public final static EPTypeClass EPTYPE = new EPTypeClass(TableLookupNode.class);

    private TableLookupPlan tableLookupPlan;

    /**
     * Ctor.
     *
     * @param tableLookupPlan - plan for performing lookup
     */
    public TableLookupNode(TableLookupPlan tableLookupPlan) {
        this.tableLookupPlan = tableLookupPlan;
    }

    public TableLookupPlan getTableLookupPlan() {
        return tableLookupPlan;
    }

    public ExecNode makeExec(AgentInstanceContext agentInstanceContext, Map<TableLookupIndexReqKey, EventTable>[] indexesPerStream, EventType[] streamTypes, Viewable[] streamViews, VirtualDWView[] viewExternal, Lock[] tableSecondaryIndexLocks) {
        JoinExecTableLookupStrategy lookupStrategy = tableLookupPlan.makeStrategy(agentInstanceContext, indexesPerStream, streamTypes, viewExternal);
        int indexedStream = tableLookupPlan.getIndexedStream();
        if (tableSecondaryIndexLocks[indexedStream] != null) {
            return new TableLookupExecNodeTableLocking(indexedStream, lookupStrategy, tableSecondaryIndexLocks[indexedStream]);
        }
        return new TableLookupExecNode(indexedStream, lookupStrategy);
    }

    /**
     * Returns lookup plan.
     *
     * @return lookup plan
     */
    protected TableLookupPlan getLookupStrategySpec() {
        return tableLookupPlan;
    }

    public void print(IndentWriter writer) {
        writer.println("TableLookupNode " +
                " tableLookupPlan=" + tableLookupPlan);
    }
}
