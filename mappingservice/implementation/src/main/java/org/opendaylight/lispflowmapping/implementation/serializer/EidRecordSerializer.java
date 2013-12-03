package org.opendaylight.lispflowmapping.implementation.serializer;

import java.nio.ByteBuffer;

import org.opendaylight.lispflowmapping.implementation.serializer.address.LispAddressSerializer;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.Address;

public class EidRecordSerializer {

    private static final EidRecordSerializer INSTANCE = new EidRecordSerializer();

    // Private constructor prevents instantiation from other classes
    private EidRecordSerializer() {
    }

    public static EidRecordSerializer getInstance() {
        return INSTANCE;
    }

    public EidRecord deserialize(ByteBuffer requestBuffer) {
        /* byte reserved = */requestBuffer.get();
        short maskLength = (short) (ByteUtil.getUnsignedByte(requestBuffer));
        LispAFIAddress prefix = LispAddressSerializer.getInstance().deserialize(requestBuffer);
        return new EidRecordBuilder().setLispAddressContainer(new LispAddressContainerBuilder().setAddress((Address) prefix).build())
                .setMask(maskLength).build();
    }
}
