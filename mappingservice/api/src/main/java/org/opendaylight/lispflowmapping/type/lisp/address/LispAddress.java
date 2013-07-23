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
import org.opendaylight.lispflowmapping.type.exception.LispMalformedPacketException;

public abstract class LispAddress {
    private AddressFamilyNumberEnum afi;

    protected LispAddress(AddressFamilyNumberEnum afi) {
        this.afi = afi;
    }

    public AddressFamilyNumberEnum getAfi() {
        return afi;
    }

    public int getAddressSize() {
        return Length.AFI;
    }

    abstract public void serialize(ByteBuffer buffer);

    protected void internalSerialize(ByteBuffer buffer) {
        buffer.putShort(getAfi().getIanaCode());
    }

    public static LispAddress valueOf(ByteBuffer buffer) {
        short afi = buffer.getShort();
        AddressFamilyNumberEnum afiType = AddressFamilyNumberEnum.valueOf(afi);
        Class<? extends LispAddress> addressClass = afiType.getLispAddressClass();
        Throwable t = null;
        try {
            Method valueOfMethod = addressClass.getMethod("valueOf", ByteBuffer.class);
            return (LispAddress) valueOfMethod.invoke(null, buffer);
        } catch (NoSuchMethodException e) {
            t = e;
        } catch (SecurityException e) {
            t = e;
        } catch (ClassCastException e) {
            t = e;
        } catch (IllegalAccessException e) {
            t = e;
        } catch (IllegalArgumentException e) {
            t = e;
        } catch (InvocationTargetException e) {
            t = e;
        }
        throw new LispMalformedPacketException("Couldn't parse LispAddress afi=" + afi, t);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((afi == null) ? 0 : afi.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LispAddress other = (LispAddress) obj;
        if (afi != other.afi)
            return false;
        return true;
    }

    private interface Length {
        int AFI = 2;
    }
}
