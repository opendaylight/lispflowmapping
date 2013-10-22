package org.opendaylight.lispflowmapping.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackages;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessage;
import org.opendaylight.lispflowmapping.implementation.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.type.lisp.EidRecord;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.MapReply;
import org.opendaylight.lispflowmapping.type.lisp.MapRequest;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispDistinguishedNameAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv6Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispListLCAFAddress;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
public class MappingServiceIntegrationTest {

    // private IMapResolver mapResolver;
    // private IMapServer mapServer;
    protected static final Logger logger = LoggerFactory.getLogger(MappingServiceIntegrationTest.class);
    private byte[] mapRequestPacket;
    private byte[] mapRegisterPacketWithNotify;
    private byte[] mapRegisterPacketWithoutNotify;
    int lispPortNumber = LispMessage.PORT_NUM;
    String lispBindAddress = "127.0.0.1";
    String ourAddress = "127.0.0.2";
    private DatagramSocket socket;
    private byte[] mapRegisterPacketWithAuthenticationAndMapNotify;
    private byte[] mapRegisterPacketWithNotifyWithListLCAFAndDistinguishedName;

    @After
    public void after() {
        if (socket != null) {
            socket.close();
        }
    }

    @Before
    public void before() throws Exception {
        socket = initSocket(socket);
        // mapResolver = context.mock(IMapResolver.class);
        // mapServer = context.mock(IMapServer.class);

        // SRC: 127.0.0.1:58560 to 127.0.0.1:4342
        // LISP(Type = 8 - Encapsulated)
        // IP: 192.168.136.10 -> 1.2.3.4
        // UDP: 56756
        // LISP(Type = 1 Map-Request
        // Record Count: 1
        // ITR-RLOC count: 0
        // Source EID AFI: 0
        // Source EID not present
        // Nonce: 0x3d8d2acd39c8d608
        // ITR-RLOC AFI=1 Address=192.168.136.10
        // Record 1: 1.2.3.4/32
        mapRequestPacket = extractWSUdpByteArray(new String("0000   00 00 00 00 00 00 00 00 00 00 00 00 08 00 45 00 " //
                + "0010   00 58 00 00 40 00 40 11 3c 93 7f 00 00 01 7f 00 "
                + "0020   00 01 e4 c0 10 f6 00 44 fe 57 80 00 00 00 45 00 "
                + "0030   00 38 d4 31 00 00 ff 11 56 f3 c0 a8 88 0a 01 02 "
                + "0040   03 04 dd b4 10 f6 00 24 ef 3a 10 00 00 01 3d 8d "
                + "0050   2a cd 39 c8 d6 08 00 00 00 01 c0 a8 88 0a 00 20 " //
                + "0060   00 01 01 02 03 04"));

        // IP: 192.168.136.10 -> 128.223.156.35
        // UDP: 49289 -> 4342
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 1
        // Nonce: 0
        // Key ID: 0x0001
        // AuthDataLength: 20 Data:
        // e8:f5:0b:c5:c5:f2:b0:21:27:a8:21:41:04:f3:46:5a:a5:68:89:ec
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight:
        // 255/0
        //

        mapRegisterPacketWithAuthenticationAndMapNotify = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 01 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 0e a4 c6 d8 a4 06 "
                + "0040   71 7c 33 a4 5c 4a 83 1c de 74 53 03 0c ad 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));

        // IP: 192.168.136.10 -> 128.223.156.35
        // UDP: 49289 -> 4342
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 1
        // Nonce: 7
        // Key ID: 0x0000 NO AUTHENTICATION!!
        // AuthDataLength: 00 Data:
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight:
        // 255/0
        //

        mapRegisterPacketWithNotify = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 01 01 00 00 "
                + "0030   00 00 00 00 00 07 00 00 00 14 0e a4 c6 d8 a4 06 "
                + "0040   71 7c 33 a4 5c 4a 83 1c de 74 53 03 0c ad 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));

        // IP: 192.168.136.10 -> 128.223.156.35
        // UDP: 49289 -> 4342
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 1
        // Nonce: 7
        // Key ID: 0x0000 NO AUTHENTICATION!!
        // AuthDataLength: 00 Data:
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // LIST LCAF
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Local RLOC: Distinguished Name("david"), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight:
        // 255/0
        //

        mapRegisterPacketWithNotifyWithListLCAFAndDistinguishedName = extractWSUdpByteArray(new String(
                "0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                        + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                        + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 01 01 00 00 "
                        + "0030   00 00 00 00 00 07 00 00 00 14 0e a4 c6 d8 a4 06 "
                        + "0040   71 7c 33 a4 5c 4a 83 1c de 74 53 03 0c ad 00 00 "
                        + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                        + "0060   ff 00 00 05 40 03 00 00 01 00 00 16 00 01 c0 a8 " //
                        + "0070   88 0a 40 03 00 00 01 00 00 08 00 11 64 61 76 69 " //
                        + "0080   64 00"));

        // IP: 192.168.136.10 -> 128.223.156.35
        // UDP: 49289 -> 4342
        // LISP(Type = 3 Map-Register, P=1, M=1
        // Record Counter: 1
        // Nonce: 7
        // Key ID: 0x0000 NO AUTHENTICATION!!
        // AuthDataLength: 00 Data:
        // EID prefix: 153.16.254.1/32 (EID=0x9910FE01), TTL: 10, Authoritative,
        // No-Action
        // Local RLOC: 192.168.136.10 (RLOC=0xC0A8880A), Reachable,
        // Priority/Weight: 1/100, Multicast Priority/Weight:
        // 255/0
        //

        mapRegisterPacketWithoutNotify = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 00 01 00 00 "
                + "0030   00 00 00 00 00 07 00 00 00 14 0e a4 c6 d8 a4 06 "
                + "0040   71 7c 33 a4 5c 4a 83 1c de 74 53 03 0c ad 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
    }

    @Inject
    private BundleContext bc;

    // Configure the OSGi container
    @Configuration
    public Option[] config() {
        return options(
                //
                systemProperty("logback.configurationFile").value("file:" + PathUtils.getBaseDir() + "/src/test/resources/logback.xml"),
                // To start OSGi console for inspection remotely
                systemProperty("osgi.console").value("2401"),
                // Set the systemPackages (used by clustering)
                systemPackages("sun.reflect", "sun.reflect.misc", "sun.misc", "javax.crypto", "javax.crypto.spec"),

                // OSGI infra
                mavenBundle("org.jboss.spec.javax.transaction", "jboss-transaction-api_1.1_spec").versionAsInProject(),
                mavenBundle("org.apache.commons", "commons-lang3").versionAsInProject(),
                mavenBundle("org.apache.felix", "org.apache.felix.dependencymanager").versionAsInProject(),

                // List logger bundles
                mavenBundle("org.slf4j", "slf4j-api").versionAsInProject(), mavenBundle("org.slf4j", "log4j-over-slf4j").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-core").versionAsInProject(), mavenBundle("ch.qos.logback", "logback-classic")
                        .versionAsInProject(),

                mavenBundle("commons-io", "commons-io").versionAsInProject(),

                mavenBundle("commons-fileupload", "commons-fileupload").versionAsInProject(),

                mavenBundle("equinoxSDK381", "javax.servlet").versionAsInProject(), mavenBundle("equinoxSDK381", "javax.servlet.jsp")
                        .versionAsInProject(), mavenBundle("equinoxSDK381", "org.eclipse.equinox.ds").versionAsInProject(),

                mavenBundle("equinoxSDK381", "org.eclipse.equinox.util").versionAsInProject(),
                mavenBundle("equinoxSDK381", "org.eclipse.osgi.services").versionAsInProject(),
                mavenBundle("equinoxSDK381", "org.apache.felix.gogo.command").versionAsInProject(),
                mavenBundle("equinoxSDK381", "org.apache.felix.gogo.runtime").versionAsInProject(),
                mavenBundle("equinoxSDK381", "org.apache.felix.gogo.shell").versionAsInProject(),
                mavenBundle("equinoxSDK381", "org.eclipse.equinox.cm").versionAsInProject(),
                mavenBundle("equinoxSDK381", "org.eclipse.equinox.console").versionAsInProject(),
                mavenBundle("equinoxSDK381", "org.eclipse.equinox.launcher").versionAsInProject(),

                mavenBundle("org.apache.felix", "org.apache.felix.dependencymanager").versionAsInProject(),
                mavenBundle("org.apache.felix", "org.apache.felix.dependencymanager.shell").versionAsInProject(),

                mavenBundle("com.google.code.gson", "gson").versionAsInProject(),
                mavenBundle("org.jboss.spec.javax.transaction", "jboss-transaction-api_1.1_spec").versionAsInProject(),
                mavenBundle("org.apache.felix", "org.apache.felix.fileinstall").versionAsInProject(),
                mavenBundle("org.apache.commons", "commons-lang3").versionAsInProject(), mavenBundle("commons-codec", "commons-codec")
                        .versionAsInProject(), mavenBundle("virgomirror", "org.eclipse.jdt.core.compiler.batch").versionAsInProject(),
                mavenBundle("eclipselink", "javax.persistence").versionAsInProject(), mavenBundle("eclipselink", "javax.resource")
                        .versionAsInProject(),

                mavenBundle("orbit", "javax.activation").versionAsInProject(),
                mavenBundle("orbit", "javax.annotation").versionAsInProject(),
                mavenBundle("orbit", "javax.ejb").versionAsInProject(),
                mavenBundle("orbit", "javax.el").versionAsInProject(),
                mavenBundle("orbit", "javax.mail.glassfish").versionAsInProject(),
                mavenBundle("orbit", "javax.xml.rpc").versionAsInProject(),
                mavenBundle("orbit", "org.apache.catalina").versionAsInProject(),

                mavenBundle("orbit", "org.apache.catalina.ha").versionAsInProject().noStart(),
                mavenBundle("orbit", "org.apache.catalina.tribes").versionAsInProject().noStart(),
                mavenBundle("orbit", "org.apache.coyote").versionAsInProject().noStart(),
                mavenBundle("orbit", "org.apache.jasper").versionAsInProject().noStart(),

                mavenBundle("orbit", "org.apache.el").versionAsInProject(),
                mavenBundle("orbit", "org.apache.juli.extras").versionAsInProject(),
                mavenBundle("orbit", "org.apache.tomcat.api").versionAsInProject(),
                mavenBundle("orbit", "org.apache.tomcat.util").versionAsInProject().noStart(),
                mavenBundle("orbit", "javax.servlet.jsp.jstl").versionAsInProject(),
                mavenBundle("orbit", "javax.servlet.jsp.jstl.impl").versionAsInProject(),

                // Basic bundles needed
                mavenBundle("org.opendaylight.controller", "containermanager").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "containermanager.it.implementation").versionAsInProject(),

                // Specific bundles
                mavenBundle("org.opendaylight.controller", "clustering.services").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "clustering.stub").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "clustering.services").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "sal").versionAsInProject(),
                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.api").versionAsInProject(),
                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.implementation").versionAsInProject(),
                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.southbound").versionAsInProject(), junitBundles());
    }

    @Test
    public void mapRegisterWithMapNotify() throws SocketTimeoutException {
        sendPacket(mapRegisterPacketWithNotify);
        MapNotify reply = recieveMapNotify();
        assertEquals(7, reply.getNonce());
    }

    @Test
    public void mapRegisterWithMapNotifyAndMapRequest() throws SocketTimeoutException {
        MapRegister mapRegister = new MapRegister();
        mapRegister.setWantMapNotify(true);
        mapRegister.setNonce(8);
        EidToLocatorRecord etlr = new EidToLocatorRecord();
        LispIpv4Address eid = new LispIpv4Address("1.2.3.4");
        etlr.setPrefix(eid);
        etlr.setMaskLength(32);
        etlr.setRecordTtl(254);
        LocatorRecord record = new LocatorRecord();
        record.setLocator(new LispIpv4Address("4.3.2.1"));
        etlr.addLocator(record);
        mapRegister.addEidToLocator(etlr);
        sendMapRegister(mapRegister);
        MapNotify mapNotify = recieveMapNotify();
        assertEquals(8, mapNotify.getNonce());
        MapRequest mapRequest = new MapRequest();
        mapRequest.setNonce(4);
        mapRequest.addEidRecord(new EidRecord((byte) 32, eid));
        mapRequest.addItrRloc(new LispIpv4Address(ourAddress));
        sendMapRequest(mapRequest);
        MapReply mapReply = recieveMapReply();
        assertEquals(4, mapReply.getNonce());
        assertEquals(record.getLocator(), mapReply.getEidToLocatorRecords().get(0).getLocators().get(0).getLocator());

    }

    @Test
    public void eidPrefixLookupIPv4() throws SocketTimeoutException {
        runPrefixTest(new LispIpv4Address("1.2.3.4"), 16, new LispIpv4Address("1.2.3.2"), new LispIpv4Address("1.1.1.1"), (byte) 32);
    }

    @Test
    public void eidPrefixLookupIPv6() throws SocketTimeoutException {
        runPrefixTest(new LispIpv6Address("1:2:3:4:5:6:7:8"), 64, new LispIpv6Address("1:2:3:4:5:1:2:3"), new LispIpv6Address("1:2:3:1:2:3:1:2"),
                (byte) 128);
    }

    private void runPrefixTest(LispAddress registerEID, int registerdMask, LispAddress matchedAddress, LispAddress unMatchedAddress, byte mask)
            throws SocketTimeoutException {
        MapRegister mapRegister = new MapRegister();
        mapRegister.setWantMapNotify(true);
        mapRegister.setNonce(8);
        EidToLocatorRecord etlr = new EidToLocatorRecord();
        etlr.setPrefix(registerEID);
        etlr.setMaskLength(registerdMask);
        etlr.setRecordTtl(254);
        LocatorRecord record = new LocatorRecord();
        record.setLocator(new LispIpv4Address("4.3.2.1"));
        etlr.addLocator(record);
        mapRegister.addEidToLocator(etlr);
        sendMapRegister(mapRegister);
        MapNotify mapNotify = recieveMapNotify();
        assertEquals(8, mapNotify.getNonce());
        MapRequest mapRequest = new MapRequest();
        mapRequest.setNonce(4);
        mapRequest.addEidRecord(new EidRecord(mask, matchedAddress));
        mapRequest.addItrRloc(new LispIpv4Address(ourAddress));
        sendMapRequest(mapRequest);
        MapReply mapReply = recieveMapReply();
        assertEquals(4, mapReply.getNonce());
        assertEquals(record.getLocator(), mapReply.getEidToLocatorRecords().get(0).getLocators().get(0).getLocator());
        mapRequest.getEids().get(0).setPrefix(unMatchedAddress);
        sendMapRequest(mapRequest);
        mapReply = recieveMapReply();
        assertEquals(0, mapReply.getEidToLocatorRecords().get(0).getLocators().size());
    }

    public void mapRequestMapRegisterAndMapRequest() throws SocketTimeoutException {

        LispIpv4Address eid = new LispIpv4Address("1.2.3.4");
        MapRequest mapRequest = new MapRequest();
        mapRequest.setNonce(4);
        mapRequest.addEidRecord(new EidRecord((byte) 32, eid));
        mapRequest.addItrRloc(new LispIpv4Address(ourAddress));
        sendMapRequest(mapRequest);
        MapReply mapReply = recieveMapReply();
        assertEquals(4, mapReply.getNonce());
        assertEquals(0, mapReply.getEidToLocatorRecords().get(0).getLocators().size());
        MapRegister mapRegister = new MapRegister();
        mapRegister.setWantMapNotify(true);
        mapRegister.setNonce(8);
        EidToLocatorRecord etlr = new EidToLocatorRecord();
        etlr.setPrefix(eid);
        etlr.setMaskLength(32);
        LocatorRecord record = new LocatorRecord();
        record.setLocator(new LispIpv4Address("4.3.2.1"));
        etlr.addLocator(record);
        mapRegister.addEidToLocator(etlr);
        sendMapRegister(mapRegister);
        MapNotify mapNotify = recieveMapNotify();
        assertEquals(8, mapNotify.getNonce());
        sendMapRequest(mapRequest);
        mapReply = recieveMapReply();
        assertEquals(4, mapReply.getNonce());
        assertEquals(record.getLocator(), mapReply.getEidToLocatorRecords().get(0).getLocators().get(0).getLocator());

    }

    private MapReply recieveMapReply() throws SocketTimeoutException {
        return MapReplySerializer.getInstance().deserialize(ByteBuffer.wrap(receivePacket().getData()));
    }

    private MapNotify recieveMapNotify() throws SocketTimeoutException {
        return MapNotifySerializer.getInstance().deserialize(ByteBuffer.wrap(receivePacket().getData()));
    }

    private void sendMapRequest(MapRequest mapRequest) {
        sendPacket(MapRequestSerializer.getInstance().serialize(mapRequest).array());
    }

    private void sendMapRegister(MapRegister mapRegister) {
        sendPacket(MapRegisterSerializer.getInstance().serialize(mapRegister).array());
    }

    @Test
    public void mapRegisterWithAuthenticationWithoutConfiguringAKey() throws SocketTimeoutException {
        sendPacket(mapRegisterPacketWithAuthenticationAndMapNotify);
        try {
            receivePacket(3000);
            // If didn't timeout then fail:
            fail();
        } catch (SocketTimeoutException ste) {
        }
    }

    @Test
    public void mapRegisterWithMapNotifyWithListLcaf() throws SocketTimeoutException {
        sendPacket(mapRegisterPacketWithNotifyWithListLCAFAndDistinguishedName);
        ByteBuffer readBuf = ByteBuffer.wrap(receivePacket().getData());
        MapNotify reply = MapNotifySerializer.getInstance().deserialize(readBuf);
        EidToLocatorRecord etlr = reply.getEidToLocatorRecords().get(0);
        List<LocatorRecord> locators = etlr.getLocators();
        assertEquals(true, (locators.get(0).getLocator() instanceof LispListLCAFAddress));
        LispListLCAFAddress listLCAF = (LispListLCAFAddress) locators.get(0).getLocator();
        LispListLCAFAddress innerList = (LispListLCAFAddress) listLCAF.getAddresses().get(1);
        LispDistinguishedNameAddress dn = new LispDistinguishedNameAddress("david");
        assertEquals(dn, ((LispDistinguishedNameAddress) innerList.getAddresses().get(0)));
        assertEquals(7, reply.getNonce());
    }

    @Test
    public void mapRegisterWithoutMapNotify() {
        sendPacket(mapRegisterPacketWithoutNotify);
        try {
            receivePacket(3000);
            // If didn't timeout then fail:
            fail();
        } catch (SocketTimeoutException ste) {
        }
    }

    private void sendPacket(byte[] bytesToSend) {
        try {
            DatagramPacket packet = new DatagramPacket(bytesToSend, bytesToSend.length);
            initPacketAddress(packet);
            logger.info("Sending MapRegister to LispPlugin on socket");
            socket.send(packet);
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }

    private DatagramPacket receivePacket() throws SocketTimeoutException {
        return receivePacket(5000);
    }

    private DatagramPacket receivePacket(int timeout) throws SocketTimeoutException {
        try {
            byte[] buffer = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            logger.info("Waiting for packet from socket...");
            socket.setSoTimeout(timeout);
            socket.receive(receivePacket);
            logger.info("Recieved packet from socket!");
            return receivePacket;
        } catch (SocketTimeoutException ste) {
            throw ste;
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
            return null;
        }
    }

    private void initPacketAddress(DatagramPacket packet) throws UnknownHostException {
        packet.setAddress(InetAddress.getByName(lispBindAddress));
        packet.setPort(lispPortNumber);
    }

    private DatagramSocket initSocket(DatagramSocket socket) {
        try {
            socket = new DatagramSocket(new InetSocketAddress(ourAddress, LispMessage.PORT_NUM));
        } catch (SocketException e) {
            e.printStackTrace();
            fail();
        }
        return socket;
    }

    private byte[] extractWSUdpByteArray(String wiresharkHex) {
        final int HEADER_LEN = 42;
        byte[] res = new byte[1000];
        String[] split = wiresharkHex.split(" ");
        int counter = 0;
        for (String cur : split) {
            cur = cur.trim();
            if (cur.length() == 2) {
                ++counter;
                if (counter > HEADER_LEN) {
                    res[counter - HEADER_LEN - 1] = (byte) Integer.parseInt(cur, 16);
                }

            }
        }
        return Arrays.copyOf(res, counter - HEADER_LEN);
    }

    private String stateToString(int state) {
        switch (state) {
        case Bundle.ACTIVE:
            return "ACTIVE";
        case Bundle.INSTALLED:
            return "INSTALLED";
        case Bundle.RESOLVED:
            return "RESOLVED";
        case Bundle.UNINSTALLED:
            return "UNINSTALLED";
        default:
            return "Not CONVERTED";
        }
    }

    @Before
    public void areWeReady() throws InvalidSyntaxException {
        assertNotNull(bc);
        boolean debugit = false;
        Bundle b[] = bc.getBundles();
        for (Bundle element : b) {
            int state = element.getState();
            if (state != Bundle.ACTIVE && state != Bundle.RESOLVED) {
                System.out.println("Bundle:" + element.getSymbolicName() + " state:" + stateToString(state));
                /*
                 * UNCOMMENT to see why bundles didn't resolve! try { String
                 * host = element.getHeaders().get(Constants.FRAGMENT_HOST); if
                 * (host != null) { logger.warn("Bundle " +
                 * element.getSymbolicName() +
                 * " is a fragment which is part of: " + host);
                 * logger.warn("Required imports are: " +
                 * element.getHeaders().get(Constants.IMPORT_PACKAGE)); } else {
                 * element.start(); } } catch (BundleException e) {
                 * logger.error("BundleException:", e); fail(); }
                 */
                debugit = true;
            }
        }
        if (debugit) {
            logger.warn(("Do some debugging because some bundle is unresolved"));
        }

        // Uncomment this code to Know which services were actually loaded to
        // BundleContext
        /*
         * for (ServiceReference sr : bc.getAllServiceReferences(null, null)) {
         * logger.info(sr.getBundle().getSymbolicName());
         * logger.info(sr.toString()); }
         */
    }
}
