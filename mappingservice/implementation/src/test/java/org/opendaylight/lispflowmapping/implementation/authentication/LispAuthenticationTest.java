package org.opendaylight.lispflowmapping.implementation.authentication;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;

import junitx.framework.ArrayAssert;

import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.serializer.LispAFIConvertor;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapnotifymessage.MapNotifyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;

public class LispAuthenticationTest extends BaseTestCase {

    @Test
    public void validate_WrongAuthentication() throws Exception {
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 1
        // Nonce: (something)
        // Key ID: 0x0001
        // AuthDataLength: 20 Data:
        // e8:f5:0b:c5:c5:f2:b0:21:27:a8:21:41:04:f3:46:5a:5a:5b:5c:5d
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight: 255/0
        //
        MapRegister mapRegister = MapRegisterSerializer.getInstance().deserialize(hexToByteBuffer("38 00 01 01 FF BB " //
                + "00 00 00 00 00 00 00 01 00 14 e8 f5 0b c5 c5 f2 " //
                + "b0 21 27 a8 21 41 04 f3 46 5a 5a 5b 5c 5d 00 00 " //
                + "00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "ff 00 00 05 00 01 c0 a8 88 0a"));

        assertFalse(LispAuthenticationUtil.validate(mapRegister, "password"));
    }

    @Test
    public void validate__SHA1() throws Exception {
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 1
        // Nonce: (something)
        // Key ID: 0x0001
        // AuthDataLength: 20 Data:
        // b2:dd:1a:25:c0:60:b1:46:e8:dc:6d:a6:ae:2e:92:92:a6:ca:b7:9d
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight: 255/0
        //
        MapRegister mapRegister = MapRegisterSerializer.getInstance().deserialize(hexToByteBuffer("38 00 01 01 FF BB " //
                + "00 00 00 00 00 00 00 01 00 14 7a aa bf 48 b9 07 " //
                + "45 a8 e6 ad 65 5d 91 7e 8b 8d a8 5d 48 40 00 00 " //
                + "00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "ff 00 00 05 00 01 c0 a8 88 0a"));

        assertTrue(LispAuthenticationUtil.validate(mapRegister, "password"));
        assertFalse(LispAuthenticationUtil.validate(mapRegister, "wrongPassword"));
    }

    @Test
    public void validate__SHA256() throws Exception {
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 1
        // Nonce: (something)
        // Key ID: 0x0002
        // AuthDataLength: 32 Data:
        // 70 30 d4 c6 10 44 0d 83 be 4d bf fd a9 8c 57 6d 68 a5 bf 32 11 c9 7b
        // 58 c4 b9 9f 06 11 23 b9 38
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight: 255/0
        //
        MapRegister mapRegister = MapRegisterSerializer.getInstance().deserialize(hexToByteBuffer("38 00 01 01 FF BB " //
                + "00 00 00 00 00 00 00 02 00 20 82 bb 21 d9 b6 5d c0 4b 97 " //
                + "35 00 22 9f fe b0 d6 56 9d cd 2c 2c 57 aa b9 2c 03 b5 7c 16 01 42 41 00 00 " //
                + "00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "ff 00 00 05 00 01 c0 a8 88 0a"));

        assertTrue(LispAuthenticationUtil.validate(mapRegister, "password"));
        assertFalse(LispAuthenticationUtil.validate(mapRegister, "wrongPassword"));
    }

    @Test
    public void validate__NoAuthentication() throws Exception {
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 1
        // Nonce: (something)
        // Key ID: 0x0000
        // AuthDataLength: 0:
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight: 255/0
        //
        MapRegister mapRegister = MapRegisterSerializer.getInstance().deserialize(hexToByteBuffer("38 00 01 01 FF BB " //
                + "00 00 00 00 00 00 00 00 00 00 " //
                + "00 00 " //
                + "00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "ff 00 00 05 00 01 c0 a8 88 0a"));

        assertTrue(LispAuthenticationUtil.validate(mapRegister, "password"));
        assertTrue(LispAuthenticationUtil.validate(mapRegister, "wrongPassword"));
    }

    // @Test
    // public void authenticate__MapNotifySHA1() throws Exception {
    // MapNotify mapNotify = new MapNotify();
    // mapNotify.addEidToLocator(new EidToLocatorRecord().setPrefix(new
    // LispIpv4Address(1)));
    //
    // mapNotify.addEidToLocator(new EidToLocatorRecord().setPrefix(new
    // LispIpv4Address(73)));
    // mapNotify.setNonce(6161616161L);
    // mapNotify.setKeyId((short) 0x0001);
    // byte[] wantedAuthenticationData = new byte[] { (byte) 0x66, (byte) 0x69,
    // (byte) 0x2c, (byte) 0xb8, (byte) 0xb8, (byte) 0x58, (byte) 0x7c,
    // (byte) 0x8f, (byte) 0x4c, (byte) 0xd4, (byte) 0x8b, (byte) 0x77, (byte)
    // 0x46, (byte) 0xf0, (byte) 0x6b, (byte) 0x9f, (byte) 0x66,
    // (byte) 0xd2, (byte) 0xaa, (byte) 0x2c };
    // ArrayAssert.assertEquals(wantedAuthenticationData,
    // LispAuthenticationUtil.createAuthenticationData(mapNotify, "password"));
    //
    // }
    //
    // @Test
    // public void authenticate__MapNotifySHA256() throws Exception {
    // MapNotify mapNotify = new MapNotify();
    // mapNotify.addEidToLocator(new EidToLocatorRecord().setPrefix(new
    // LispIpv4Address(1)));
    //
    // mapNotify.addEidToLocator(new EidToLocatorRecord().setPrefix(new
    // LispIpv4Address(73)));
    // mapNotify.setNonce(6161616161L);
    // mapNotify.setKeyId((short) 0x0002);
    // byte[] wantedAuthenticationData = new byte[] { (byte) 0x4c, (byte) 0xf1,
    // (byte) 0x5a, (byte) 0x4c, (byte) 0xdb, (byte) 0x8d, (byte) 0x88,
    // (byte) 0x47, (byte) 0xf1, (byte) 0x7f, (byte) 0x27, (byte) 0x81, (byte)
    // 0x1e, (byte) 0xbf, (byte) 0x22, (byte) 0xc7, (byte) 0xe6,
    // (byte) 0x70, (byte) 0x16, (byte) 0x5e, (byte) 0xa1, (byte) 0x59, (byte)
    // 0xe4, (byte) 0x06, (byte) 0x3f, (byte) 0xc2, (byte) 0x6a,
    // (byte) 0x1c, (byte) 0x86, (byte) 0xa5, (byte) 0x8d, (byte) 0x63 };
    // ArrayAssert.assertEquals(wantedAuthenticationData,
    // LispAuthenticationUtil.createAuthenticationData(mapNotify, "password"));
    //
    // }

    @Test
    public void authenticate__MapNotifyNoAuthenticationData() throws Exception {
        MapNotifyBuilder mapNotifyBuilder = new MapNotifyBuilder();
        mapNotifyBuilder.setKeyId((short) 0x0000);
        mapNotifyBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        EidToLocatorRecordBuilder etlrBuilder = new EidToLocatorRecordBuilder();
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(asIPAfiAddress("1.1.1.1")));
        etlrBuilder.setRecordTtl(55);
        mapNotifyBuilder.getEidToLocatorRecord().add(etlrBuilder.build());
        ArrayAssert.assertEquals(new byte[0], LispAuthenticationUtil.createAuthenticationData(mapNotifyBuilder.build(), "password"));

    }

    private Ipv4 asIPAfiAddress(String ip) {
        return new Ipv4Builder().setIpv4Address(new Ipv4Address(ip)).setAfi((short) AddressFamilyNumberEnum.IP.getIanaCode()).build();
    }
}
