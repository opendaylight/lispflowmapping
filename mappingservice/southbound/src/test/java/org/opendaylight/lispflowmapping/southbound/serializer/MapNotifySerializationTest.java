/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.southbound.serializer;

import static junit.framework.Assert.assertEquals;

import java.nio.ByteBuffer;

import junitx.framework.ArrayAssert;

import org.junit.Test;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.type.lisp.EidToLocatorRecord;
import org.opendaylight.lispflowmapping.type.lisp.MapNotify;
import org.opendaylight.lispflowmapping.type.lisp.address.LispIpv4Address;
import org.opendaylight.lispflowmapping.type.lisp.address.LispNoAddress;

public class MapNotifySerializationTest extends BaseTestCase {

	@Test
	public void serialize__Fields() throws Exception {
		MapNotify mn = new MapNotify();
		mn.addEidToLocator(new EidToLocatorRecord()
				.setPrefix(new LispIpv4Address(1)));

		mn.addEidToLocator(new EidToLocatorRecord()
				.setPrefix(new LispIpv4Address(73)));
		mn.setNonce(6161616161L);
		mn.setKeyId((short) 0x0001);
		byte[] authenticationData = new byte[] { (byte) 0x16, (byte) 0x98,
				(byte) 0x96, (byte) 0xeb, (byte) 0x88, (byte) 0x2d,
				(byte) 0x4d, (byte) 0x22, (byte) 0xe5, (byte) 0x8f,
				(byte) 0xe6, (byte) 0x89, (byte) 0x64, (byte) 0xb9,
				(byte) 0x17, (byte) 0xa4, (byte) 0xba, (byte) 0x4e,
				(byte) 0x8c, (byte) 0x41 };
		mn.setAuthenticationData(authenticationData);

		ByteBuffer bb = MapNotifySerializer.getInstance().serialize(mn);
		assertHexEquals((byte) 0x40, bb.get()); // Type + MSByte of reserved
		assertEquals(0, bb.getShort()); // Rest of reserved
		assertEquals(2, bb.get()); // Record Count
		assertEquals(6161616161L, bb.getLong()); // Nonce
		assertHexEquals((short) 0x0001, bb.getShort()); // Key ID
		assertEquals(authenticationData.length, bb.getShort());

		byte[] actualAuthenticationData = new byte[20];
		bb.get(actualAuthenticationData);
		ArrayAssert.assertEquals(authenticationData, actualAuthenticationData);

		bb.position(bb.position() + 12); /* EID in first record */
		assertEquals(0x1, bb.getInt());

		bb.position(bb.position() + 12); /* EID in second record */
		assertEquals(73, bb.getInt());

		assertEquals(bb.position(), bb.capacity());
	}

	@Test
	public void serialize__NoAuthenticationData() throws Exception {
		MapNotify mn = new MapNotify();
		mn.addEidToLocator(new EidToLocatorRecord().setPrefix(
				new LispIpv4Address(1)).setRecordTtl(55));

		ByteBuffer bb = MapNotifySerializer.getInstance().serialize(mn);
		bb.position(bb.position() + 14); // jump to AuthenticationDataLength
		assertEquals(0, bb.getShort());
		assertEquals(55, bb.getInt());

		mn.setAuthenticationData(null);

		bb = MapNotifySerializer.getInstance().serialize(mn);
		bb.position(bb.position() + 14); // jump to AuthenticationDataLength
		assertEquals(0, bb.getShort());
		assertEquals(55, bb.getInt());

		mn.setAuthenticationData(new byte[0]);

		bb = MapNotifySerializer.getInstance().serialize(mn);
		bb.position(bb.position() + 14); // jump to AuthenticationDataLength
		assertEquals(0, bb.getShort());
		assertEquals(55, bb.getInt());
	}

	@Test
	public void serialize__NoPrefixInEidToLocator() throws Exception {
		MapNotify mn = new MapNotify();
		mn.addEidToLocator(new EidToLocatorRecord());
		mn.addEidToLocator(new EidToLocatorRecord().setPrefix(null));
		mn.addEidToLocator(new EidToLocatorRecord()
				.setPrefix(new LispNoAddress()));

		ByteBuffer bb = MapNotifySerializer.getInstance().serialize(mn);
		bb.position(bb.position() + 26); // jump to first record prefix AFI
		assertEquals(0, bb.getShort());

		bb.position(bb.position() + 10); // jump to second record prefix AFI
		assertEquals(0, bb.getShort());

		bb.position(bb.position() + 10); // jump to third record prefix AFI
		assertEquals(0, bb.getShort());
	}
}
