/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation.inventory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opendaylight.controller.sal.core.Property;
import org.opendaylight.controller.sal.utils.HexEncode;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class XtrIdNodeProperty extends Property implements Cloneable {
    private static final long serialVersionUID = 1L;
    @XmlElement(name="value")
    private final byte[] xtrId;
    public static final String name = "xTR-ID";

    /*
     * Private constructor used for JAXB mapping
     */
    private XtrIdNodeProperty() {
        super(name);
        this.xtrId = null;
    }

    public XtrIdNodeProperty(byte[] xtrId) {
        super(name);
        this.xtrId = xtrId;
    }

    @Override
    public String getStringValue() {
        if (xtrId == null) return null;
        return HexEncode.bytesToHexString(this.xtrId);
    }

    @Override
    public Property clone() {
        return new XtrIdNodeProperty(this.xtrId);
    }

    public byte[] getXtrId() {
        return xtrId;
    }
}
