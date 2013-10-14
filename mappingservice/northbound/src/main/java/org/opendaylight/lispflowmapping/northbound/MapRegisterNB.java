/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.northbound;
 
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.LocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapRegister;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddress;
import org.opendaylight.lispflowmapping.type.lisp.address.LispAddressGeneric;

@XmlRootElement(name="MapRegisterNB")
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
	
    public void convertGenericToLispAddresses(){
    	LispAddress EID;
    	LispAddressGeneric EIDGeneric;
    	
    	List<LocatorRecord> locRecordList;
    	LispAddress RLOC;
    	LispAddressGeneric RLOCGeneric;
    	
    	int EIDtoLocatorRecordCount = this.getMapRegister().getEidToLocatorRecords().size();
    	
    	for (int i=0;i<EIDtoLocatorRecordCount;i++){
	    	EIDGeneric = this.getMapRegister().getEidToLocatorRecords().get(i).getPrefixGeneric();
	    	
	    	EID = EIDGeneric.convertToLispAddress();
	    	
	    	this.getMapRegister().getEidToLocatorRecords().get(i).setPrefix(EID);
	    	locRecordList = this.getMapRegister().getEidToLocatorRecords().get(i).getLocators();
	    	
	    	for(int j=0;j<locRecordList.size();j++){
	    		
	    		RLOCGeneric = locRecordList.get(j).getLocatorGeneric();
	    		
	    		RLOC = RLOCGeneric.convertToLispAddress();
	    		locRecordList.get(j).setLocator(RLOC);
	    	}
    	
    	}
    }
    
    public static void convertRecordsLispToGenericAddresses(List<EidToLocatorRecord> eidToLocatorRecords){
    	
    	int EIDtoLocatorRecordCount = eidToLocatorRecords.size();
    	
    	for (int i=0;i<EIDtoLocatorRecordCount;i++){
	    	convertRecordFromLispToGenericAddresses(eidToLocatorRecords.get(i));
    	}
    }
    
    public static void convertRecordFromLispToGenericAddresses(EidToLocatorRecord record){
    	LispAddress EID;    	
    	List<LocatorRecord> locRecordList;
    	LispAddress RLOC;
    	
    	EID = record.getPrefix();
	    record.setPrefixGeneric(new LispAddressGeneric(EID));
	    	
	    locRecordList = record.getLocators();
	    for(int j=0;j<locRecordList.size();j++){
	    	
	    	RLOC = locRecordList.get(j).getLocator();
	    	locRecordList.get(j).setLocatorGeneric(new LispAddressGeneric(RLOC));
	    }
    }
    
}