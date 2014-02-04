package org.opendaylight.lispflowmapping.integrationtest;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemPackages;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import org.opendaylight.controller.test.sal.binding.it.TestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.util.PathUtils;

public class MappingServiceTestHelper {

    public static final String ODL = "org.opendaylight.controller";
    public static final String LISP = "org.opendaylight.lispflowmapping";
    public static final String YANG = "org.opendaylight.yangtools";
    public static final String JERSEY = "com.sun.jersey";

    public static Option mappingServiceBundles() {
        return new DefaultCompositeOption(
                systemProperty("logback.configurationFile").value("file:" + PathUtils.getBaseDir() + "/src/test/resources/logback.xml"),
                // To start OSGi console for inspection remotely
                systemProperty("osgi.console").value("2401"),
                systemProperty("org.eclipse.gemini.web.tomcat.config.path").value(PathUtils.getBaseDir() + "/src/test/resources/tomcat-server.xml"),

                // setting default level. Jersey bundles will need to be started
                // earlier.
                systemProperty("osgi.bundles.defaultStartLevel").value("4"),
                // CoreOptions.vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address="
                // + DEBUG_PORT),

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
                mavenBundle("org.osgi", "org.osgi.core").versionAsInProject(),

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

                TestHelper.baseModelBundles(),
                TestHelper.configMinumumBundles(),
                TestHelper.bindingIndependentSalBundles(),
                TestHelper.bindingAwareSalBundles(),
                TestHelper.mdSalCoreBundles(),
                TestHelper.junitAndMockitoBundles(),

                mavenBundle("org.javassist", "javassist").versionAsInProject(), //

                // Northbound bundles
                mavenBundle("org.opendaylight.controller", "commons.northbound").versionAsInProject(), //
                mavenBundle(ODL + ".thirdparty", "com.sun.jersey.jersey-servlet").versionAsInProject(), //
                mavenBundle(ODL + ".thirdparty", "net.sf.jung2").versionAsInProject(), //
                mavenBundle(ODL + ".thirdparty", "org.apache.catalina.filters.CorsFilter").versionAsInProject().noStart(),
                mavenBundle(JERSEY, "jersey-client").versionAsInProject(),
                mavenBundle(JERSEY, "jersey-server").versionAsInProject().startLevel(2),
                mavenBundle(JERSEY, "jersey-core").versionAsInProject().startLevel(2),
                mavenBundle("com.fasterxml.jackson.core", "jackson-annotations").versionAsInProject(),//
                mavenBundle("com.fasterxml.jackson.core", "jackson-core").versionAsInProject(),//
                mavenBundle("com.fasterxml.jackson.core", "jackson-databind").versionAsInProject(),//
                mavenBundle("com.fasterxml.jackson.jaxrs", "jackson-jaxrs-json-provider").versionAsInProject(),//
                mavenBundle("com.fasterxml.jackson.jaxrs", "jackson-jaxrs-base").versionAsInProject(),//
                mavenBundle("com.fasterxml.jackson.module", "jackson-module-jaxb-annotations").versionAsInProject(),
                mavenBundle("org.codehaus.jettison", "jettison").versionAsInProject(),//
                mavenBundle("org.ow2.asm", "asm-all").versionAsInProject(), //
                mavenBundle("org.opendaylight.controller", "bundlescanner").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "bundlescanner.implementation").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "topologymanager").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "hosttracker").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "forwardingrulesmanager").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "forwardingrulesmanager.implementation").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "statisticsmanager").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "statisticsmanager.implementation").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "switchmanager").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "switchmanager.implementation").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "connectionmanager").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "connectionmanager.implementation").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "configuration").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "configuration.implementation").versionAsInProject(),//
                mavenBundle("org.opendaylight.controller", "usermanager").versionAsInProject(), //
                mavenBundle("org.opendaylight.controller", "usermanager.implementation").versionAsInProject(), //
                mavenBundle("org.springframework", "org.springframework.asm").versionAsInProject(), mavenBundle("org.springframework",
                        "org.springframework.aop").versionAsInProject(), mavenBundle("org.springframework", "org.springframework.context")
                        .versionAsInProject(), mavenBundle("org.springframework", "org.springframework.context.support").versionAsInProject(),
                mavenBundle("org.springframework", "org.springframework.core").versionAsInProject(), mavenBundle("org.springframework",
                        "org.springframework.beans").versionAsInProject(), mavenBundle("org.springframework", "org.springframework.expression")
                        .versionAsInProject(), mavenBundle("org.springframework", "org.springframework.web").versionAsInProject(),

                mavenBundle("org.aopalliance", "com.springsource.org.aopalliance").versionAsInProject(), mavenBundle("org.springframework",
                        "org.springframework.web.servlet").versionAsInProject(),
                mavenBundle("org.springframework.security", "spring-security-config").versionAsInProject(), mavenBundle(
                        "org.springframework.security", "spring-security-core").versionAsInProject(), mavenBundle("org.springframework.security",
                        "spring-security-web").versionAsInProject(), mavenBundle("org.springframework.security", "spring-security-taglibs")
                        .versionAsInProject(), mavenBundle("org.springframework", "org.springframework.transaction").versionAsInProject(),
                mavenBundle("org.opendaylight.controller", "sal.connection").versionAsInProject(), mavenBundle("org.opendaylight.controller",
                        "sal.connection.implementation").versionAsInProject(), mavenBundle("org.opendaylight.controller", "security")
                        .versionAsInProject().noStart(),

                // Tomcat for northbound
                mavenBundle("geminiweb", "org.eclipse.gemini.web.core").versionAsInProject(), mavenBundle("geminiweb",
                        "org.eclipse.gemini.web.extender").versionAsInProject(), mavenBundle("geminiweb", "org.eclipse.gemini.web.tomcat")
                        .versionAsInProject(),
                mavenBundle("geminiweb", "org.eclipse.virgo.kernel.equinox.extensions").versionAsInProject().noStart(), mavenBundle("geminiweb",
                        "org.eclipse.virgo.util.common").versionAsInProject(), mavenBundle("geminiweb", "org.eclipse.virgo.util.io")
                        .versionAsInProject(), mavenBundle("geminiweb", "org.eclipse.virgo.util.math").versionAsInProject(), mavenBundle("geminiweb",
                        "org.eclipse.virgo.util.osgi").versionAsInProject(), mavenBundle("geminiweb", "org.eclipse.virgo.util.osgi.manifest")
                        .versionAsInProject(), mavenBundle("geminiweb", "org.eclipse.virgo.util.parser.manifest").versionAsInProject(),

                // Our bundles
                mavenBundle("org.opendaylight.controller", "clustering.stub").versionAsInProject(), mavenBundle("org.opendaylight.controller",
                        "clustering.services").versionAsInProject(), mavenBundle("org.opendaylight.controller", "sal").versionAsInProject(),

                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.yangmodel").versionAsInProject(), mavenBundle(
                        "org.opendaylight.lispflowmapping", "mappingservice.config").versionAsInProject(), mavenBundle(
                        "org.opendaylight.lispflowmapping", "mappingservice.api").versionAsInProject(), mavenBundle(
                        "org.opendaylight.lispflowmapping", "mappingservice.implementation").versionAsInProject(), //
                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.southbound").versionAsInProject(), //
                mavenBundle("org.opendaylight.lispflowmapping", "mappingservice.northbound").versionAsInProject(), //

                junitBundles());
    }

}
