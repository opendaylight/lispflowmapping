/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
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

import org.opendaylight.lispflowmapping.type.lisp.MapRegister;

@XmlRootElement(name="list")
@XmlAccessorType(XmlAccessType.NONE)

public class MapRegisterNB {
 
	
	@XmlElement
	String key;
	
	@XmlElement
	MapRegister mapregister;

	public String getKey() {
		return key;
	}

	public MapRegister getMapRegister() {
		return mapregister;
	}
	
}