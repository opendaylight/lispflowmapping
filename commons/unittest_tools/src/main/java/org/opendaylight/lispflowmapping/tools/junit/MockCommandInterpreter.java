/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.tools.junit;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.osgi.framework.Bundle;

public class MockCommandInterpreter implements CommandInterpreter {
    private StringBuilder prints = new StringBuilder();
    private List<String> arguments = new ArrayList<String>();
    private Iterator<String> argIterator;

    public StringBuilder getPrints() {
        return prints;
    }

    public void addArgument(String arg) {
        arguments.add(arg);
    }

    public String nextArgument() {
        if (argIterator == null) {
            argIterator = arguments.iterator();
        }

        return argIterator.next();
    }

    public Object execute(String cmd) {
        // TODO Auto-generated method stub
        return null;
    }

    public void print(Object o) {
        prints.append(o);
    }

    public void println() {
        prints.append("\n");
    }

    public void println(Object o) {
        prints.append(o).append("\n");
    }

    public void printStackTrace(Throwable t) {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("rawtypes")
    public void printDictionary(Dictionary dic, String title) {
        // TODO Auto-generated method stub

    }

    public void printBundleResource(Bundle bundle, String resource) {
        // TODO Auto-generated method stub

    }
}
