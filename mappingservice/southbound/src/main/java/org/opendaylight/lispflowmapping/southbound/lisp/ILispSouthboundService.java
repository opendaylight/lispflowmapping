package org.opendaylight.lispflowmapping.southbound.lisp;

import java.net.DatagramPacket;

public interface ILispSouthboundService {

	public DatagramPacket handlePacket(DatagramPacket packet);
	
}
