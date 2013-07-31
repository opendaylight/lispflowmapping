package org.opendaylight.lispflowmapping.southbound.serializer.address;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;

public abstract class LispAddressSerializer {
	

	abstract public void serialize(ByteBuffer buffer, LispAddress lispAddress);
	
    protected void internalSerialize(ByteBuffer buffer, LispAddress lispAddress) {
        buffer.putShort(lispAddress.getAfi().getIanaCode());
    }
    
    public int getAddressSize(LispAddress lispAddress) {
        return Length.AFI;
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
    
    private interface Length {
        int AFI = 2;
    }
}
