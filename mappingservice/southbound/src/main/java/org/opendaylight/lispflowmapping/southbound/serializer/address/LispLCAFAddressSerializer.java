package org.opendaylight.lispflowmapping.southbound.serializer.address;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.lispflowmapping.type.exception.LispMalformedPacketException;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispLCAFAddress;

public abstract class LispLCAFAddressSerializer extends LispAddressSerializer{
	

	public static LispLCAFAddress valueOf(ByteBuffer buffer) {
        buffer.position(buffer.position() + Length.RES + Length.FLAGS);
        byte lispCode = buffer.get();
        LispCanonicalAddressFormatEnum lcafType = LispCanonicalAddressFormatEnum.valueOf(lispCode);
        byte res2 = buffer.get();
        short length = buffer.getShort();

        Class<? extends LispAddressSerializer> addressClassserializer = LispAddressSerializerFactory.getSerializerClass(lcafType.getLcafClass());
        if (addressClassserializer == null) {
            throw new LispMalformedPacketException("Unknown LispLCAFAddress type=" + lispCode);
        }
        Method valueOfMethod;
        Throwable t = null;
        try {
            valueOfMethod = addressClassserializer.getMethod("valueOf", byte.class, short.class, ByteBuffer.class);
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
    public int getAddressSize(LispAddress lispAddress) {
        return super.getAddressSize(lispAddress) + Length.LCAF_HEADER + getLcafLength(lispAddress);
    }
    
    public abstract short getLcafLength(LispAddress lispAddress);

    @Override
    protected void internalSerialize(ByteBuffer buffer, LispAddress lispAddress) {
        super.internalSerialize(buffer, lispAddress);
        LispLCAFAddress lispLCAFAddress = (LispLCAFAddress)lispAddress;
        buffer.putShort((short) 0); // RES + Flags.
        buffer.put(lispLCAFAddress.getType().getLispCode());
        buffer.put(lispLCAFAddress.getRes2());
        buffer.putShort(getLcafLength(lispAddress));
    }
    private interface Length {
        int RES = 1;
        int FLAGS = 1;

        int LCAF_HEADER = 6;
    }
}
