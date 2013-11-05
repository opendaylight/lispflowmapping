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
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

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
//import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
//import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
//import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
//import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapReply;
//import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.lib.osgi.Constants;

@RunWith(PaxExam.class)
public class MappingServiceIntegrationTest {

    // private IFlowMapping lms;
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

    public static final String ODL = "org.opendaylight.controller";
    public static final String YANG = "org.opendaylight.yangtools";
    public static final String JERSEY = "com.sun.jersey";

    @After
    public void after() {
        if (socket != null) {
            socket.close();
        }
        if (connection != null) {
            connection.disconnect();
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
                + "0030   00 38 d4 31 00 00 ff 11 56 f3 7f 00 00 02 01 02 "
                + "0040   03 04 dd b4 10 f6 00 24 ef 3a 10 00 00 01 3d 8d "
                + "0050   2a cd 39 c8 d6 08 00 00 00 01 7f 00 00 02 00 20 " //
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
    private HttpURLConnection connection;

    // Configure the OSGi container
    @Configuration
    public Option[] config() {
        return options(
                //
                systemProperty("logback.configurationFile").value("file:" + PathUtils.getBaseDir() + "/src/test/resources/logback.xml"),
                // To start OSGi console for inspection remotely
                systemProperty("osgi.console").value("2401"),
                systemProperty("org.eclipse.gemini.web.tomcat.config.path").value(PathUtils.getBaseDir() + "/src/test/resources/tomcat-server.xml"),

                // setting default level. Jersey bundles will need to be started
                // earlier.
                systemProperty("osgi.bundles.defaultStartLevel").value("4"),

                // Set the systemPackages (used by clustering)
                systemPackages("sun.reflect", "sun.reflect.misc", "sun.misc", "javax.crypto", "javax.crypto.spec"),

                // OSGI infra
                mavenBundle("org.jboss.spec.javax.transaction", "jboss-transaction-api_1.1_spec").versionAsInProject(),
                mavenBundle("org.apache.commons", "commons-lang3").versionAsInProject(),
                mavenBundle("org.apache.felix", "org.apache.felix.dependencymanager").versionAsInProject(),

                // List logger bundles
                mavenBundle("org.slf4j", "jcl-over-slf4j").versionAsInProject(),
                mavenBundle("org.slf4j", "slf4j-api").versionAsInProject(),
                mavenBundle("org.slf4j", "log4j-over-slf4j").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-core").versionAsInProject(),
                mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject(),

                mavenBundle(ODL, "config-api").versionAsInProject(), //
                mavenBundle(ODL, "config-manager").versionAsInProject(), //
                mavenBundle("commons-io", "commons-io").versionAsInProject(),

                mavenBundle("commons-fileupload", "commons-fileupload").versionAsInProject(),

                mavenBundle("equinoxSDK381", "javax.servlet").versionAsInProject(),
                mavenBundle("equinoxSDK381", "javax.servlet.jsp").versionAsInProject(),
                mavenBundle("equinoxSDK381", "org.eclipse.equinox.ds").versionAsInProject(),

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
                mavenBundle("org.apache.commons", "commons-lang3").versionAsInProject(),
                mavenBundle("commons-codec", "commons-codec").versionAsInProject(),
                mavenBundle("virgomirror", "org.eclipse.jdt.core.compiler.batch").versionAsInProject(),
                mavenBundle("eclipselink", "javax.persistence").versionAsInProject(),
                mavenBundle("eclipselink", "javax.resource").versionAsInProject(),

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
                mavenBundle(ODL, "sal-binding-api").versionAsInProject(), //
                mavenBundle(ODL, "sal-binding-config").versionAsInProject(),
                mavenBundle(ODL, "sal-binding-broker-impl").versionAsInProject(), //
                mavenBundle(ODL, "sal-common").versionAsInProject(), //
                mavenBundle(ODL, "sal-common-api").versionAsInProject(), //
                mavenBundle(ODL, "sal-common-impl").versionAsInProject(),
                mavenBundle(ODL, "sal-common-util").versionAsInProject(), //

                mavenBundle(YANG, "concepts").versionAsInProject(),
                mavenBundle(YANG, "yang-binding").versionAsInProject(), //
                mavenBundle(YANG, "yang-common").versionAsInProject(), //
                mavenBundle(YANG + ".model", "ietf-inet-types").versionAsInProject(),//
                mavenBundle(YANG + ".model", "ietf-yang-types").versionAsInProject(),//
                mavenBundle(YANG + ".thirdparty", "xtend-lib-osgi").versionAsInProject(),//
                mavenBundle(YANG, "yang-data-api").versionAsInProject(), //
                mavenBundle(YANG, "yang-data-impl").versionAsInProject(), //
                mavenBundle(YANG, "yang-model-api").versionAsInProject(), //
                mavenBundle(YANG, "yang-model-util").versionAsInProject(), //
                mavenBundle(YANG, "yang-parser-api").versionAsInProject(),
                mavenBundle(YANG, "yang-parser-impl").versionAsInProject(),
                mavenBundle(YANG, "binding-generator-spi").versionAsInProject(), //
                mavenBundle(YANG, "binding-model-api").versionAsInProject(), //
                mavenBundle(YANG, "binding-generator-util").versionAsInProject(),
                mavenBundle(YANG, "yang-parser-impl").versionAsInProject(),
                mavenBundle(YANG, "binding-type-provider").versionAsInProject(),
                mavenBundle(YANG, "binding-generator-api").versionAsInProject(),
                mavenBundle(YANG, "binding-generator-spi").versionAsInProject(),
                mavenBundle(YANG, "binding-generator-impl").versionAsInProject(),
                mavenBundle(YANG + ".thirdparty", "antlr4-runtime-osgi-nohead").versionAsInProject(), //

                mavenBundle("com.google.guava", "guava").versionAsInProject(), //
                mavenBundle("org.javassist", "javassist").versionAsInProject(), //

                // Northbound bundles
                mavenBundle("org.opendaylight.controller", "commons.northbound").versionAsInProject(), //
                mavenBundle(ODL + ".thirdparty", "com.sun.jersey.jersey-servlet").versionAsInProject(), //
                mavenBundle(ODL + ".thirdparty", "net.sf.jung2").versionAsInProject(), //
                mavenBundle(ODL + ".thirdparty", "org.apache.catalina.filters.CorsFilter").versionAsInProject().noStart(),
                mavenBundle(JERSEY, "jersey-client").versionAsInProject(),
                mavenBundle(JERSEY, "jersey-server").versionAsInProject().startLevel(2),
                mavenBundle(JERSEY, "jersey-core").versionAsInProject().startLevel(2),
                mavenBundle(JERSEY, "jersey-json").versionAsInProject().startLevel(2),
                mavenBundle("org.codehaus.jackson", "jackson-mapper-asl").versionAsInProject(),//
                mavenBundle("org.codehaus.jackson", "jackson-core-asl").versionAsInProject(),//
                mavenBundle("org.codehaus.jackson", "jackson-jaxrs").versionAsInProject(),//
                mavenBundle("org.codehaus.jackson", "jackson-xc").versionAsInProject(),//
                mavenBundle("org.codehaus.jettison", "jettison").versionAsInProject(),//
                mavenBundle("org.ow2.asm", "asm-all").versionAsInProject(), //
                mavenBundle("org.opendaylight.controller", "bundlescanner").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "bundlescanner.implementation").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "switchmanager").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "connectionmanager").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "connectionmanager.implementation").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "commons.httpclient").versionAsInProject(), //
                mavenBundle("org.opendaylight.controller", "configuration").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "configuration.implementation").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "usermanager").versionAsInProject(), //
                mavenBundle("org.opendaylight.controller", "usermanager.implementation").versionAsInProject(), //
                mavenBundle("org.springframework", "org.springframework.asm").versionAsInProject(),
                mavenBundle("org.springframework", "org.springframework.aop").versionAsInProject(),
                mavenBundle("org.springframework", "org.springframework.context").versionAsInProject(),
                mavenBundle("org.springframework", "org.springframework.context.support").versionAsInProject(),
                mavenBundle("org.springframework", "org.springframework.core").versionAsInProject(),
                mavenBundle("org.springframework", "org.springframework.beans").versionAsInProject(),
                mavenBundle("org.springframework", "org.springframework.expression").versionAsInProject(),
                mavenBundle("org.springframework", "org.springframework.web").versionAsInProject(),

                mavenBundle("org.aopalliance", "com.springsource.org.aopalliance").versionAsInProject(),
                mavenBundle("org.springframework", "org.springframework.web.servlet").versionAsInProject(),
                mavenBundle("org.springframework.security", "spring-security-config").versionAsInProject(),
                mavenBundle("org.springframework.security", "spring-security-core").versionAsInProject(),
                mavenBundle("org.springframework.security", "spring-security-web").versionAsInProject(),
                mavenBundle("org.springframework.security", "spring-security-taglibs").versionAsInProject(),
                mavenBundle("org.springframework", "org.springframework.transaction").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "sal.connection").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "sal.connection.implementation").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "security").versionAsInProject().noStart(),

                // Tomcat for northbound
                mavenBundle("geminiweb", "org.eclipse.gemini.web.core").versionAsInProject(),
                mavenBundle("geminiweb", "org.eclipse.gemini.web.extender").versionAsInProject(),
                mavenBundle("geminiweb", "org.eclipse.gemini.web.tomcat").versionAsInProject(),
                mavenBundle("geminiweb", "org.eclipse.virgo.kernel.equinox.extensions").versionAsInProject().noStart(),
                mavenBundle("geminiweb", "org.eclipse.virgo.util.common").versionAsInProject(),
                mavenBundle("geminiweb", "org.eclipse.virgo.util.io").versionAsInProject(),
                mavenBundle("geminiweb", "org.eclipse.virgo.util.math").versionAsInProject(),
                mavenBundle("geminiweb", "org.eclipse.virgo.util.osgi").versionAsInProject(),
                mavenBundle("geminiweb", "org.eclipse.virgo.util.osgi.manifest").versionAsInProject(),
                mavenBundle("geminiweb", "org.eclipse.virgo.util.parser.manifest").versionAsInProject(),

                // Our bundles
                mavenBundle("org.opendaylight.controller", "clustering.stub").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "clustering.services").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "sal").versionAsInProject(),
                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.yangmodel").versionAsInProject(),
                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.api").versionAsInProject(),
                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.implementation").versionAsInProject(), //
                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.southbound").versionAsInProject(), //
                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.northbound").versionAsInProject(), //

                // Additions
                mavenBundle(ODL, "sal-core-api").versionAsInProject().update(), //
                mavenBundle(ODL, "sal-core-spi").versionAsInProject().update(), //
                mavenBundle(ODL, "sal-broker-impl").versionAsInProject(), //
                mavenBundle(ODL, "sal-connector-api").versionAsInProject(), //
                junitBundles());
    }

    // @Test
    // public void mapRequestSimple() throws SocketTimeoutException {
    // sendPacket(mapRequestPacket);
    // ByteBuffer readBuf = ByteBuffer.wrap(receivePacket().getData());
    // MapReply reply = MapReplySerializer.getInstance().deserialize(readBuf);
    // assertEquals(4435248268955932168L, reply.getNonce().longValue());
    //
    // }

    // @Test
    // public void mapRegisterWithMapNotify() throws SocketTimeoutException {
    // sendPacket(mapRegisterPacketWithNotify);
    // MapNotify reply = recieveMapNotify();
    // assertEquals(7, reply.getNounce().longValue());
    // }

    // @Test
    // public void northboundAddKey() throws Exception {
    //
    // LispIpv4Address address = new LispIpv4Address("10.0.0.1");
    // int mask = 32;
    // String pass = "asdf";
    //
    // URL url = createPutURL("key");
    // String authKeyJSON = createAuthKeyJSON(pass, address, mask);
    // callURL("PUT", "application/json", "text/plain", authKeyJSON, url);
    //
    // String retrievedKey = lms.getAuthenticationKey(address, mask);
    //
    // // Check stored password matches the one sent
    // assertEquals(pass, retrievedKey);
    //
    // }
    //
    // @Test
    // public void northboundRetrieveKey() throws Exception {
    //
    // LispIpv4Address address = new LispIpv4Address("10.0.0.1");
    // int mask = 32;
    // String pass = "asdf";
    //
    // lms.addAuthenticationKey(address, mask, pass);
    //
    // URL url = createGetKeyIPv4URL(address, mask);
    // String reply = callURL("GET", null, "application/json", null, url);
    // JSONTokener jt = new JSONTokener(reply);
    // JSONObject json = new JSONObject(jt);
    //
    // // test that the password matches what was we expected.
    // assertEquals(pass, json.get("key"));
    //
    // }
    //
    // private String createAuthKeyJSON(String key, LispIpv4Address address, int
    // mask) {
    // return "{\"key\" : \"" + key + "\",\"maskLength\" : " + mask +
    // ",\"address\" : " + "{\"ipAddress\" : \""
    // + address.getAddress().getHostAddress() + "\",\"afi\" : " +
    // address.getAfi().getIanaCode() + "}}";
    // }
    //
    // @Test
    // public void northboundAddMapping() throws Exception {
    //
    // String pass = "asdf";
    // LispIpv4Address eid = new LispIpv4Address("10.0.0.1");
    // int mask = 32;
    // LispIpv4Address rloc = new LispIpv4Address("20.0.0.2");
    //
    // // NB add mapping always checks the key
    // lms.addAuthenticationKey(eid, mask, pass);
    //
    // URL url = createPutURL("mapping");
    // String mapRegisterJSON = createMapRegisterJSON(pass, eid, mask, rloc);
    // callURL("PUT", "application/json", "text/plain", mapRegisterJSON, url);
    //
    // // Retrieve the RLOC from the database
    // MapRequest mapRequest = new MapRequest();
    // mapRequest.addEidRecord(new EidRecord((byte) mask, eid));
    // MapReply mapReply = lms.handleMapRequest(mapRequest);
    //
    // LispIpv4Address retrievedRloc = (LispIpv4Address)
    // mapReply.getEidToLocatorRecords().get(0).getLocators().get(0).getLocator();
    //
    // assertEquals(rloc.getAddress().getHostAddress(),
    // retrievedRloc.getAddress().getHostAddress());
    //
    // }
    //
    // private String createMapRegisterJSON(String key, LispIpv4Address eid, int
    // mask, LispIpv4Address rloc) {
    // String jsonString = "{ " + "\"key\" : \"" + key + "\"," +
    // "\"mapregister\" : " + "{ " + "\"wantMapNotify\" : true,"
    // + "\"proxyMapReply\" : false, " + "\"eidToLocatorRecords\" : " + "[ " +
    // "{ " + "\"authoritative\" : true," + "\"prefixGeneric\" : "
    // + "{ " + "\"ipAddress\" : \"" + eid.getAddress().getHostAddress() + "\","
    // + "\"afi\" : " + eid.getAfi().getIanaCode() + "},"
    // + "\"mapVersion\" : 0," + "\"maskLength\" : " + mask + ", " +
    // "\"action\" : \"NoAction\"," + "\"locators\" : " + "[ " + "{ "
    // + "\"multicastPriority\" : 1," + "\"locatorGeneric\" : " + "{ " +
    // "\"ipAddress\" : \"" + rloc.getAddress().getHostAddress() + "\","
    // + "\"afi\" : " + rloc.getAfi().getIanaCode() + "}, " +
    // "\"routed\" : true," + "\"multicastWeight\" : 50,"
    // + "\"rlocProbed\" : false, " + "\"localLocator\" : false, " +
    // "\"priority\" : 1, " + "\"weight\" : 50 " + "} " + "], "
    // + "\"recordTtl\" : 100" + "} " + "], " + "\"nonce\" : 3," +
    // "\"keyId\" : 0 " + "} " + "}";
    //
    // return jsonString;
    // }
    //
    // @Test
    // public void northboundRetrieveMapping() throws Exception {
    //
    // String pass = "";
    // LispIpv4Address eid = new LispIpv4Address("10.0.0.1");
    // int mask = 32;
    // LispIpv4Address rloc = new LispIpv4Address("20.0.0.2");
    //
    // // Insert mapping in the database
    // MapRegister mapRegister = new MapRegister();
    // EidToLocatorRecord etlr = new EidToLocatorRecord();
    // etlr.setPrefix(eid);
    // etlr.setMaskLength(mask);
    // etlr.setRecordTtl(254);
    // LocatorRecord record = new LocatorRecord();
    // record.setLocator(rloc);
    // etlr.addLocator(record);
    // mapRegister.addEidToLocator(etlr);
    // lms.handleMapRegister(mapRegister);
    //
    // // Get mapping using NB interface. No IID used
    // URL url = createGetMappingIPv4URL(0, eid, mask);
    // String reply = callURL("GET", null, "application/json", null, url);
    // JSONTokener jt = new JSONTokener(reply);
    // JSONObject json = new JSONObject(jt);
    //
    // // With just one locator, locators is not a JSONArray
    // String rlocRetrieved =
    // json.getJSONObject("locators").getJSONObject("locatorGeneric").getString("ipAddress");
    //
    // assertEquals(rloc.getAddress().getHostAddress(), rlocRetrieved);
    //
    // }
    //
    // private URL createGetKeyIPv4URL(LispIpv4Address address, int mask) throws
    // MalformedURLException {
    // String restUrl =
    // String.format("http://localhost:8080/lispflowmapping/default/%s/%d/%s/%d",
    // "key", address.getAfi().getIanaCode(), address
    // .getAddress().getHostAddress(), mask);
    // URL url = new URL(restUrl);
    // return url;
    // }
    //
    // private URL createGetMappingIPv4URL(int iid, LispIpv4Address address, int
    // mask) throws MalformedURLException {
    // String restUrl =
    // String.format("http://localhost:8080/lispflowmapping/default/%s/%d/%d/%s/%d",
    // "mapping", iid,
    // address.getAfi().getIanaCode(), address.getAddress().getHostAddress(),
    // mask);
    // URL url = new URL(restUrl);
    // return url;
    // }
    //
    // private URL createPutURL(String resource) throws MalformedURLException {
    // String restUrl =
    // String.format("http://localhost:8080/lispflowmapping/default/%s",
    // resource);
    // URL url = new URL(restUrl);
    // return url;
    // }
    //
    // private String createAuthenticationString() {
    // String authString = "admin:admin";
    // byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
    // String authStringEnc = new String(authEncBytes);
    // return authStringEnc;
    // }
    //
    // private String callURL(String method, String content, String accept,
    // String body, URL url) throws IOException, JSONException {
    // String authStringEnc = createAuthenticationString();
    // connection = (HttpURLConnection) url.openConnection();
    // connection.setRequestMethod(method);
    // connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
    // if (content != null) {
    // connection.setRequestProperty("Content-Type", content);
    // }
    // if (accept != null) {
    // connection.setRequestProperty("Accept", accept);
    // }
    // if (body != null) {
    // // now add the request body
    // connection.setDoOutput(true);
    // OutputStreamWriter wr = new
    // OutputStreamWriter(connection.getOutputStream());
    // wr.write(body);
    // wr.flush();
    // }
    // connection.connect();
    //
    // // getting the result, first check response code
    // Integer httpResponseCode = connection.getResponseCode();
    //
    // if (httpResponseCode > 299) {
    // logger.info("HTTP Response Code: " + httpResponseCode);
    // fail();
    // }
    //
    // InputStream is = connection.getInputStream();
    // BufferedReader rd = new BufferedReader(new InputStreamReader(is,
    // Charset.forName("UTF-8")));
    // StringBuilder sb = new StringBuilder();
    // int cp;
    // while ((cp = rd.read()) != -1) {
    // sb.append((char) cp);
    // }
    // is.close();
    // connection.disconnect();
    // return (sb.toString());
    // }

    // private MapReply registerAddressAndQuery(LispAddress eid) throws
    // SocketTimeoutException {
    // return registerAddressAndQuery(eid, -1);
    // }
    //
    // private LispAddress locatorEid = new LispIpv4Address("4.3.2.1");
    //
    // // takes an address, packs it in a MapRegister, sends it, returns the
    // // MapReply
    // private MapReply registerAddressAndQuery(LispAddress eid, int maskLength)
    // throws SocketTimeoutException {
    // MapRegister mapRegister = new MapRegister();
    // mapRegister.setWantMapNotify(true);
    // mapRegister.setNonce(8);
    // EidToLocatorRecord etlr = new EidToLocatorRecord();
    // etlr.setPrefix(eid);
    // if (maskLength != -1) {
    // etlr.setMaskLength(maskLength);
    // }
    // etlr.setRecordTtl(254);
    // LocatorRecord record = new LocatorRecord();
    // record.setLocator(locatorEid);
    // etlr.addLocator(record);
    // mapRegister.addEidToLocator(etlr);
    // sendMapRegister(mapRegister);
    // MapNotify mapNotify = recieveMapNotify();
    // assertEquals(8, mapNotify.getNonce());
    // MapRequest mapRequest = new MapRequest();
    // mapRequest.setNonce(4);
    // mapRequest.addEidRecord(new EidRecord((byte) 32, eid));
    // mapRequest.addItrRloc(new LispIpv4Address(ourAddress));
    // sendMapRequest(mapRequest);
    // return recieveMapReply();
    // }
    //
    // @Test
    // public void mapRegisterWithMapNotifyAndMapRequest() throws
    // SocketTimeoutException {
    //
    // LispIpv4Address eid = new LispIpv4Address("1.2.3.4");
    //
    // MapReply mapReply = registerAddressAndQuery(eid, 32);
    //
    // assertEquals(4, mapReply.getNonce());
    // assertEquals(locatorEid,
    // mapReply.getEidToLocatorRecords().get(0).getLocators().get(0).getLocator());
    //
    // }
    //
    // @Test
    // public void registerAndQuery__MAC() throws SocketTimeoutException {
    // byte[] macAddress = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4,
    // (byte) 5, (byte) 6 };
    //
    // LispMACAddress eid = new LispMACAddress(macAddress);
    // MapReply reply = registerAddressAndQuery(eid);
    //
    // LispAddress addressFromNetwork =
    // reply.getEidToLocatorRecords().get(0).getPrefix();
    // assertTrue(addressFromNetwork instanceof LispMACAddress);
    // byte[] macAddressFromReply = ((LispMACAddress)
    // addressFromNetwork).getMAC();
    //
    // assertArrayEquals(macAddress, macAddressFromReply);
    // }
    //
    // @Test
    // public void registerAndQuery__SrcDestLCAF() throws SocketTimeoutException
    // {
    // String ipString = "10.20.30.200";
    // LispAddress addrToSend1 = new LispIpv4Address(ipString);
    // byte[] fakeMAC = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4,
    // (byte) 5, (byte) 6 };
    // LispAddress addrToSend2 = new LispMACAddress(fakeMAC);
    // LispLCAFAddress address = new LispSourceDestLCAFAddress((byte) 0, (short)
    // 0, (byte) 32, (byte) 0, addrToSend1, addrToSend2);
    //
    // MapReply reply = registerAddressAndQuery(address);
    //
    // LispAddress fromNetwork =
    // reply.getEidToLocatorRecords().get(0).getPrefix();
    // assertTrue(fromNetwork instanceof LispSourceDestLCAFAddress);
    // LispSourceDestLCAFAddress sourceDestFromNetwork =
    // (LispSourceDestLCAFAddress) fromNetwork;
    //
    // LispAddress receivedAddr1 = sourceDestFromNetwork.getSrcAddress();
    // LispAddress receivedAddr2 = sourceDestFromNetwork.getDstAddress();
    //
    // assertTrue(receivedAddr1 instanceof LispIpv4Address);
    // assertTrue(receivedAddr2 instanceof LispMACAddress);
    //
    // LispIpv4Address receivedIP = (LispIpv4Address) receivedAddr1;
    // LispMACAddress receivedMAC = (LispMACAddress) receivedAddr2;
    //
    // assertEquals(ipString, receivedIP.getAddress().getHostAddress());
    // assertArrayEquals(fakeMAC, receivedMAC.getMAC());
    // }
    //
    // @Test
    // public void registerAndQuery__ListLCAF() throws SocketTimeoutException {
    // byte[] macAddress = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4,
    // (byte) 5, (byte) 6 };
    // String ipAddress = "10.20.255.30";
    // List<LispAddress> list = new ArrayList<LispAddress>();
    // list.add(new LispMACAddress(macAddress));
    // list.add(new LispIpv4Address(ipAddress));
    //
    // LispListLCAFAddress listAddrToSend = new LispListLCAFAddress((byte) 0,
    // list);
    //
    // MapReply reply = registerAddressAndQuery(listAddrToSend);
    //
    // LispAddress receivedAddress =
    // reply.getEidToLocatorRecords().get(0).getPrefix();
    //
    // assertTrue(receivedAddress instanceof LispListLCAFAddress);
    //
    // LispListLCAFAddress listAddrFromNetwork = (LispListLCAFAddress)
    // receivedAddress;
    // LispAddress receivedAddr1 = listAddrFromNetwork.getAddresses().get(0);
    // LispAddress receivedAddr2 = listAddrFromNetwork.getAddresses().get(1);
    //
    // assertTrue(receivedAddr1 instanceof LispMACAddress);
    // assertTrue(receivedAddr2 instanceof LispIpv4Address);
    //
    // assertArrayEquals(macAddress, ((LispMACAddress) receivedAddr1).getMAC());
    // assertEquals(ipAddress, ((LispIpv4Address)
    // receivedAddr2).getAddress().getHostAddress());
    // }
    //
    // @Test
    // public void registerAndQuerySegmentLCAF() throws SocketTimeoutException {
    // String ipAddress = "10.20.255.30";
    // int instanceId = 6;
    // LispIpv4Address lispIpAddress = new LispIpv4Address(ipAddress);
    // LispSegmentLCAFAddress addressToSend = new LispSegmentLCAFAddress((byte)
    // 0, instanceId, lispIpAddress);
    //
    // MapReply reply = registerAddressAndQuery(addressToSend);
    //
    // LispAddress receivedAddress =
    // reply.getEidToLocatorRecords().get(0).getPrefix();
    // assertTrue(receivedAddress instanceof LispSegmentLCAFAddress);
    //
    // LispSegmentLCAFAddress segmentfromNetwork = (LispSegmentLCAFAddress)
    // receivedAddress;
    // LispAddress addrFromSegment = segmentfromNetwork.getAddress();
    // assertTrue(addrFromSegment instanceof LispIpv4Address);
    // assertEquals(ipAddress, ((LispIpv4Address)
    // addrFromSegment).getAddress().getHostAddress());
    //
    // assertEquals(instanceId, segmentfromNetwork.getInstanceId());
    // }
    //
    // @Test
    // public void registerAndQuery__TrafficEngineering() throws
    // SocketTimeoutException {
    // byte[] macAddress = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4,
    // (byte) 5, (byte) 6 };
    // String ipAddress = "10.20.255.30";
    // List<ReencapHop> hops = new ArrayList<ReencapHop>();
    // boolean f = false;
    // boolean t = true;
    // hops.add(new ReencapHop(new LispMACAddress(macAddress), (short) 0, t, t,
    // t));
    // hops.add(new ReencapHop(new LispIpv4Address(ipAddress), (short) 0, f, f,
    // f));
    //
    // LispTrafficEngineeringLCAFAddress addressToSend = new
    // LispTrafficEngineeringLCAFAddress((byte) 0, hops);
    //
    // MapReply reply = registerAddressAndQuery(addressToSend);
    //
    // assertTrue(reply.getEidToLocatorRecords().get(0).getPrefix() instanceof
    // LispTrafficEngineeringLCAFAddress);
    //
    // LispTrafficEngineeringLCAFAddress receivedAddress =
    // (LispTrafficEngineeringLCAFAddress)
    // reply.getEidToLocatorRecords().get(0).getPrefix();
    //
    // ReencapHop hop1 = receivedAddress.getHops().get(0);
    // ReencapHop hop2 = receivedAddress.getHops().get(1);
    //
    // assertEquals(t, hop1.isLookup());
    // assertEquals(t, hop1.isRLOCProbe());
    // assertEquals(t, hop1.isStrict());
    //
    // assertEquals(f, hop2.isLookup());
    // assertEquals(f, hop2.isRLOCProbe());
    // assertEquals(f, hop2.isStrict());
    //
    // assertTrue(hop1.getHop() instanceof LispMACAddress);
    // assertTrue(hop2.getHop() instanceof LispIpv4Address);
    //
    // LispMACAddress receivedMACAddress = (LispMACAddress) hop1.getHop();
    // LispIpv4Address receivedIPAddress = (LispIpv4Address) hop2.getHop();
    //
    // assertArrayEquals(macAddress, receivedMACAddress.getMAC());
    // assertEquals(ipAddress, receivedIPAddress.getAddress().getHostAddress());
    // }
    //
    // @Test
    // public void registerAndQuery__ApplicationData() throws
    // SocketTimeoutException {
    // String ipAddress = "1.2.3.4";
    // byte protocol = (byte) 1;
    // int ipTOs = 2;
    // short localPort = (short) 3;
    // short remotePort = (short) 4;
    //
    // LispApplicationDataLCAFAddress addressToSend = new
    // LispApplicationDataLCAFAddress((byte) 0, protocol, ipTOs, localPort,
    // remotePort,
    // new LispIpv4Address(ipAddress));
    //
    // MapReply reply = registerAddressAndQuery(addressToSend);
    //
    // LispAddress receivedAddress =
    // reply.getEidToLocatorRecords().get(0).getPrefix();
    //
    // assertTrue(receivedAddress instanceof LispApplicationDataLCAFAddress);
    //
    // LispApplicationDataLCAFAddress receivedApplicationDataAddress =
    // (LispApplicationDataLCAFAddress) receivedAddress;
    // assertEquals(protocol, receivedApplicationDataAddress.getProtocol());
    // assertEquals(ipTOs, receivedApplicationDataAddress.getIPTos());
    // assertEquals(localPort, receivedApplicationDataAddress.getLocalPort());
    // assertEquals(remotePort, receivedApplicationDataAddress.getRemotePort());
    //
    // LispIpv4Address ipAddressReceived = (LispIpv4Address)
    // receivedApplicationDataAddress.getAddress();
    // assertEquals(ipAddress, ipAddressReceived.getAddress().getHostAddress());
    // }
    //
    // @Test
    // public void eidPrefixLookupIPv4() throws SocketTimeoutException {
    // runPrefixTest(new LispIpv4Address("1.2.3.4"), 16, new
    // LispIpv4Address("1.2.3.2"), new LispIpv4Address("1.1.1.1"), (byte) 32);
    // }
    //
    // @Test
    // public void eidPrefixLookupIPv6() throws SocketTimeoutException {
    // runPrefixTest(new LispIpv6Address("1:2:3:4:5:6:7:8"), 64, new
    // LispIpv6Address("1:2:3:4:5:1:2:3"), new
    // LispIpv6Address("1:2:3:1:2:3:1:2"),
    // (byte) 128);
    // }
    //
    // private void runPrefixTest(LispAddress registerEID, int registerdMask,
    // LispAddress matchedAddress, LispAddress unMatchedAddress, byte mask)
    // throws SocketTimeoutException {
    // MapRegister mapRegister = new MapRegister();
    // mapRegister.setWantMapNotify(true);
    // mapRegister.setNonce(8);
    // EidToLocatorRecord etlr = new EidToLocatorRecord();
    // etlr.setPrefix(registerEID);
    // etlr.setMaskLength(registerdMask);
    // etlr.setRecordTtl(254);
    // LocatorRecord record = new LocatorRecord();
    // record.setLocator(new LispIpv4Address("4.3.2.1"));
    // etlr.addLocator(record);
    // mapRegister.addEidToLocator(etlr);
    // sendMapRegister(mapRegister);
    // MapNotify mapNotify = recieveMapNotify();
    // assertEquals(8, mapNotify.getNonce());
    // MapRequest mapRequest = new MapRequest();
    // mapRequest.setNonce(4);
    // mapRequest.addEidRecord(new EidRecord(mask, matchedAddress));
    // mapRequest.addItrRloc(new LispIpv4Address(ourAddress));
    // sendMapRequest(mapRequest);
    // MapReply mapReply = recieveMapReply();
    // assertEquals(4, mapReply.getNonce());
    // assertEquals(record.getLocator(),
    // mapReply.getEidToLocatorRecords().get(0).getLocators().get(0).getLocator());
    // mapRequest.getEids().get(0).setPrefix(unMatchedAddress);
    // sendMapRequest(mapRequest);
    // mapReply = recieveMapReply();
    // assertEquals(0,
    // mapReply.getEidToLocatorRecords().get(0).getLocators().size());
    // }
    //
    // public void mapRequestMapRegisterAndMapRequest() throws
    // SocketTimeoutException {
    //
    // LispIpv4Address eid = new LispIpv4Address("1.2.3.4");
    // MapRequest mapRequest = new MapRequest();
    // mapRequest.setNonce(4);
    // mapRequest.addEidRecord(new EidRecord((byte) 32, eid));
    // mapRequest.addItrRloc(new LispIpv4Address(ourAddress));
    // sendMapRequest(mapRequest);
    // MapReply mapReply = recieveMapReply();
    // assertEquals(4, mapReply.getNonce());
    // assertEquals(0,
    // mapReply.getEidToLocatorRecords().get(0).getLocators().size());
    // MapRegister mapRegister = new MapRegister();
    // mapRegister.setWantMapNotify(true);
    // mapRegister.setNonce(8);
    // EidToLocatorRecord etlr = new EidToLocatorRecord();
    // etlr.setPrefix(eid);
    // etlr.setMaskLength(32);
    // etlr.setRecordTtl(254);
    // LocatorRecord record = new LocatorRecord();
    // record.setLocator(new LispIpv4Address("4.3.2.1"));
    // etlr.addLocator(record);
    // mapRegister.addEidToLocator(etlr);
    // sendMapRegister(mapRegister);
    // MapNotify mapNotify = recieveMapNotify();
    // assertEquals(8, mapNotify.getNonce());
    // sendMapRequest(mapRequest);
    // mapReply = recieveMapReply();
    // assertEquals(4, mapReply.getNonce());
    // assertEquals(record.getLocator(),
    // mapReply.getEidToLocatorRecords().get(0).getLocators().get(0).getLocator());
    //
    // }

    // private MapReply recieveMapReply() throws SocketTimeoutException {
    // return
    // MapReplySerializer.getInstance().deserialize(ByteBuffer.wrap(receivePacket().getData()));
    // }
    //
    // private MapNotify recieveMapNotify() throws SocketTimeoutException {
    // return
    // MapNotifySerializer.getInstance().deserialize(ByteBuffer.wrap(receivePacket().getData()));
    // }
    //
    // private void sendMapRequest(MapRequest mapRequest) {
    // sendPacket(MapRequestSerializer.getInstance().serialize(mapRequest).array());
    // }

    /*
     * private void sendMapRegister(MapRegister mapRegister) {
     * sendPacket(MapRegisterSerializer
     * .getInstance().serialize(mapRegister).array()); }
     */

    // @Test
    // public void mapRegisterWithAuthenticationWithoutConfiguringAKey() throws
    // SocketTimeoutException {
    // sendPacket(mapRegisterPacketWithAuthenticationAndMapNotify);
    // try {
    // receivePacket(3000);
    // // If didn't timeout then fail:
    // fail();
    // } catch (SocketTimeoutException ste) {
    // }
    // }

    // @Test
    // public void mapRegisterWithMapNotifyWithListLcaf() throws
    // SocketTimeoutException {
    // sendPacket(mapRegisterPacketWithNotifyWithListLCAFAndDistinguishedName);
    // ByteBuffer readBuf = ByteBuffer.wrap(receivePacket().getData());
    // MapNotify reply = MapNotifySerializer.getInstance().deserialize(readBuf);
    // EidToLocatorRecord etlr = reply.getEidToLocatorRecords().get(0);
    // List<LocatorRecord> locators = etlr.getLocators();
    // assertEquals(true, (locators.get(0).getLocator() instanceof
    // LispListLCAFAddress));
    // LispListLCAFAddress listLCAF = (LispListLCAFAddress)
    // locators.get(0).getLocator();
    // LispListLCAFAddress innerList = (LispListLCAFAddress)
    // listLCAF.getAddresses().get(1);
    // LispDistinguishedNameAddress dn = new
    // LispDistinguishedNameAddress("david");
    // assertEquals(dn, ((LispDistinguishedNameAddress)
    // innerList.getAddresses().get(0)));
    // assertEquals(7, reply.getNonce());
    // }

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
        return receivePacket(6000);
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

                // UNCOMMENT to see why bundles didn't resolve!

                try {
                    String host = element.getHeaders().get(Constants.FRAGMENT_HOST);
                    if (host != null) {
                        logger.warn("Bundle " + element.getSymbolicName() + " is a fragment which is part of: " + host);
                        logger.warn("Required imports are: " + element.getHeaders().get(Constants.IMPORT_PACKAGE));
                    } else {
                        element.start();
                    }
                } catch (BundleException e) {
                    logger.error("BundleException:", e);
                    fail();
                }

                debugit = true;

            }
        }
        if (debugit) {
            logger.warn(("Do some debugging because some bundle is unresolved"));
        }
        // ServiceReference r =
        // bc.getServiceReference(IFlowMapping.class.getName());
        /*
         * if (r != null) { this.lms = (IFlowMapping) bc.getService(r); } // If
         * LispMappingServer is null, cannot work assertNotNull(this.lms);
         */

        // Uncomment this code to Know which services were actually loaded to
        // BundleContext
        /*
         * for (ServiceReference sr : bc.getAllServiceReferences(null, null)) {
         * logger.info(sr.getBundle().getSymbolicName());
         * logger.info(sr.toString()); }
         */
    }
}
