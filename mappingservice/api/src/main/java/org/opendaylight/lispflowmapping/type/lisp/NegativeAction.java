/*
 * Copyright (c) 2013 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.type.lisp;

public enum NegativeAction {
    NoAction(0), //
    NativelyForward(1), //
    SendMapRequest(2), //
    Drop(3);

    int value;

    private NegativeAction(int value) {
        this.value = value;
    }
}
