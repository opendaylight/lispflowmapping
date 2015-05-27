/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp.address;

import java.util.ArrayList;
import java.util.Arrays;
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
    short localPortLow;

	@XmlElement
    short localPortHigh;

    @XmlElement
    short remotePortLow;

    @XmlElement
    short remotePortHigh;

	@XmlElement
	byte iidMaskLength;

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

	@XmlElement
	String distinguishedName;


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
		default:
			throw new IllegalArgumentException("AFI " + afi +
					" not supported by this constructor: LispAddressGeneric(int afi, String address)");
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
			ipAddress = ipv4Address.getAddress().getHostAddress();
			break;
		case AS:
			LispASAddress asAddress = (LispASAddress) lispAddress;
			asNum = asAddress.getAS();
			break;
		case IP6:
			LispIpv6Address ipv6Address = (LispIpv6Address) lispAddress;
			ipAddress = ipv6Address.getAddress().getHostAddress();
			break;
		case MAC:
			LispMACAddress macAddress = (LispMACAddress) lispAddress;
			mac = macAddress.getMAC();
			break;
		case DISTINGUISHED_NAME:
			LispDistinguishedNameAddress distinguishedNameAddress = (LispDistinguishedNameAddress) lispAddress;
			distinguishedName = distinguishedNameAddress.getDistinguishedName();
			break;
		case LCAF:
			LispLCAFAddress lcafAddress = (LispLCAFAddress) lispAddress;
			lcafType = lcafAddress.getType().getLispCode();

			switch (lcafAddress.getType()){
			case APPLICATION_DATA:
				applicationDataGeneric(lcafAddress);
				break;
			case LIST:
				listGeneric(lcafAddress);
				break;
			case SEGMENT:
				segmentGeneric(lcafAddress);
				break;
			case SOURCE_DEST:
				srcDstGeneric(lcafAddress);
				break;
			case TRAFFIC_ENGINEERING:
				trafficEngineeringGeneric(lcafAddress);
				break;
			default:
				throw new IllegalArgumentException("LCAF type " + lcafAddress.getType() +
						" not supported by this constructor: LispAddressGeneric(LispAddress lispAddress)");
			}
			break;
		default:
			throw new IllegalArgumentException("AFI " + afi +
					" not supported by this constructor: LispAddressGeneric(LispAddress lispAddress)");
		}
	}

	private void applicationDataGeneric(LispLCAFAddress lcafAddress){
		LispApplicationDataLCAFAddress appDataAddress = (LispApplicationDataLCAFAddress) lcafAddress;
	    protocol = appDataAddress.getProtocol();
	    ipTos = appDataAddress.getIPTos();
	    localPortLow = appDataAddress.getLocalPortLow();
        localPortLow = appDataAddress.getLocalPortHigh();
	    remotePortLow = appDataAddress.getRemotePortLow();
        remotePortHigh = appDataAddress.getRemotePortHigh();
	    address = new LispAddressGeneric(appDataAddress.getAddress());
	}

	private void listGeneric(LispLCAFAddress lcafAddress){
		LispListLCAFAddress listAddress = (LispListLCAFAddress) lcafAddress;
		addresses = new ArrayList<LispAddressGeneric>();
		for(int i=0;i<listAddress.getAddresses().size();i++){
			addresses.add(new LispAddressGeneric(listAddress.getAddresses().get(i)));
		}
	}

	private void segmentGeneric(LispLCAFAddress lcafAddress){
		LispSegmentLCAFAddress segmentAddress = (LispSegmentLCAFAddress) lcafAddress;
		instanceId = segmentAddress.getInstanceId();
		address = new LispAddressGeneric(segmentAddress.getAddress());
		iidMaskLength = segmentAddress.getIdMaskLen();
	}

	private void srcDstGeneric(LispLCAFAddress lcafAddress){
		LispSourceDestLCAFAddress srcDstAddress = (LispSourceDestLCAFAddress) lcafAddress;
	    srcAddress = new LispAddressGeneric(srcDstAddress.getSrcAddress());
	    dstAddress = new LispAddressGeneric(srcDstAddress.getDstAddress());
	    srcMaskLength = srcDstAddress.getSrcMaskLength();
	    dstMaskLength = srcDstAddress.getDstMaskLength();
	}

	private void trafficEngineeringGeneric(LispLCAFAddress lcafAddress){
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
		this.mac = Arrays.copyOf(mac, mac.length);
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

	public short getLocalPortLow() {
		return localPortLow;
	}

	public void setLocalPortLow(short localPortLow) {
		this.localPortLow = localPortLow;
	}

    public short getLocalPortHigh() {
        return localPortHigh;
    }

    public void setLocalPortHigh(short localPortHigh) {
        this.localPortHigh = localPortHigh;
    }

	public short getRemotePortLow() {
		return remotePortLow;
	}

	public void setRemotePortLow(short remotePortLow) {
		this.remotePortLow = remotePortLow;
	}

    public short getRemotePortHigh() {
        return remotePortHigh;
    }

    public void setRemotePortHigh(short remotePortHigh) {
        this.remotePortHigh = remotePortHigh;
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

    public byte getIidMaskLength() {
        return iidMaskLength;
    }

    public void setIidMaskLength(byte iidMaskLength) {
        this.iidMaskLength = iidMaskLength;
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


	public int getIpTos() {
		return ipTos;
	}


	public void setIpTos(int ipTos) {
		this.ipTos = ipTos;
	}


	public String getDistinguishedName() {
		return distinguishedName;
	}


	public void setDistinguishedName(String distinguishedName) {
		this.distinguishedName = distinguishedName;
	}


}
