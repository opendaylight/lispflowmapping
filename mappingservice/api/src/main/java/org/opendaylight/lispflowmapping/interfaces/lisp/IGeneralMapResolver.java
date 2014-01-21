/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.interfaces.lisp;

/**
 * The general methods of the map resolver
 */
public interface IGeneralMapResolver {
    /**
     * @return Should the map resolver use masking.
     */
    boolean shouldIterateMask();

    /**
     * @return Should the map reslover use authentication
     */
    boolean shouldAuthenticate();

    void setShouldIterateMask(boolean iterateMask);

    void setShouldAuthenticate(boolean shouldAuthenticate);
}
