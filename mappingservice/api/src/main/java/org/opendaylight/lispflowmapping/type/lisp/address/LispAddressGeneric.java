/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp.address;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opendaylight.lispflowmapping.type.AddressFamilyNumberEnum;
import org.opendaylight.lispflowmapping.type.LispCanonicalAddressFormatEnum;

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
	int instanceId;
	
	@XmlElement
	int asNum;
	
	@XmlElement
	int lcafType;
	
	@XmlElement
    byte protocol;
	
	@XmlElement
    int ipTos;
	
	@XmlElement
    short localPort;
	
	@XmlElement
    short remotePort;
	
	@XmlElement
    LispAddressGeneric address;
	
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
	
	public LispAddressGeneric(){}
	
	
	public LispAddressGeneric(int afi) {
		this.afi = afi;
	}
	
	public LispAddressGeneric(int afi, String address) {
		this.afi = afi;
		
		AddressFamilyNumberEnum afiEnum = AddressFamilyNumberEnum.valueOf((short) afi);
		
		switch (afiEnum) {
		case AS:
			asNum = Integer.valueOf(address);
			break;
		case IP:
		case IP6:
			ipAddress = address;
			break;
		//TODO rest cases			
		default:
			break;
		}
	}
	
	public LispAddressGeneric(int afi, LispAddressGeneric address) {
		this.afi = afi;
		this.address = address;
	}
	
	public LispAddressGeneric(LispAddress lispAddress) {
		
		afi = lispAddress.getAfi().getIanaCode();
		
		switch (lispAddress.getAfi()){
		case IP:
			LispIpv4Address ipv4Address = (LispIpv4Address) lispAddress;
			ipAddress = ipv4Address.getAddress().toString();
			break;
		case AS:
			LispASAddress asAddress = (LispASAddress) lispAddress;
			asNum = asAddress.getAS();
			break;
		case IP6:
			LispIpv6Address ipv6Address = (LispIpv6Address) lispAddress;
			ipAddress = ipv6Address.getAddress().toString();
			break;
		case MAC:
			LispMACAddress macAddress = (LispMACAddress) lispAddress;
			mac = macAddress.getMAC();
			break;
		case LCAF:
			LispLCAFAddress lcafAddress = (LispLCAFAddress) lispAddress;
			lcafType = lcafAddress.getType().getLispCode();
			
			switch (lcafAddress.getType()){
			case APPLICATION_DATA:
				LispApplicationDataLCAFAddress appDataAddress = (LispApplicationDataLCAFAddress) lcafAddress; 
			    protocol = appDataAddress.getProtocol();
			    ipTos = appDataAddress.getIPTos();
			    localPort = appDataAddress.getLocalPort();
			    remotePort = appDataAddress.getLocalPort();
			    address = new LispAddressGeneric(appDataAddress.getAddress());
				break;
			case LIST:
				LispListLCAFAddress listAddress = (LispListLCAFAddress) lcafAddress;
				addresses = new ArrayList<LispAddressGeneric>();
				for(int i=0;i<listAddress.getAddresses().size();i++){
					addresses.add(new LispAddressGeneric(listAddress.getAddresses().get(i)));
				}
				break;
			case SEGMENT:
				LispSegmentLCAFAddress segmentAddress = (LispSegmentLCAFAddress) lcafAddress;
				instanceId = segmentAddress.getInstanceId();
				address = new LispAddressGeneric(segmentAddress);
				break;
			case SOURCE_DEST:
				LispSourceDestLCAFAddress srcDstAddress = (LispSourceDestLCAFAddress) lcafAddress;
			    srcAddress = new LispAddressGeneric(srcDstAddress.getSrcAddress());
			    dstAddress = new LispAddressGeneric(srcDstAddress.getDstAddress());
			    srcMaskLength = srcDstAddress.getSrcMaskLength();
			    dstMaskLength = srcDstAddress.getDstMaskLength();
				break;
			case TRAFFIC_ENGINEERING:
				LispTrafficEngineeringLCAFAddress teAddress = (LispTrafficEngineeringLCAFAddress) lcafAddress;
				
				hops = new ArrayList<LispAddressGeneric>();
				for(int i=0;i<teAddress.getHops().size();i++){
					LispAddressGeneric hop = new LispAddressGeneric(AddressFamilyNumberEnum.UNKNOWN.getIanaCode());
					hop.setLookup(teAddress.getHops().get(i).isLookup());
					hop.setRLOCProbe(teAddress.getHops().get(i).isRLOCProbe());
					hop.setStrict(teAddress.getHops().get(i).isStrict());
					hop.setAddress(new LispAddressGeneric(teAddress.getHops().get(i).getHop()));
					hops.add(hop);
				}
				break;
			default:
				break;
			
			}
			break;
		default:
			break;
			
		}
	}

	public LispAddress convertToLispAddress(){
    	
    	LispAddress lispAddress = null;
    	
    	AddressFamilyNumberEnum afiEnum  = AddressFamilyNumberEnum.valueOf((short) this.getAfi());
    	
    	switch (afiEnum) {
    	
    	case IP: 
    		lispAddress = new LispIpv4Address(this.getIpAddress());
    		break;
		case AS:
			lispAddress = new LispASAddress(this.getAsNum());
			break;
		case IP6:
			lispAddress = new LispIpv6Address(this.getIpAddress());
			break;
		case MAC:
			lispAddress = new LispMACAddress(this.getMac());
			break;
		case LCAF:
			LispCanonicalAddressFormatEnum lcafEnum = LispCanonicalAddressFormatEnum.valueOf(this.getLcafType());
			
			switch (lcafEnum){
			case APPLICATION_DATA:
				lispAddress = new LispApplicationDataLCAFAddress(
						(byte) 0, protocol, ipTos, localPort, remotePort, address.convertToLispAddress());
				break;
			case LIST:
				
				List<LispAddress> list = new ArrayList<LispAddress>();
				
				for (int i=0;i<addresses.size();i++){
					list.add(addresses.get(i).convertToLispAddress());
				}
				
				lispAddress = new LispListLCAFAddress((byte) 0, list);
				break;
			case SEGMENT:
				byte idMaskLen = 0; //Is this used?
				lispAddress = new LispSegmentLCAFAddress(idMaskLen, instanceId, address.convertToLispAddress());
				break;
			case SOURCE_DEST:
				lispAddress = new LispSourceDestLCAFAddress((byte)0, (short)0, srcMaskLength, dstMaskLength, 
						srcAddress.convertToLispAddress(), dstAddress.convertToLispAddress());
				break;
			case TRAFFIC_ENGINEERING:
				
				List<ReencapHop> listHops = new ArrayList<ReencapHop>();
				ReencapHop hop;
				
				for (int i=0;i<hops.size();i++){
					hop = new ReencapHop(
							hops.get(i).getAddress().convertToLispAddress(), (short) 0, lookup, RLOCProbe, strict);
					listHops.add(hop);
				}
				lispAddress = new LispTrafficEngineeringLCAFAddress((byte)0, listHops);
				break;
			case UNKNOWN:
				break;
			default:
				break;
			}
			break;
		default:
			break;
    	}
    	
    	return lispAddress;
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
	
    public int getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(int iid) {
		this.instanceId = iid;
	}

	public byte[] getMac() {
		return mac;
	}

	public void setMac(byte[] mac) {
		this.mac = mac;
	}

	public int getAsNum() {
		return asNum;
	}

	public void setAsNum(int asNum) {
		this.asNum = asNum;
	}

	public int getLcafType() {
		return lcafType;
	}

	public void setLcafType(int lcafType) {
		this.lcafType = lcafType;
	}

	public byte getProtocol() {
		return protocol;
	}

	public void setProtocol(byte protocol) {
		this.protocol = protocol;
	}

	public int getIPTos() {
		return ipTos;
	}

	public void setIPTos(int iPTos) {
		ipTos = iPTos;
	}

	public short getLocalPort() {
		return localPort;
	}

	public void setLocalPort(short localPort) {
		this.localPort = localPort;
	}

	public short getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(short remotePort) {
		this.remotePort = remotePort;
	}

	public LispAddressGeneric getSrcAddress() {
		return srcAddress;
	}

	public void setSrcAddress(LispAddressGeneric srcAddress) {
		this.srcAddress = srcAddress;
	}

	public LispAddressGeneric getDstAddress() {
		return dstAddress;
	}

	public void setDstAddress(LispAddressGeneric dstAddress) {
		this.dstAddress = dstAddress;
	}

	public byte getSrcMaskLength() {
		return srcMaskLength;
	}

	public void setSrcMaskLength(byte srcMaskLength) {
		this.srcMaskLength = srcMaskLength;
	}

	public byte getDstMaskLength() {
		return dstMaskLength;
	}

	public void setDstMaskLength(byte dstMaskLength) {
		this.dstMaskLength = dstMaskLength;
	}

	public boolean isLookup() {
		return lookup;
	}

	public void setLookup(boolean lookup) {
		this.lookup = lookup;
	}

	public boolean isRLOCProbe() {
		return RLOCProbe;
	}

	public void setRLOCProbe(boolean rLOCProbe) {
		RLOCProbe = rLOCProbe;
	}

	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	public List<LispAddressGeneric> getHops() {
		return hops;
	}

	public void setHops(List<LispAddressGeneric> hops) {
		this.hops = hops;
	}
	
	public LispAddressGeneric getAddress() {
		return address;
	}

	public void setAddress(LispAddressGeneric address) {
		this.address = address;
	}

	public List<LispAddressGeneric> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<LispAddressGeneric> addresses) {
		this.addresses = addresses;
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
