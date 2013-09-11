package org.opendaylight.lispflowmapping.implementation.serializer.address;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.opendaylight.lispflowmapping.implementation.serializer.address.factory.LispAFIAddressSerializerFactory;
import org.opendaylight.lispflowmapping.implementation.util.ByteUtil;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispTrafficEngineeringLCAFAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.ReencapHop;

public class LispTrafficEngineeringLCAFAddressSerializer extends LispLCAFAddressSerializer{
	
	private static final LispTrafficEngineeringLCAFAddressSerializer INSTANCE = new LispTrafficEngineeringLCAFAddressSerializer();

	// Private constructor prevents instantiation from other classes
	private LispTrafficEngineeringLCAFAddressSerializer() {
	}

	public static LispTrafficEngineeringLCAFAddressSerializer getInstance() {
		return INSTANCE;
	}


	@Override
    public short getLcafLength(LispAddress lispAddress) {
        short totalSize = 0;
        for (ReencapHop hop : ((LispTrafficEngineeringLCAFAddress)lispAddress).getHops()) {
            totalSize += LispAddressSerializer.getInstance().getAddressSize(hop) + 2;
        }
        return totalSize;
    }

	@Override
    public void serializeData(ByteBuffer buffer, LispAddress lispAddress) {
        for (ReencapHop hop : ((LispTrafficEngineeringLCAFAddress)lispAddress).getHops()) {
            serializeAFIAddressHeader(buffer, hop);
            buffer.put((byte)0);
            buffer.put((byte) (ByteUtil.boolToBit(hop.isLookup(), Flags.LOOKUP) | //
                    ByteUtil.boolToBit(hop.isRLOCProbe(), Flags.RLOC_PROBE) | //
                    ByteUtil.boolToBit(hop.isStrict(), Flags.STRICT)));
            LispAddressSerializer serializer = LispAFIAddressSerializerFactory.getSerializer(hop.getAfi());
            serializer.serializeData(buffer, hop.getHop());
        }
    }
	
	@Override
	public LispTrafficEngineeringLCAFAddress innerDeserialize(ByteBuffer buffer, byte res2, short length) {
        List<ReencapHop> hops = new ArrayList<ReencapHop>();
        while (length > 0) {
            short afi = buffer.getShort();
            LispAddressSerializer serializer = LispAFIAddressSerializerFactory.getSerializer(AddressFamilyNumberEnum.valueOf(afi));
            byte flags = (byte) buffer.getShort();
            boolean lookup = ByteUtil.extractBit(flags, Flags.LOOKUP);
            boolean RLOCProbe = ByteUtil.extractBit(flags, Flags.RLOC_PROBE);
            boolean strict = ByteUtil.extractBit(flags, Flags.STRICT);
            LispAddress address = serializer.deserializeData(buffer);
            ReencapHop hop = new ReencapHop(address, (short)0, lookup, RLOCProbe, strict);
            length -= LispAddressSerializer.getInstance().getAddressSize(hop) + 2;
            hops.add(hop);
        }
        return new LispTrafficEngineeringLCAFAddress(res2, hops);
    }
	
	private interface Flags {
	        int LOOKUP = 0x04;
	        int RLOC_PROBE = 0x02;
	        int STRICT = 0x01;
	    }
	
}
