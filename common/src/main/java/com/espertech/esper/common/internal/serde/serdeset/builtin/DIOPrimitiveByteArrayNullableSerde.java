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
package com.espertech.esper.common.internal.serde.serdeset.builtin;

import com.espertech.esper.common.client.serde.DataInputOutputSerde;
import com.espertech.esper.common.client.serde.EventBeanCollatedWriter;
import com.espertech.esper.common.client.type.EPTypeClass;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DIOPrimitiveByteArrayNullableSerde implements DataInputOutputSerde<byte[]> {
    public final static EPTypeClass EPTYPE = new EPTypeClass(DIOPrimitiveByteArrayNullableSerde.class);

    public final static DIOPrimitiveByteArrayNullableSerde INSTANCE = new DIOPrimitiveByteArrayNullableSerde();

    private DIOPrimitiveByteArrayNullableSerde() {
    }

    public void write(byte[] object, DataOutput output) throws IOException {
        writeInternal(object, output);
    }

    public byte[] read(DataInput input) throws IOException {
        return readInternal(input);
    }

    public void write(byte[] object, DataOutput output, byte[] unitKey, EventBeanCollatedWriter writer) throws IOException {
        writeInternal(object, output);
    }

    public byte[] read(DataInput input, byte[] unitKey) throws IOException {
        return readInternal(input);
    }

    private void writeInternal(byte[] object, DataOutput output) throws IOException {
        if (object == null) {
            output.writeInt(-1);
            return;
        }
        output.writeInt(object.length);
        output.write(object);
    }

    private byte[] readInternal(DataInput input) throws IOException {
        int len = input.readInt();
        if (len == -1) {
            return null;
        }
        byte[] array = new byte[len];
        input.readFully(array);
        return array;
    }
}
