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
package com.espertech.esper.common.internal.collection;

import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.type.EPTypeClass;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A utility class for an iterator that has one element.
 */
public class SingleEventIterator implements Iterator<EventBean> {
    public final static EPTypeClass EPTYPE = new EPTypeClass(SingleEventIterator.class);

    private EventBean eventBean;
    private boolean hasMore;

    /**
     * Constructor, takes the single event to iterate over as a parameter.
     * The single event can be null indicating that there are no more elements.
     *
     * @param eventBean single bean that the iterator returns, or null for an empty iterator
     */
    public SingleEventIterator(EventBean eventBean) {
        if (eventBean == null) {
            hasMore = false;
        } else {
            this.eventBean = eventBean;
            this.hasMore = true;
        }
    }

    public boolean hasNext() {
        return hasMore;
    }

    public EventBean next() {
        if (!hasMore) {
            throw new NoSuchElementException();
        }
        hasMore = false;
        return eventBean;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}

