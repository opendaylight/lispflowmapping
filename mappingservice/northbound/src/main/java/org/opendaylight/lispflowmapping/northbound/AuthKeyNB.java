/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.northbound;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opendaylight.lispflowmapping.type.lisp.address.LispAddressGeneric;

@XmlRootElement(name="AuthKeyNB")
@XmlAccessorType(XmlAccessType.NONE)

public class AuthKeyNB {


	@XmlElement
	String key;

	@XmlElement
	int maskLength;

	@XmlElement
	LispAddressGeneric address;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getMaskLength() {
		return maskLength;
	}

	public void setMaskLength(int maskLength) {
		this.maskLength = maskLength;
	}

	public LispAddressGeneric getAddress() {
		return address;
	}

	public void setAddress(LispAddressGeneric address) {
		this.address = address;
	}







}