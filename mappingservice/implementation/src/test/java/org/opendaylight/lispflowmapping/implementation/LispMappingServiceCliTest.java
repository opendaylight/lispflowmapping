/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.implementation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import junitx.framework.StringAssert;

import org.jmock.api.Invocation;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.lispflowmapping.implementation.dao.InMemoryDAO;
import org.opendaylight.lispflowmapping.implementation.util.LispAFIConvertor;
import org.opendaylight.lispflowmapping.interfaces.dao.IRowVisitor;
import org.opendaylight.lispflowmapping.tools.junit.BaseTestCase;
import org.opendaylight.lispflowmapping.tools.junit.MockCommandInterpreter;
import org.opendaylight.yang.gen.v1.lispflowmapping.rev131031.lispaddress.lispaddresscontainer.address.Ipv4;

public class LispMappingServiceCliTest extends BaseTestCase {
    private LispMappingService testedLispMappingService;
    private InMemoryDAO dao;
    private Map<Object, Object> visitorExecutions;
    private MockCommandInterpreter mockCommandInterpreter;

    @Override
    @Before
    public void before() throws Exception {
        super.before();

        testedLispMappingService = new LispMappingService();
        dao = context.mock(InMemoryDAO.class);
        inject(testedLispMappingService, "lispDao", dao);
        mockCommandInterpreter = new MockCommandInterpreter();

        visitorExecutions = new HashMap<Object, Object>();
        allowing(dao).getAll(wany(IRowVisitor.class));
        will(new SimpleAction() {
            @Override
            public Object invoke(Invocation invocation) throws Throwable {
                IRowVisitor visitor = (IRowVisitor) invocation.getParameter(0);
                for (Entry<Object, Object> entry : visitorExecutions.entrySet()) {
                    visitor.visitRow(Ipv4.class, entry.getKey(), "IP", entry.getValue());
                }
                return null;
            }
        });
    }

    @Test
    public void remove__Basic() throws Exception {
        mockCommandInterpreter.addArgument("1.2.3.4");
        oneOf(dao).remove(LispAFIConvertor.asIPAfiAddress("1.2.3.4"));
        testedLispMappingService._removeEid(mockCommandInterpreter);
    }

    @Test
    public void oneEntryInDb() throws Exception {
        prepareVisitorResult("1.2.3.4", "9.8.7.6");

        testedLispMappingService._dumpAll(mockCommandInterpreter);

        String console = mockCommandInterpreter.getPrints().toString();
        StringAssert.assertStartsWith("EID\tRLOCs\n", console);
        StringAssert.assertContains("Ipv4", console);
    }

    @Test
    public void twoEntriesInDb() throws Exception {
        prepareVisitorResult("7.2.3.4", "9.8.7.6");
        prepareVisitorResult("1.2.3.4", "9.8.7.6");
        testedLispMappingService._dumpAll(mockCommandInterpreter);

        String console = mockCommandInterpreter.getPrints().toString();
        StringAssert.assertStartsWith("EID\tRLOCs\n", console);
        StringAssert.assertContains("Ipv4", console);
        StringAssert.assertContains("Ipv4", console);
    }

    private void prepareVisitorResult(final String key, final String value) {
        visitorExecutions.put(LispAFIConvertor.asIPAfiAddress(key), LispAFIConvertor.asIPAfiAddress(value));
    }
}
