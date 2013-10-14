/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp.address;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;

@XmlRootElement(name="LispAddressGeneric")
@XmlAccessorType(XmlAccessType.NONE)
public class LispAddressGeneric{

	@XmlElement
	int afi;
	
	@XmlElement
	String ipAddress;
	
	@XmlElement
	byte[] mac;
	
	@XmlElement
	int iid;
	
	@XmlElement
	int asNum;
	
	@XmlElement
	int lcafType;
	
	@XmlElement
    byte protocol;
	
	@XmlElement
    int IPTos;
	
	@XmlElement
    short localPort;
	
	@XmlElement
    short remotePort;
	
	@XmlElement
    LispAddressGeneric srcAddress;
	
	@XmlElement
    LispAddressGeneric dstAddress;
	
	@XmlElement
    byte srcMaskLength;
	
	@XmlElement
    byte dstMaskLength;
	
	@XmlElement
    boolean lookup;
	
	@XmlElement
    boolean RLOCProbe;
	
	@XmlElement
    boolean strict;
	
	@XmlElement
	List<LispAddressGeneric> hops;
	
	@XmlElement
	List<LispAddressGeneric> addresses;
	
	public LispAddressGeneric() {
		super();
		this.afi = 0;
		this.ipAddress = "";
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getAfi() {
		return afi;
	}

	public void setAfi(int afi) {
		this.afi = afi;
	}
	
    public int getIid() {
		return iid;
	}

	public void setIid(int iid) {
		this.iid = iid;
	}

	public LispAddress convertToLispAddress(){
    	
    	LispAddress parsedAddress = null;
    	
    	switch (this.getAfi()) {
    	
    	case 1: 
    		parsedAddress = new LispIpv4Address(this.getIpAddress());
    		break;
    	}
    	
    	return parsedAddress;
    }
	
	
	
	
//
//	@XmlElement
//	LispApplicationDataLCAFAddress applicationDataLCAFAddress;
//
//	@XmlElement
//	LispIpv4Address ipv4Address;
//	
//	@XmlElement
//	LispIpv6Address ipv6Address;
//	
//	@XmlElement
//	LispListLCAFAddress listLCAFAddress;
//	
//	@XmlElement
//	LispMACAddress macAddress;
//	
//	@XmlElement
//	LispNoAddress noAddress;
//	
//	@XmlElement
//	LispSegmentLCAFAddress segmentLCAFAddress;
//	
//	@XmlElement
//	LispSourceDestLCAFAddress sourceDestLCAFAddress;
//	
//	@XmlElement
//	LispTrafficEngineeringLCAFAddress trafficEngineeringLCAFAddress;
//	
//	//ReencapHop?
//
//	public LispApplicationDataLCAFAddress getApplicationDataLCAFAddress() {
//		return applicationDataLCAFAddress;
//	}
//
//	public void setApplicationDataLCAFAddress(
//			LispApplicationDataLCAFAddress applicationDataLCAFAddress) {
//		this.applicationDataLCAFAddress = applicationDataLCAFAddress;
//	}
//
//	public LispIpv4Address getIpv4Address() {
//		return ipv4Address;
//	}
//
//	public void setIpv4Address(LispIpv4Address ipv4Address) {
//		this.ipv4Address = ipv4Address;
//	}
//
//	public LispIpv6Address getIpv6Address() {
//		return ipv6Address;
//	}
//
//	public void setIpv6Address(LispIpv6Address ipv6Address) {
//		this.ipv6Address = ipv6Address;
//	}
//
//	public LispListLCAFAddress getListLCAFAddress() {
//		return listLCAFAddress;
//	}
//
//	public void setListLCAFAddress(LispListLCAFAddress listLCAFAddress) {
//		this.listLCAFAddress = listLCAFAddress;
//	}
//
//	public LispMACAddress getMacAddress() {
//		return macAddress;
//	}
//
//	public void setMacAddress(LispMACAddress macAddress) {
//		this.macAddress = macAddress;
//	}
//
//	public LispNoAddress getNoAddress() {
//		return noAddress;
//	}
//
//	public void setNoAddress(LispNoAddress noAddress) {
//		this.noAddress = noAddress;
//	}
//
//	public LispSegmentLCAFAddress getSegmentLCAFAddress() {
//		return segmentLCAFAddress;
//	}
//
//	public void setSegmentLCAFAddress(LispSegmentLCAFAddress segmentLCAFAddress) {
//		this.segmentLCAFAddress = segmentLCAFAddress;
//	}
//
//	public LispSourceDestLCAFAddress getSourceDestLCAFAddress() {
//		return sourceDestLCAFAddress;
//	}
//
//	public void setSourceDestLCAFAddress(
//			LispSourceDestLCAFAddress sourceDestLCAFAddress) {
//		this.sourceDestLCAFAddress = sourceDestLCAFAddress;
//	}
//
//	public LispTrafficEngineeringLCAFAddress getTrafficEngineeringLCAFAddress() {
//		return trafficEngineeringLCAFAddress;
//	}
//
//	public void setTrafficEngineeringLCAFAddress(
//			LispTrafficEngineeringLCAFAddress trafficEngineeringLCAFAddress) {
//		this.trafficEngineeringLCAFAddress = trafficEngineeringLCAFAddress;
//	}
//
//    
    
    
}
