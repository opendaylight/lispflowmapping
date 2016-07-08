/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.lisp.authentication;

import java.nio.ByteBuffer;

public interface ILispAuthentication {

    boolean validate(ByteBuffer mapRegisterBuffer, byte[] expectedAuthData, String key);

    byte[] getAuthenticationData(ByteBuffer buffer, String key);

    int getAuthenticationLength();

    int MAP_REGISTER_AND_NOTIFY_AUTHENTICATION_POSITION = 16;

}
