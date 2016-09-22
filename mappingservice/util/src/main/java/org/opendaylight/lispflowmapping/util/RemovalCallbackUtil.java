/*
 * Copyright (c) 2015 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.util;

import org.opendaylight.lispflowmapping.interfaces.dao.ILispDAO;

/**
 * Created by sheikahm on 10/5/16.
 */
public class RemovalCallbackUtil {
    private RemovalCallbackUtil() {
    }

    public static Runnable mapCacheRemovalCallback(ILispDAO dao, Object key, String subKey) {
        return () -> {
            dao.removeSpecific(key, subKey);
        };
    }

    public static Runnable join(Runnable firstCallable, Runnable secondCallable) {
        return () -> {
            //this function is all about relative order
            firstCallable.run();
            secondCallable.run();
        };
    }

    public static Runnable soundBoundMapCacheRemovalCallback(ILispDAO dao, Object key, String subKey,
                                                             Runnable dsbeDataRemovalCallback) {
        return join(mapCacheRemovalCallback(dao, key, subKey), dsbeDataRemovalCallback);
    }
}
