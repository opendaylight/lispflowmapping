/*
 * Copyright (c) 2017 Cisco Systems, Inc.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.lispflowmapping.neutron.intenthandler;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executors;

/**
 * Created by Shakib Ahmed on 1/24/17.
 */
public final class IntentHandlerAsyncExecutorProvider {
    private static ListeningExecutorService listeningExecutorService;

    private static final int EXTRA_THREADS_TO_HANDLE_VPP_LISTENER = 1;

    private static IntentHandlerAsyncExecutorProvider intentHandlerAsyncExecutorProvider;

    private IntentHandlerAsyncExecutorProvider() {
        listeningExecutorService = MoreExecutors
                .listeningDecorator(Executors.newFixedThreadPool(EXTRA_THREADS_TO_HANDLE_VPP_LISTENER));
    }

    public static synchronized IntentHandlerAsyncExecutorProvider getInstace() {
        if (intentHandlerAsyncExecutorProvider == null) {
            intentHandlerAsyncExecutorProvider = new IntentHandlerAsyncExecutorProvider();
        }
        return intentHandlerAsyncExecutorProvider;
    }

    public synchronized ListeningExecutorService getExecutor() {
        return listeningExecutorService;
    }

    public void close() {
        listeningExecutorService.shutdown();
    }
}
