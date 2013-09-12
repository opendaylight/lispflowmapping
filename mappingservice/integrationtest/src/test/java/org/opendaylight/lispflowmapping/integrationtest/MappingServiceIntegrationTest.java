package org.opendaylight.lispflowmapping.integrationtest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackages;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.util.Arrays;

import javax.inject.Inject;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@RunWith(PaxExam.class)
public class MappingServiceIntegrationTest {

    // private IMapResolver mapResolver;
    // private IMapServer mapServer;
    private byte[] mapRequestPacket;
    private byte[] mapRegisterPacket;

    @Before
    public void before() throws Exception {
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

        mapRegisterPacket = extractWSUdpByteArray(new String("0000   00 50 56 ee d1 4f 00 0c 29 7a ce 79 08 00 45 00 " //
                + "0010   00 5c 00 00 40 00 40 11 d4 db c0 a8 88 0a 80 df "
                + "0020   9c 23 d6 40 10 f6 00 48 59 a4 38 00 01 01 00 00 "
                + "0030   00 00 00 00 00 00 00 01 00 14 0e a4 c6 d8 a4 06 "
                + "0040   71 7c 33 a4 5c 4a 83 1c de 74 53 03 0c ad 00 00 "
                + "0050   00 0a 01 20 10 00 00 00 00 01 99 10 fe 01 01 64 " //
                + "0060   ff 00 00 05 00 01 c0 a8 88 0a"));
    }

    @Inject
    private BundleContext bc;

    @Configuration
    public static Option[] configuration() {
        return options(
                systemProperty("logback.configurationFile").value("file:" + PathUtils.getBaseDir() + "/src/test/resources/logback.xml"),
                // To start OSGi console for inspection remotely
                systemProperty("osgi.console").value("2401"),
                // // Set the systemPackages (used by clustering)
                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.api"),
                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.implementation"),
                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.southbound"),
                junitBundles());
    }

    @Test
    public void bundleContextShouldNotBeNull() throws Exception {
        assertNotNull(bc);
    }

    @Test
    public void test() {
        fail("Not yet implemented");
    }

    @Test
    public void test2() {
        Assert.assertTrue(true);
        ;
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

}
