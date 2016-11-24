/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.tools.junit;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTestCase extends BaseExpectations {
    protected static final Logger LOG = LoggerFactory.getLogger(BaseTestCase.class);

    protected InetAddress asInet(int intValue) {
        try {
            return InetAddress.getByAddress(ByteBuffer.allocate(4).putInt(intValue).array());
        } catch (UnknownHostException e) {
            LOG.debug("Unknown host {}", ByteBuffer.allocate(4).putInt(intValue).array(), e);
            fail("");
        }
        return null;
    }

    public static void fail(String message) {
        Assert.fail(message);
    }

    public static void fail() {
        Assert.fail();
    }

    public static void assertNotNull(String message, Object object) {
        Assert.assertNotNull(message, object);
    }

    public static void assertNotNull(Object object) {
        Assert.assertNotNull(object);
    }

    public static void assertNull(Object message, Object object) {
        Assert.assertNull(message.toString(), object);
    }

    public static void assertNull(Object object) {
        Assert.assertNull("" + object, object);
    }

    public static void assertSame(String message, Object expected, Object actual) {
        Assert.assertSame(message, expected, actual);
    }

    public static void assertSame(Object expected, Object actual) {
        Assert.assertSame(expected, actual);
    }

    public static void assertNotSame(String message, Object unexpected, Object actual) {
        Assert.assertNotSame(message, unexpected, actual);
    }

    public static void assertNotSame(Object unexpected, Object actual) {
        Assert.assertNotSame(unexpected, actual);
    }

    protected Object inject(Object testedObject, String memberName, Object member) throws Exception {
        assertNotNull(testedObject);
        assertNotNull(memberName);
        assertNotNull(member);

        Field field = null;
        for (Class<?> cls = testedObject.getClass(); (cls != null) && (field == null); cls = cls.getSuperclass()) {
            field = cls.getDeclaredField(memberName);
        }
        assertNotNull("Couldn't find member '" + memberName + "' in " + testedObject.getClass().getSimpleName(), field);
        return inject(testedObject, new FieldData(field, member));
    }

    protected Object injectStatic(Class<?> clazz, String memberName, Object member) throws Exception {
        assertNotNull(clazz);
        assertNotNull(memberName);
        assertNotNull(member);

        Field field = null;
        for (Class<?> cls = clazz; (cls != null) && (field == null); cls = cls.getSuperclass()) {
            field = cls.getDeclaredField(memberName);
        }
        assertNotNull("Couldn't find member '" + memberName + "' in " + clazz.getSimpleName(), field);
        return inject(null, new FieldData(field, member));
    }

    protected Object inject(Object testedObject, FieldData fieldData) {
        assertNotNull(fieldData.field);
        Field field = fieldData.field;
        if (fieldData.value != null) {
            try {
                field.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                field.set(testedObject, fieldData.value);
                return fieldData.value;
            } catch (Exception e) {
                LOG.debug("Caught generic exception", e);
                fail(e.getMessage());
            }
        }
        return null;
    }

    protected static final class FieldData {
        public final Field field;
        public Object value;

        public FieldData(Field field, Object value) {
            assertNotNull(field);
            this.field = field;
            this.value = value;
        }
    }

    protected abstract class SimpleAction implements Action {
        public void describeTo(Description arg0) {
        }

        public abstract Object invoke(Invocation invocation) throws Throwable;
    }

    protected static ByteBuffer hexToByteBuffer(String hex) {
        String[] hexBytes = hex.split(" ");
        ByteBuffer bb = ByteBuffer.allocate(hexBytes.length);
        for (String hexByte : hexBytes) {
            bb.put((byte) Integer.parseInt(hexByte, 16));
        }
        bb.clear();
        return bb;
    }

    protected static void assertHexEquals(short expected, short actual) {
        assertEquals(String.format("0x%04X", expected), String.format("0x%04X", actual));
    }

    protected static void assertHexEquals(byte expected, byte actual) {
        assertEquals(String.format("0x%02X", expected), String.format("0x%02X", actual));
    }
}
