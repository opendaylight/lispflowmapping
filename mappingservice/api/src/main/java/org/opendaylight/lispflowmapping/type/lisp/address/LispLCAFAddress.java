/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.type.lisp.address;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.exception.LispMalformedPacketException;

public abstract class LispLCAFAddress extends LispAddress {

    protected LispCanonicalAddressFormatEnum lcafType;
    protected byte res2;

    protected LispLCAFAddress(ByteBuffer buffer) {
        super(AddressFamilyNumberEnum.LCAF);
    }

    public LispLCAFAddress(LispCanonicalAddressFormatEnum lcafType, byte res2) {
        super(AddressFamilyNumberEnum.LCAF);
        this.lcafType = lcafType;
        this.res2 = res2;
    }

    public static LispLCAFAddress valueOf(ByteBuffer buffer) {
        buffer.position(buffer.position() + Length.RES + Length.FLAGS);
        byte lispCode = buffer.get();
        LispCanonicalAddressFormatEnum lcafType = LispCanonicalAddressFormatEnum.valueOf(lispCode);
        byte res2 = buffer.get();
        short length = buffer.getShort();

        Class<? extends LispAddress> addressClass = lcafType.getLcafClass();
        if (addressClass == null) {
            throw new LispMalformedPacketException("Unknown LispLCAFAddress type=" + lispCode);
        }
        Method valueOfMethod;
        Throwable t = null;
        try {
            valueOfMethod = addressClass.getMethod("valueOf", byte.class, short.class, ByteBuffer.class);
            return (LispLCAFAddress) valueOfMethod.invoke(null, res2, length, buffer);
        } catch (NoSuchMethodException e) {
            t = e;
        } catch (SecurityException e) {
            t = e;
        } catch (IllegalAccessException e) {
            t = e;
        } catch (IllegalArgumentException e) {
            t = e;
        } catch (InvocationTargetException e) {
            t = e;
        }
        throw new LispMalformedPacketException("Couldn't parse LispLCAFAddress type=" + lispCode, t);
    }

    @Override
    public final int getAddressSize() {
        return super.getAddressSize() + Length.LCAF_HEADER + getLcafLength();
    }

    public abstract short getLcafLength();

    @Override
    protected void internalSerialize(ByteBuffer buffer) {
        super.internalSerialize(buffer);
        buffer.putShort((short) 0); // RES + Flags.
        buffer.put(lcafType.getLispCode());
        buffer.put(getRes2());
        buffer.putShort(getLcafLength());
    }

    public LispCanonicalAddressFormatEnum getType() {
        return lcafType;
    }

    public byte getRes2() {
        return res2;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((lcafType == null) ? 0 : lcafType.hashCode());
        result = prime * result + res2;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        LispLCAFAddress other = (LispLCAFAddress) obj;
        if (lcafType != other.lcafType)
            return false;
        if (res2 != other.res2)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "[lcafType=" + lcafType + ", res2=" + res2 + "]";
    }

    private interface Length {
        int RES = 1;
        int FLAGS = 1;

        int LCAF_HEADER = 6;
    }
}
