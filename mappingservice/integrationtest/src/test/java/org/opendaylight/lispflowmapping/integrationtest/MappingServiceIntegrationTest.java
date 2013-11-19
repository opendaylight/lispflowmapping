package org.opendaylight.lispflowmapping.integrationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackages;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.lispflowmapping.implementation.dao.ClusterDAOService;
import org.opendaylight.lispflowmapping.implementation.serializer.LispAFIConvertor;
import org.opendaylight.lispflowmapping.implementation.serializer.LispMessage;
import org.opendaylight.lispflowmapping.implementation.serializer.MapNotifySerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRegisterSerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapReplySerializer;
import org.opendaylight.lispflowmapping.implementation.serializer.MapRequestSerializer;
import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;
import org.opendaylight.lispflowmapping.interfaces.lisp.IFlowMapping;
import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispPlugin;
import org.opendaylight.lispflowmapping.type.sbplugin.IConfigLispPlugin;
import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafApplicationDataAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafListAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafSegmentAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafSourceDestAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LcafTrafficEngineeringAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispAFIAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispIpv4Address;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.LispMacAddress;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapNotify;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRegister;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapReply;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.MapRequest;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.ReencapHop;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidrecords.EidRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecord.Action;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.eidtolocatorrecords.EidToLocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcaflistaddress.Addresses;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcaflistaddress.AddressesBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafsegmentaddress.AddressBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafsourcedestaddress.DstAddressBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafsourcedestaddress.SrcAddressBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcaftrafficengineeringaddress.Hops;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcaftrafficengineeringaddress.HopsBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainer;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.LispAddressContainerBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv6;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafApplicationDataBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafListBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafSegmentBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafSourceDestBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.LcafTrafficEngineeringBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Mac;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.MacBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.NoBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecord;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.locatorrecords.LocatorRecordBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.mapregisternotification.MapRegisterBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.ItrRloc;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.ItrRlocBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequest.SourceEidBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.maprequestnotification.MapRequestBuilder;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.reencaphop.Hop;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.reencaphop.HopBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
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

    private IFlowMapping lms;
    protected static final Logger logger = LoggerFactory.getLogger(MappingServiceIntegrationTest.class);
    private byte[] mapRequestPacket;
    private byte[] mapRegisterPacketWithNotify;
    private byte[] mapRegisterPacketWithoutNotify;
    int lispPortNumber = LispMessage.PORT_NUM;
    String lispBindAddress = "127.0.0.1";
    String ourAddress = "127.0.0.2";
    private LispAFIAddress locatorEid;
    private DatagramSocket socket;
    private byte[] mapRegisterPacketWithAuthenticationAndMapNotify;

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
        locatorEid = asIPAfiAddress("4.3.2.1");
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
                + "0050   2a cd 39 c8 d6 08 00 01 01 02 03 04 00 01 7f 00 00 02 00 20 " //
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

    @Test
    public void mapRequestSimple() throws SocketTimeoutException {
        sendPacket(mapRequestPacket);
        ByteBuffer readBuf = ByteBuffer.wrap(receivePacket().getData());
        MapReply reply = MapReplySerializer.getInstance().deserialize(readBuf);
        assertEquals(4435248268955932168L, reply.getNonce().longValue());

    }

    @Test
    public void mapRegisterWithMapNotify() throws SocketTimeoutException {
        sendPacket(mapRegisterPacketWithNotify);
        MapNotify reply = receiveMapNotify();
        assertEquals(7, reply.getNonce().longValue());
    }

    @Test
    public void northboundAddKey() throws Exception {

        LispIpv4Address address = LispAFIConvertor.asIPAfiAddress("10.0.0.1");
        int mask = 32;
        String pass = "asdf";

        URL url = createPutURL("key");
        String authKeyJSON = createAuthKeyJSON(pass, address, mask);
        callURL("PUT", "application/json", "text/plain", authKeyJSON, url);

        String retrievedKey = lms.getAuthenticationKey(LispAFIConvertor.toContainer(address), mask);

        // Check stored password matches the one sent
        assertEquals(pass, retrievedKey);

    }

    @Test
    public void northboundRetrieveKey() throws Exception {

        LispIpv4Address address = LispAFIConvertor.asIPAfiAddress("10.0.0.1");
        int mask = 32;
        String pass = "asdf";

        lms.addAuthenticationKey(LispAFIConvertor.toContainer(address), mask, pass);

        URL url = createGetKeyIPv4URL(address, mask);
        String reply = callURL("GET", null, "application/json", null, url);
        JSONTokener jt = new JSONTokener(reply);
        JSONObject json = new JSONObject(jt);

        // test that the password matches what was we expected.
        assertEquals(pass, json.get("key"));

    }

    // timePeriod - in ms
    public void assertNoPacketReceived(int timePeriod) {
        try {
            receivePacket(timePeriod);
            // If didn't timeout then fail:
            fail();
        } catch (SocketTimeoutException ste) {
        }
    }

    // This registers an IP with a MapRegister, then adds a password via the
    // northbound REST API
    // and checks that the password works
    @Test
    public void testPasswordExactMatch() throws Exception {
        String ipString = "10.0.0.1";
        LispIpv4Address address = LispAFIConvertor.asIPAfiAddress(ipString);
        int mask = 32;
        String pass = "pass";

        URL url = createPutURL("key");

        String jsonAuthData = createAuthKeyJSON(pass, address, mask);

        logger.info("Sending this JSON to LISP server: \n" + jsonAuthData);
        logger.info("Address: " + address);

        byte[] expectedSha = new byte[] { (byte) 146, (byte) 234, (byte) 52, (byte) 247, (byte) 186, (byte) 232, (byte) 31, (byte) 249, (byte) 87,
                (byte) 73, (byte) 234, (byte) 54, (byte) 225, (byte) 160, (byte) 129, (byte) 251, (byte) 73, (byte) 53, (byte) 196, (byte) 62 };

        byte[] zeros = new byte[20];

        callURL("PUT", "application/json", "text/plain", jsonAuthData, url);

        // build a MapRegister
        MapRegisterBuilder mapRegister = new MapRegisterBuilder();
        mapRegister.setWantMapNotify(true);
        mapRegister.setNonce((long) 8);
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setLispAddressContainer(LispAFIConvertor.toContainer(address));
        etlr.setMaskLength((short) mask);
        etlr.setRecordTtl(254);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAFIConvertor.toContainer(locatorEid));
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegister.getEidToLocatorRecord().add(etlr.build());

        mapRegister.setKeyId((short) 1); // LispKeyIDEnum.SHA1.getKeyID()
        mapRegister.setAuthenticationData(zeros);

        sendMapRegister(mapRegister.build());
        assertNoPacketReceived(3000);

        mapRegister.setAuthenticationData(expectedSha);

        sendMapRegister(mapRegister.build());

        // this will fail if no MapNotify arrives for 6 seconds
        MapNotify notify = receiveMapNotify();
    }

    @Test
    public void testPasswordMaskMatch() throws Exception {
        LispIpv4Address addressInRange = LispAFIConvertor.asIPAfiAddress("10.20.30.40");
        LispIpv4Address addressOutOfRange = LispAFIConvertor.asIPAfiAddress("20.40.30.40");
        LispIpv4Address range = LispAFIConvertor.asIPAfiAddress("10.20.30.0");

        int mask = 32;
        String pass = "pass";

        URL url = createPutURL("key");
        String jsonAuthData = createAuthKeyJSON(pass, range, 8);

        callURL("PUT", "application/json", "text/plain", jsonAuthData, url);
        // build a MapRegister
        MapRegisterBuilder mapRegister = new MapRegisterBuilder();

        mapRegister.setWantMapNotify(true);
        mapRegister.setNonce((long) 8);
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setLispAddressContainer(LispAFIConvertor.toContainer(addressInRange));
        etlr.setMaskLength((short) mask);
        etlr.setRecordTtl(254);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAFIConvertor.toContainer(locatorEid));
        record.setLispAddressContainer(LispAFIConvertor.toContainer(locatorEid));
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegister.getEidToLocatorRecord().add(etlr.build());

        mapRegister.setKeyId((short) 1); // LispKeyIDEnum.SHA1.getKeyID()
        mapRegister
                .setAuthenticationData(new byte[] { -15, -52, 38, -94, 125, -111, -68, -79, 68, 6, 101, 45, -1, 47, -4, -67, -113, 104, -110, -71 });

        sendMapRegister(mapRegister.build());

        // this will fail if no MapNotify arrives for 6 seconds
        MapNotify notify = receiveMapNotify();

        etlr.setLispAddressContainer(LispAFIConvertor.toContainer(addressOutOfRange));
        mapRegister
                .setAuthenticationData(new byte[] { -54, 68, -58, -91, -23, 22, -88, -31, 113, 39, 115, 78, -68, -123, -71, -14, -99, 67, -23, -73 });

        sendMapRegister(mapRegister.build());
        assertNoPacketReceived(3000);
    }

    private String createAuthKeyJSON(String key, LispIpv4Address address, int mask) {
        return "{\"key\" : \"" + key + "\",\"maskLength\" : " + mask + ",\"address\" : " + "{\"ipAddress\" : \""
                + address.getIpv4Address().getValue() + "\",\"afi\" : " + address.getAfi().shortValue() + "}}";
    }

    @Test
    public void northboundAddMapping() throws Exception {

        String pass = "asdf";
        LispIpv4Address eid = LispAFIConvertor.asIPAfiAddress("10.0.0.1");
        int mask = 32;
        LispIpv4Address rloc = LispAFIConvertor.asIPAfiAddress("20.0.0.2");

        // NB add mapping always checks the key
        lms.addAuthenticationKey(LispAFIConvertor.toContainer(eid), mask, pass);

        URL url = createPutURL("mapping");
        String mapRegisterJSON = createMapRegisterJSON(pass, eid, mask, rloc);
        callURL("PUT", "application/json", "text/plain", mapRegisterJSON, url);

        // Retrieve the RLOC from the database
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setEidRecord(new ArrayList<EidRecord>());
        mapRequestBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) mask).setLispAddressContainer(LispAFIConvertor.toContainer(eid)).build());
        MapReply mapReply = lms.handleMapRequest(mapRequestBuilder.build());

        LispIpv4Address retrievedRloc = (LispIpv4Address) mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).getLispAddressContainer()
                .getAddress();

        assertEquals(rloc.getIpv4Address().getValue(), retrievedRloc.getIpv4Address().getValue());

    }

    private String createMapRegisterJSON(String key, LispIpv4Address eid, int mask, LispIpv4Address rloc) {
        String jsonString = "{ " + "\"key\" : \"" + key + "\"," + "\"mapregister\" : " + "{ " + "\"proxyMapReply\" : false, "
                + "\"eidToLocatorRecords\" : " + "[ " + "{ " + "\"authoritative\" : true," + "\"prefixGeneric\" : " + "{ " + "\"ipAddress\" : \""
                + eid.getIpv4Address().getValue() + "\"," + "\"afi\" : " + eid.getAfi().shortValue() + "}," + "\"mapVersion\" : 0,"
                + "\"maskLength\" : " + mask + ", " + "\"action\" : \"NoAction\"," + "\"locators\" : " + "[ " + "{ " + "\"multicastPriority\" : 1,"
                + "\"locatorGeneric\" : " + "{ " + "\"ipAddress\" : \"" + rloc.getIpv4Address().getValue() + "\"," + "\"afi\" : "
                + rloc.getAfi().shortValue() + "}, " + "\"routed\" : true," + "\"multicastWeight\" : 50," + "\"rlocProbed\" : false, "
                + "\"localLocator\" : false, " + "\"priority\" : 1, " + "\"weight\" : 50 " + "} " + "], " + "\"recordTtl\" : 100" + "} " + "], "
                + "\"nonce\" : 3," + "\"keyId\" : 0 " + "} " + "}";

        return jsonString;
    }

    @Test
    public void northboundRetrieveMapping() throws Exception {

        String pass = "";
        LispIpv4Address eid = LispAFIConvertor.asIPAfiAddress("10.0.0.1");
        int mask = 32;
        LispIpv4Address rloc = LispAFIConvertor.asIPAfiAddress("20.0.0.2");

        // Insert mapping in the database
        MapRegisterBuilder mapRegister = new MapRegisterBuilder();
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setLispAddressContainer(LispAFIConvertor.toContainer(eid));
        etlr.setMaskLength((short) mask);
        etlr.setRecordTtl(254);
        etlr.setAuthoritative(false);
        etlr.setAction(Action.NoAction);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAFIConvertor.toContainer(rloc));
        record.setRouted(true);
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegister.getEidToLocatorRecord().add(etlr.build());
        lms.handleMapRegister(mapRegister.build());

        // Get mapping using NB interface. No IID used
        URL url = createGetMappingIPv4URL(0, eid, mask);
        String reply = callURL("GET", null, "application/json", null, url);
        JSONTokener jt = new JSONTokener(reply);
        JSONObject json = new JSONObject(jt);

        // With just one locator, locators is not a JSONArray
        String rlocRetrieved = json.getJSONObject("locators").getJSONObject("locatorGeneric").getString("ipAddress");

        assertEquals(rloc.getIpv4Address().getValue(), rlocRetrieved);

    }

    private URL createGetKeyIPv4URL(LispIpv4Address address, int mask) throws MalformedURLException {
        String restUrl = String.format("http://localhost:8080/lispflowmapping/default/%s/%d/%s/%d", "key", address.getAfi().shortValue(), address
                .getIpv4Address().getValue(), mask);
        URL url = new URL(restUrl);
        return url;
    }

    private URL createGetMappingIPv4URL(int iid, LispIpv4Address address, int mask) throws MalformedURLException {
        String restUrl = String.format("http://localhost:8080/lispflowmapping/default/%s/%d/%d/%s/%d", "mapping", iid, address.getAfi().shortValue(),
                address.getIpv4Address().getValue(), mask);
        URL url = new URL(restUrl);
        return url;
    }

    private URL createPutURL(String resource) throws MalformedURLException {

        String restUrl = String.format("http://localhost:8080/lispflowmapping/default/%s", resource);

        URL url = new URL(restUrl);
        return url;
    }

    private String createAuthenticationString() {
        String authString = "admin:admin";
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        return authStringEnc;
    }

    private String callURL(String method, String content, String accept, String body, URL url) throws IOException, JSONException {
        String authStringEnc = createAuthenticationString();
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
        if (content != null) {
            connection.setRequestProperty("Content-Type", content);
        }
        if (accept != null) {
            connection.setRequestProperty("Accept", accept);
        }
        if (body != null) {
            // now add the request body
            connection.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(body);
            wr.flush();
        }
        connection.connect();

        // getting the result, first check response code
        Integer httpResponseCode = connection.getResponseCode();

        if (httpResponseCode > 299) {
            logger.info("HTTP Address: " + url);
            logger.info("HTTP Response Code: " + httpResponseCode);
            fail();
        }

        InputStream is = connection.getInputStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        is.close();
        connection.disconnect();
        return (sb.toString());
    }

    private MapReply registerAddressAndQuery(LispAFIAddress eid) throws SocketTimeoutException {
        return registerAddressAndQuery(eid, -1);
    }

    private IConfigLispPlugin configLispPlugin;

    // takes an address, packs it in a MapRegister, sends it, returns the
    // MapReply
    private MapReply registerAddressAndQuery(LispAFIAddress eid, int maskLength) throws SocketTimeoutException {
        MapRegisterBuilder mapRegisterBuilder = new MapRegisterBuilder();
        mapRegisterBuilder.setWantMapNotify(true);
        mapRegisterBuilder.setKeyId((short) 0);
        mapRegisterBuilder.setAuthenticationData(new byte[0]);
        mapRegisterBuilder.setNonce((long) 8);
        mapRegisterBuilder.setProxyMapReply(false);
        EidToLocatorRecordBuilder etlrBuilder = new EidToLocatorRecordBuilder();
        etlrBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid));
        if (maskLength != -1) {
            etlrBuilder.setMaskLength((short) maskLength);
        } else {
            etlrBuilder.setMaskLength((short) 0);
        }
        etlrBuilder.setRecordTtl(254);
        etlrBuilder.setAction(Action.NoAction);
        etlrBuilder.setAuthoritative(false);
        etlrBuilder.setMapVersion((short) 0);
        LocatorRecordBuilder recordBuilder = new LocatorRecordBuilder();
        recordBuilder.setLocalLocator(false);
        recordBuilder.setRlocProbed(false);
        recordBuilder.setRouted(true);
        recordBuilder.setMulticastPriority((short) 0);
        recordBuilder.setMulticastWeight((short) 0);
        recordBuilder.setPriority((short) 0);
        recordBuilder.setWeight((short) 0);
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(locatorEid));
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.getLocatorRecord().add(recordBuilder.build());
        mapRegisterBuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegisterBuilder.getEidToLocatorRecord().add(etlrBuilder.build());
        sendMapRegister(mapRegisterBuilder.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setNonce((long) 4);
        mapRequestBuilder.setEidRecord(new ArrayList<EidRecord>());
        mapRequestBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(LispAFIConvertor.toContainer(eid)).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(asIPAfiAddress(ourAddress)))
                .build());
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(asIPAfiAddress(ourAddress))).build());
        mapRequestBuilder.setAuthoritative(false);
        mapRequestBuilder.setMapDataPresent(false);
        mapRequestBuilder.setPitr(false);
        mapRequestBuilder.setProbe(false);
        mapRequestBuilder.setSmr(false);
        mapRequestBuilder.setSmrInvoked(false);
        sendMapRequest(mapRequestBuilder.build());
        return receiveMapReply();
    }

    @Test
    public void mapRegisterWithMapNotifyAndMapRequest() throws SocketTimeoutException {

        LispAFIAddress eid = asIPAfiAddress("1.2.3.4");

        MapReply mapReply = registerAddressAndQuery(eid, 32);

        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(LispAFIConvertor.toContainer(locatorEid), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0)
                .getLispAddressContainer());

    }

    @Test
    public void registerAndQuery__MAC() throws SocketTimeoutException {
        String macAddress = "01:02:03:04:05:06";

        MapReply reply = registerAddressAndQuery(asMacAfiAddress(macAddress));

        assertTrue(true);
        LispAFIAddress addressFromNetwork = LispAFIConvertor.toAFI(reply.getEidToLocatorRecord().get(0).getLispAddressContainer());
        assertTrue(addressFromNetwork instanceof LispMacAddress);
        String macAddressFromReply = ((Mac) addressFromNetwork).getMacAddress().getValue();

        assertEquals(macAddress, macAddressFromReply);
    }

    @Test
    @Ignore
    public void registerAndQuery__SrcDestLCAF() throws SocketTimeoutException {
        String ipString = "10.20.30.200";
        String macString = "01:02:03:04:05:06";
        org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Ipv4 addrToSend1 = asPrimitiveIPAfiAddress(ipString);
        org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Mac addrToSend2 = asPrimitiveMacAfiAddress(macString);
        LcafSourceDestBuilder builder = new LcafSourceDestBuilder();
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode());
        builder.setLcafType((short) LispCanonicalAddressFormatEnum.SOURCE_DEST.getLispCode());
        builder.setSrcMaskLength((short) 0);
        builder.setDstMaskLength((short) 0);
        builder.setSrcAddress(new SrcAddressBuilder().setPrimitiveAddress(addrToSend1).build());
        builder.setDstAddress(new DstAddressBuilder().setPrimitiveAddress(addrToSend2).build());

        MapReply reply = registerAddressAndQuery(builder.build());

        LispAddressContainer fromNetwork = reply.getEidToLocatorRecord().get(0).getLispAddressContainer();
        assertTrue(fromNetwork.getAddress() instanceof LcafSourceDestAddress);
        LcafSourceDestAddress sourceDestFromNetwork = (LcafSourceDestAddress) fromNetwork.getAddress();

        LispAFIAddress receivedAddr1 = (LispAFIAddress) sourceDestFromNetwork.getSrcAddress().getPrimitiveAddress();
        LispAFIAddress receivedAddr2 = (LispAFIAddress) sourceDestFromNetwork.getDstAddress().getPrimitiveAddress();

        assertTrue(receivedAddr1 instanceof LispIpv4Address);
        assertTrue(receivedAddr2 instanceof LispMacAddress);

        LispIpv4Address receivedIP = (LispIpv4Address) receivedAddr1;
        LispMacAddress receivedMAC = (LispMacAddress) receivedAddr2;

        assertEquals(ipString, receivedIP.getIpv4Address().getValue());
        assertEquals(macString, receivedMAC.getMacAddress().getValue());
    }

    @Test
    public void registerAndQuery__ListLCAF() throws SocketTimeoutException {
        String macString = "01:02:03:04:05:06";
        String ipString = "10.20.255.30";
        LcafListBuilder listbuilder = new LcafListBuilder();
        listbuilder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType((short) LispCanonicalAddressFormatEnum.LIST.getLispCode());
        listbuilder.setAddresses(new ArrayList<Addresses>());
        listbuilder.getAddresses().add(new AddressesBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(asIPAfiAddress(ipString))).build());
        listbuilder.getAddresses().add(new AddressesBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(asMacAfiAddress(macString))).build());

        MapReply reply = registerAddressAndQuery(listbuilder.build());

        LispAFIAddress receivedAddress = LispAFIConvertor.toAFI(reply.getEidToLocatorRecord().get(0).getLispAddressContainer());

        assertTrue(receivedAddress instanceof LcafListAddress);

        LcafListAddress listAddrFromNetwork = (LcafListAddress) receivedAddress;
        LispAFIAddress receivedAddr1 = (LispAFIAddress) listAddrFromNetwork.getAddresses().get(0).getPrimitiveAddress();
        LispAFIAddress receivedAddr2 = (LispAFIAddress) listAddrFromNetwork.getAddresses().get(1).getPrimitiveAddress();

        assertTrue(receivedAddr1 instanceof LispIpv4Address);
        assertTrue(receivedAddr2 instanceof LispMacAddress);

        assertEquals(macString, ((LispMacAddress) receivedAddr2).getMacAddress().getValue());
        assertEquals(ipString, ((LispIpv4Address) receivedAddr1).getIpv4Address().getValue());
    }

    @Test
    public void registerAndQuerySegmentLCAF() throws SocketTimeoutException {
        String ipString = "10.20.255.30";
        int instanceId = 6;
        LcafSegmentBuilder builder = new LcafSegmentBuilder();
        builder.setInstanceId((long) instanceId);
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType((short) LispCanonicalAddressFormatEnum.SEGMENT.getLispCode());
        builder.setAddress(new AddressBuilder().setPrimitiveAddress(LispAFIConvertor.toPrimitive(asIPAfiAddress(ipString))).build());

        MapReply reply = registerAddressAndQuery(builder.build());

        LispAFIAddress receivedAddress = LispAFIConvertor.toAFI(reply.getEidToLocatorRecord().get(0).getLispAddressContainer());
        assertTrue(receivedAddress instanceof LcafSegmentAddress);

        LcafSegmentAddress segmentfromNetwork = (LcafSegmentAddress) receivedAddress;
        LispAFIAddress addrFromSegment = (LispAFIAddress) segmentfromNetwork.getAddress().getPrimitiveAddress();
        assertTrue(addrFromSegment instanceof LispIpv4Address);
        assertEquals(ipString, ((LispIpv4Address) addrFromSegment).getIpv4Address().getValue());

        assertEquals(instanceId, segmentfromNetwork.getInstanceId().intValue());
    }

    @Test
    public void registerAndQuery__TrafficEngineering() throws SocketTimeoutException {
        String macString = "01:02:03:04:05:06";
        String ipString = "10.20.255.30";
        HopBuilder hopBuilder = new HopBuilder();
        hopBuilder.setPrimitiveAddress(LispAFIConvertor.toPrimitive(asIPAfiAddress(ipString)));
        Hop hop1 = hopBuilder.build();
        hopBuilder.setPrimitiveAddress(LispAFIConvertor.toPrimitive(asMacAfiAddress(macString)));
        Hop hop2 = hopBuilder.build();
        HopsBuilder hb = new HopsBuilder();
        hb.setHop(hop1);
        hb.setLookup(true);
        hb.setRLOCProbe(false);
        hb.setStrict(true);
        HopsBuilder hb2 = new HopsBuilder();
        hb2.setHop(hop2);
        hb2.setLookup(false);
        hb2.setRLOCProbe(true);
        hb2.setStrict(false);
        Hops hops1 = hb.build();
        Hops hops2 = hb2.build();
        LcafTrafficEngineeringBuilder trafficBuilder = new LcafTrafficEngineeringBuilder();
        trafficBuilder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType(
                (short) LispCanonicalAddressFormatEnum.TRAFFIC_ENGINEERING.getLispCode());
        trafficBuilder.setHops(new ArrayList<Hops>());
        trafficBuilder.getHops().add(hb.build());
        trafficBuilder.getHops().add(hb2.build());

        MapReply reply = registerAddressAndQuery(trafficBuilder.build());

        assertTrue(LispAFIConvertor.toAFI(reply.getEidToLocatorRecord().get(0).getLispAddressContainer()) instanceof LcafTrafficEngineeringAddress);

        LcafTrafficEngineeringAddress receivedAddress = (LcafTrafficEngineeringAddress) LispAFIConvertor.toAFI(reply.getEidToLocatorRecord().get(0)
                .getLispAddressContainer());

        ReencapHop receivedHop1 = receivedAddress.getHops().get(0);
        ReencapHop receivedHop2 = receivedAddress.getHops().get(1);

        assertEquals(true, hops1.isLookup());
        assertEquals(false, hops1.isRLOCProbe());
        assertEquals(true, hops1.isStrict());

        assertEquals(false, hops2.isLookup());
        assertEquals(true, hops2.isRLOCProbe());
        assertEquals(false, hops2.isStrict());

        assertTrue(receivedHop1.getHop().getPrimitiveAddress() instanceof LispIpv4Address);
        assertTrue(receivedHop2.getHop().getPrimitiveAddress() instanceof LispMacAddress);

        assertEquals(ipString, ((LispIpv4Address) receivedHop1.getHop().getPrimitiveAddress()).getIpv4Address().getValue());
        assertEquals(macString, ((LispMacAddress) receivedHop2.getHop().getPrimitiveAddress()).getMacAddress().getValue());
    }

    @Test
    public void registerAndQuery__ApplicationData() throws SocketTimeoutException {
        String ipString = "1.2.3.4";
        short protocol = 1;
        int ipTOs = 2;
        int localPort = 3;
        int remotePort = 4;

        LcafApplicationDataBuilder builder = new LcafApplicationDataBuilder();
        builder.setAfi(AddressFamilyNumberEnum.LCAF.getIanaCode()).setLcafType((short) LispCanonicalAddressFormatEnum.APPLICATION_DATA.getLispCode());
        builder.setIpTos(ipTOs);
        builder.setProtocol(protocol);
        builder.setLocalPort(new PortNumber(localPort));
        builder.setRemotePort(new PortNumber(remotePort));
        builder.setAddress(new org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lcafapplicationdataaddress.AddressBuilder()
                .setPrimitiveAddress(LispAFIConvertor.toPrimitive(asIPAfiAddress(ipString))).build());

        LcafApplicationDataAddress addressToSend = builder.build();

        MapReply reply = registerAddressAndQuery(addressToSend);

        LispAFIAddress receivedAddress = LispAFIConvertor.toAFI(reply.getEidToLocatorRecord().get(0).getLispAddressContainer());

        assertTrue(receivedAddress instanceof LcafApplicationDataAddress);

        LcafApplicationDataAddress receivedApplicationDataAddress = (LcafApplicationDataAddress) receivedAddress;
        assertEquals(protocol, receivedApplicationDataAddress.getProtocol().intValue());
        assertEquals(ipTOs, receivedApplicationDataAddress.getIpTos().intValue());
        assertEquals(localPort, receivedApplicationDataAddress.getLocalPort().getValue().intValue());
        assertEquals(remotePort, receivedApplicationDataAddress.getRemotePort().getValue().intValue());

        LispIpv4Address ipAddressReceived = (LispIpv4Address) receivedApplicationDataAddress.getAddress().getPrimitiveAddress();
        assertEquals(ipString, ipAddressReceived.getIpv4Address().getValue());
    }

    @Test
    public void eidPrefixLookupIPv4() throws SocketTimeoutException {
        runPrefixTest(LispAFIConvertor.asIPAfiAddress("1.2.3.4"), 16, LispAFIConvertor.asIPAfiAddress("1.2.3.2"),
                LispAFIConvertor.asIPAfiAddress("1.1.1.1"), (byte) 32);
    }

    @Test
    public void eidPrefixLookupIPv6() throws SocketTimeoutException {
        runPrefixTest(LispAFIConvertor.asIPv6AfiAddress("1:2:3:4:5:6:7:8"), 64, LispAFIConvertor.asIPv6AfiAddress("1:2:3:4:5:1:2:3"),
                LispAFIConvertor.asIPv6AfiAddress("1:2:3:1:2:3:1:2"), (byte) 128);
    }

    private void runPrefixTest(LispAFIAddress registerEID, int registerdMask, LispAFIAddress matchedAddress, LispAFIAddress unMatchedAddress,
            byte mask) throws SocketTimeoutException {

        MapRegisterBuilder mapRegister = new MapRegisterBuilder();
        mapRegister.setWantMapNotify(true);
        mapRegister.setNonce((long) 8);
        mapRegister.setWantMapNotify(true);
        mapRegister.setKeyId((short) 0);
        mapRegister.setAuthenticationData(new byte[0]);
        mapRegister.setNonce((long) 8);
        mapRegister.setProxyMapReply(false);
        EidToLocatorRecordBuilder etlr = new EidToLocatorRecordBuilder();
        etlr.setRecordTtl(254);
        etlr.setAction(Action.NoAction);
        etlr.setAuthoritative(false);
        etlr.setMapVersion((short) 0);
        etlr.setLispAddressContainer(LispAFIConvertor.toContainer(registerEID));
        etlr.setMaskLength((short) registerdMask);
        etlr.setRecordTtl(254);
        LocatorRecordBuilder record = new LocatorRecordBuilder();
        record.setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress("4.3.2.1")));
        record.setLocalLocator(false);
        record.setRlocProbed(false);
        record.setRouted(true);
        record.setMulticastPriority((short) 0);
        record.setMulticastWeight((short) 0);
        record.setPriority((short) 0);
        record.setWeight((short) 0);
        etlr.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlr.getLocatorRecord().add(record.build());
        mapRegister.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegister.getEidToLocatorRecord().add(etlr.build());
        sendMapRegister(mapRegister.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
        MapRequestBuilder mapRequest = new MapRequestBuilder();
        mapRequest.setNonce((long) 4);
        mapRequest.setSourceEid(new SourceEidBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(asIPAfiAddress(ourAddress))).build());
        mapRequest.setEidRecord(new ArrayList<EidRecord>());
        mapRequest.setAuthoritative(false);
        mapRequest.setMapDataPresent(false);
        mapRequest.setPitr(false);
        mapRequest.setProbe(false);
        mapRequest.setSmr(false);
        mapRequest.setSmrInvoked(false);
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) mask).setLispAddressContainer(LispAFIConvertor.toContainer(matchedAddress)).build());
        mapRequest.setItrRloc(new ArrayList<ItrRloc>());
        mapRequest.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(LispAFIConvertor.asIPAfiAddress(ourAddress))).build());
        sendMapRequest(mapRequest.build());
        MapReply mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(record.getLispAddressContainer(), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0).getLispAddressContainer());
        mapRequest.setEidRecord(new ArrayList<EidRecord>());
        mapRequest.getEidRecord().add(
                new EidRecordBuilder().setMask((short) mask).setLispAddressContainer(LispAFIConvertor.toContainer(unMatchedAddress)).build());
        sendMapRequest(mapRequest.build());
        mapReply = receiveMapReply();
        assertEquals(0, mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().size());
    }

    @Test
    public void mapRequestMapRegisterAndMapRequest() throws SocketTimeoutException {

        LispAFIAddress eid = asIPAfiAddress("1.2.3.4");
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setNonce((long) 4);
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(
                LispAFIConvertor.toContainer(new NoBuilder().setAfi((short) 0).build())).build());
        mapRequestBuilder.setEidRecord(new ArrayList<EidRecord>());
        mapRequestBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(LispAFIConvertor.toContainer(eid)).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(asIPAfiAddress(ourAddress))).build());
        sendMapRequest(mapRequestBuilder.build());
        MapReply mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(0, mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().size());
        MapRegisterBuilder mapRegisterbuilder = new MapRegisterBuilder();
        mapRegisterbuilder.setWantMapNotify(true);
        mapRegisterbuilder.setNonce((long) 8);
        EidToLocatorRecordBuilder etlrBuilder = new EidToLocatorRecordBuilder();
        etlrBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid));
        etlrBuilder.setMaskLength((short) 32);
        etlrBuilder.setRecordTtl(254);
        LocatorRecordBuilder recordBuilder = new LocatorRecordBuilder();
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(asIPAfiAddress("4.3.2.1")));
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.getLocatorRecord().add(recordBuilder.build());
        mapRegisterbuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegisterbuilder.getEidToLocatorRecord().add(etlrBuilder.build());
        sendMapRegister(mapRegisterbuilder.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
        sendMapRequest(mapRequestBuilder.build());
        mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(recordBuilder.getLispAddressContainer(), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0)
                .getLispAddressContainer());

    }

    public void mapRequestMapRegisterAndMapRequestTestTimeout() throws SocketTimeoutException {

        LispIpv4Address eid = LispAFIConvertor.asIPAfiAddress("1.2.3.4");
        MapRequestBuilder mapRequestBuilder = new MapRequestBuilder();
        mapRequestBuilder.setNonce((long) 4);
        mapRequestBuilder.setSourceEid(new SourceEidBuilder().setLispAddressContainer(
                LispAFIConvertor.toContainer(new NoBuilder().setAfi((short) 0).build())).build());
        mapRequestBuilder.setEidRecord(new ArrayList<EidRecord>());
        mapRequestBuilder.getEidRecord().add(
                new EidRecordBuilder().setMask((short) 32).setLispAddressContainer(LispAFIConvertor.toContainer(eid)).build());
        mapRequestBuilder.setItrRloc(new ArrayList<ItrRloc>());
        mapRequestBuilder.getItrRloc().add(
                new ItrRlocBuilder().setLispAddressContainer(LispAFIConvertor.toContainer(asIPAfiAddress(ourAddress))).build());
        sendMapRequest(mapRequestBuilder.build());
        MapReply mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(0, mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().size());
        MapRegisterBuilder mapRegisterbuilder = new MapRegisterBuilder();
        mapRegisterbuilder.setWantMapNotify(true);
        mapRegisterbuilder.setNonce((long) 8);
        EidToLocatorRecordBuilder etlrBuilder = new EidToLocatorRecordBuilder();
        etlrBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(eid));
        etlrBuilder.setMaskLength((short) 32);
        etlrBuilder.setRecordTtl(254);
        LocatorRecordBuilder recordBuilder = new LocatorRecordBuilder();
        recordBuilder.setLispAddressContainer(LispAFIConvertor.toContainer(asIPAfiAddress("4.3.2.1")));
        etlrBuilder.setLocatorRecord(new ArrayList<LocatorRecord>());
        etlrBuilder.getLocatorRecord().add(recordBuilder.build());
        mapRegisterbuilder.setEidToLocatorRecord(new ArrayList<EidToLocatorRecord>());
        mapRegisterbuilder.getEidToLocatorRecord().add(etlrBuilder.build());
        sendMapRegister(mapRegisterbuilder.build());
        MapNotify mapNotify = receiveMapNotify();
        assertEquals(8, mapNotify.getNonce().longValue());
        sendMapRequest(mapRequestBuilder.build());
        mapReply = receiveMapReply();
        assertEquals(4, mapReply.getNonce().longValue());
        assertEquals(recordBuilder.getLispAddressContainer(), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0)
                .getLispAddressContainer());
        ServiceReference r = bc.getServiceReference(ILispDAO.class.getName());
        if (r != null) {
            ClusterDAOService clusterService = (ClusterDAOService) bc.getService(r);
            clusterService.setTimeUnit(TimeUnit.NANOSECONDS);
            sendMapRequest(mapRequestBuilder.build());
            mapReply = receiveMapReply();
            assertEquals(0, mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().size());
            clusterService.setTimeUnit(TimeUnit.MINUTES);
            sendMapRequest(mapRequestBuilder.build());
            mapReply = receiveMapReply();
            assertEquals(recordBuilder.getLispAddressContainer(), mapReply.getEidToLocatorRecord().get(0).getLocatorRecord().get(0)
                    .getLispAddressContainer());
        }
    }

    private MapReply receiveMapReply() throws SocketTimeoutException {
        return MapReplySerializer.getInstance().deserialize(ByteBuffer.wrap(receivePacket().getData()));
    }

    private MapNotify receiveMapNotify() throws SocketTimeoutException {
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
        ServiceReference r = bc.getServiceReference(IFlowMapping.class.getName());
        if (r != null) {
            this.lms = (IFlowMapping) bc.getService(r);
        }

        assertNotNull(IFlowMapping.class.getName() + " service wasn't found in bundle context ", this.lms);

        r = bc.getServiceReference(IConfigLispPlugin.class.getName());
        if (r != null) {
            this.configLispPlugin = (IConfigLispPlugin) bc.getService(r);
        }

        assertNotNull(IConfigLispPlugin.class.getName() + " service wasn't found in bundle context ", this.configLispPlugin);
        configLispPlugin.setLispAddress(lispBindAddress);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // If LispMappingServer is null, cannot work
        assertNotNull(this.lms);

        // Uncomment this code to Know which services were actually loaded to
        // BundleContext
        /*
         * for (ServiceReference sr : bc.getAllServiceReferences(null, null)) {
         * logger.info(sr.getBundle().getSymbolicName());
         * logger.info(sr.toString()); }
         */
    }

    private LispAddressContainer getIPContainer(String ip) {
        return new LispAddressContainerBuilder().setAddress(asIPAfiAddress(ip)).build();
    }

    private Ipv4 asIPAfiAddress(String ip) {
        return new Ipv4Builder().setIpv4Address(new Ipv4Address(ip)).setAfi((short) AddressFamilyNumberEnum.IP.getIanaCode()).build();
    }

    private org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Ipv4 asPrimitiveIPAfiAddress(String ip) {
        return new org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Ipv4Builder()
                .setIpv4Address(new Ipv4Address(ip)).setAfi((short) AddressFamilyNumberEnum.IP.getIanaCode()).build();
    }

    private Mac asMacAfiAddress(String mac) {
        return new MacBuilder().setMacAddress(new MacAddress(mac)).setAfi((short) AddressFamilyNumberEnum.MAC.getIanaCode()).build();
    }

    private org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.Mac asPrimitiveMacAfiAddress(String mac) {
        return new org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispsimpleaddress.primitiveaddress.MacBuilder()
                .setMacAddress(new MacAddress(mac)).setAfi((short) AddressFamilyNumberEnum.MAC.getIanaCode()).build();
    }

    private Ipv6 asIPv6AfiAddress(String ip) {
        return new Ipv6Builder().setIpv6Address(new Ipv6Address(ip)).setAfi((short) AddressFamilyNumberEnum.IP6.getIanaCode()).build();
    }

}
