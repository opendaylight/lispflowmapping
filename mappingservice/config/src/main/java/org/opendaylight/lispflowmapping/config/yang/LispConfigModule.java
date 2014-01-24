/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Generated file

 * Generated from: yang module name: config-lisp  yang module local name: lispconfig
 * Generated by: org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
 * Generated at: Wed Nov 13 19:35:03 IST 2013
 *
 * Do not modify this file unless it is present under src/main directory
 */
package org.opendaylight.lispflowmapping.config.yang;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.opendaylight.controller.config.api.JmxAttributeValidationException;

/**
*
*/
public final class LispConfigModule extends org.opendaylight.lispflowmapping.config.yang.AbstractLispConfigModule {

    public LispConfigModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public LispConfigModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, LispConfigModule oldModule,
            java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void validate() {
        super.validate();
        try {
            Inet4Address.getByName(getBindAddress());
        } catch (UnknownHostException e) {
            throw new JmxAttributeValidationException("LISP bind address is not a valid ipv4 address: " + getBindAddress(), bindAddressJmxAttribute);
        }
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LispConfigContextSetterImpl setter = new LispConfigContextSetterImpl();
        setter.updateContext(this);
        return setter;
    }
}
