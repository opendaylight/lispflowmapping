/*
 * Copyright (c) 2014 Contextream, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.lispflowmapping.tools.junit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.internal.InvocationExpectationBuilder;
import org.jmock.internal.ReturnDefaultValueAction;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.jmock.syntax.ReceiverClause;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseExpectations extends Expectations {
    protected Mockery context;
    protected boolean showAllExpectations;
    private final Synchroniser synchroniser = new Synchroniser();
    protected static final Logger LOG = LoggerFactory.getLogger(BaseExpectations.class);

    @Before
    public void before() throws Exception {
        context = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
                // otherwise we get errors on the finalizer thread:
                setThreadingPolicy(synchroniser);
            }
        };
        defaultAction = new ReturnDefaultValueAction(ClassImposteriser.INSTANCE);
    }

    @After
    public void after() throws Exception {
        context.assertIsSatisfied();
    }

    // New suger syntax

    protected final void ret(Object result) {
        will(returnValue(result));
    }

    protected final void rethrow(Throwable throwable) {
        will(throwException(throwable));
    }

    protected final void retField(Object container, String fieldName) throws Exception {
        will(new ReturnField(container, fieldName));
    }

    protected final void rets(Object... results) {
        will(returnValues(results));
    }

    protected final <T> T wany(Class<T> type) {
        return with(any(type));
    }

    protected final int wanyint() {
        return with(any(int.class));
    }

    protected final long wanylong() {
        return with(any(long.class));
    }

    protected final boolean wanybool() {
        return with(any(boolean.class));
    }

    protected final <T> T wsame(T instance) {
        return with(same(instance));
    }

    protected final <T> T weq(T instance) {
        return with(equal(instance));
    }

    protected final short weq(short instance) {
        return with(equal(instance));
    }

    protected final int weq(int instance) {
        return with(equal(instance));
    }

    protected final long weq(long instance) {
        return with(equal(instance));
    }

    protected final boolean weq(boolean instance) {
        return with(equal(instance));
    }

    public static Action returnValues(Object... result) {
        return new ActionSequenceValue(result);
    }

    protected void retMethod(final String methodName) {
        will(new MethodAction(this, methodName));
    }

    private static class MethodAction implements Action {
        private Object testInstance;
        private String methodName;
        private Method method;

        public MethodAction(Object testInstance, String methodName) {
            this.testInstance = testInstance;
            this.methodName = methodName;
            method = findMethod(testInstance.getClass());
            Assert.assertNotNull("Cannot locate '" + methodName + "'", method);
        }

        private Method findMethod(Class<? extends Object> clazz) {
            if (Object.class.equals(clazz)) {
                return null;
            }
            try {
                return clazz.getMethod(methodName, Invocation.class);
            } catch (SecurityException e) {
                LOG.debug("Catched SecurityException", e);
            } catch (NoSuchMethodException e) {
                LOG.debug("Catched NoSuchMethodException", e);
            }

            return findMethod(clazz.getSuperclass());
        }

        public void describeTo(Description arg0) {
            arg0.appendText("running " + methodName);
        }

        public Object invoke(Invocation invocation) throws Throwable {
            Exception e = null;
            try {
                return method.invoke(testInstance, invocation);
            } catch (SecurityException se) {
                e = se;
            } catch (IllegalAccessException iae) {
                e = iae;
            } catch (InvocationTargetException ite) {
                throw ite.getTargetException();
            }
            Assert.fail("Got " + e.getMessage() + " while trying to execute '" + methodName + "'");
            return null;
        }

    }

    private static class ActionSequenceValue implements Action {
        private final Object[] values;
        private int i = 0;

        public ActionSequenceValue(Object... values) {
            this.values = values;
        }

        public Object invoke(Invocation invocation) throws Throwable {
            if (i < values.length) {
                return values[i++];
            }
            throw new RuntimeException("no morCxtrmExpectationse actions available: " + invocation);
        }

        public void describeTo(Description description) {
            description.appendText(", and then ").appendText(Arrays.toString(values));
        }
    }

    protected static final class ReturnField implements Action {
        private final Object container;
        private final Field field;

        public ReturnField(Object container, String fieldName) throws Exception {
            this.container = container;
            field = container.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
        }

        public void describeTo(Description description) {
            description.appendText("return " + container.getClass().getName() + "." + field.getName());
        }

        public Object invoke(Invocation invocation) throws Throwable {
            return field.get(container);
        }
    }

    public static class ValueSaverAction<T> extends BaseMatcher<T> implements Action {
        private final String logMatch;
        private final boolean logInvocation;
        public T lastValue;
        public final List<T> values = new ArrayList<T>();

        public ValueSaverAction() {
            this(null, false);
        }

        public ValueSaverAction(String logMatch, boolean logInvocation) {
            this.logMatch = logMatch;
            this.logInvocation = logInvocation;
        }

        @SuppressWarnings("unchecked")
        public final boolean matches(Object arg) {
            T value = (T) arg;
            if (!validate(value)) {
                return false;
            }
            lastValue = transformOnMatch(value);
            values.add(lastValue);
            boolean match = match(lastValue);
            if (match && (logMatch != null)) {
                LOG.trace("Match: " + logMatch + " " + value);
            }
            return match;
        }

        protected T onMatch(T value) {
            return value;
        }

        protected boolean match(T lastValue2) {
            return true;
        }

        protected boolean validate(T value) {
            return true;
        }

        protected T transformOnMatch(T value) {
            return value;
        }

        public void describeTo(Description arg0) {
        }

        public Object invoke(Invocation invocation) throws Throwable {
            if (logInvocation) {
                LOG.trace("Invoke: returning " + lastValue);
            }
            return lastValue;
        }

        @Override
        public String toString() {
            return "ValueSavers: " + values.toString();
        }
    }

    @SafeVarargs
    protected final static <T> Matcher<T[]> contains(final T... expected) {
        return new BaseMatcher<T[]>() {
            @SuppressWarnings("unchecked")
            public boolean matches(Object actual) {
                T[] arr = (T[]) actual;
                if (arr.length != expected.length) {
                    return false;
                }
                for (T expectedInstance : expected) {
                    boolean found = false;
                    for (int j = 0; (j < arr.length) && !found; j++) {
                        found = (arr[j] == expectedInstance);
                    }
                    if (!found) {
                        return false;
                    }
                }
                return true;
            }

            public void describeTo(Description arg0) {
            }
        };
    }

    @SafeVarargs
    protected final static <T> Matcher<T[]> sameArbitraryArray(final T... expectedArr) {
        return new TypeSafeMatcher<T[]>() {
            @Override
            public boolean matchesSafely(T[] actualArr) {
                Set<T> expected = new HashSet<T>();
                for (T val : expectedArr) {
                    expected.add(val);
                }
                Set<T> actual = new HashSet<T>();
                actual.addAll(Arrays.asList(actualArr));
                return actual.equals(expected);
            }

            public void describeTo(Description description) {
                description.appendText("Same arbitrary array as " + Arrays.toString(expectedArr));
            }
        };
    }

    @SafeVarargs
    protected final static <T> Matcher<T[]> doseNotContain(final T... forbiddenValues) {
        return new TypeSafeMatcher<T[]>() {
            @Override
            public boolean matchesSafely(T[] arr) {
                for (T forbiddenInstance : forbiddenValues) {
                    for (T element : arr) {
                        if (element == forbiddenInstance) {
                            return false;
                        }
                    }
                }
                return true;
            }

            public void describeTo(Description description) {
            }
        };
    }

    public class StringArrayMatcher extends BaseMatcher<String[]> {
        private final Object[] expected;

        /**
         * @param expected
         *            null are considered "any"
         */
        private StringArrayMatcher(Object... expected) {
            this.expected = expected;
        }

        public boolean matches(Object item) {
            if (!(item instanceof String[])) {
                return false;
            }
            String[] actual = (String[]) item;
            if (expected.length != actual.length) {
                return false;
            }
            for (int i = 0; i < expected.length; i++) {
                if ((expected[i] != null) && !expected[i].toString().equals(actual[i])) {
                    return false;
                }
            }
            return true;
        }

        public void describeTo(Description description) {
            description.appendText("String" + Arrays.toString(expected));
        }
    }

    /**
     * @param expected
     *            null are considered "any"
     */
    public final String[] wStrArr(Object... expected) {
        return with(new StringArrayMatcher(expected));
    }

    // ////////////////////////////////////////////////////////////
    // Make expectations work in our new ConteXtream better way //
    // ////////////////////////////////////////////////////////////
    private ReturnDefaultValueAction defaultAction;

    protected InvocationExpectationBuilder getCurrentBuilder() {
        try {
            return currentBuilder();
        } catch (IllegalStateException e) {
            LOG.debug("Catched IllegalStateException", e);
            return null;
        }
    }

    private void addCurrentExpectation() {
        context.addExpectation(currentBuilder().toExpectation(defaultAction));
    }

    @Override
    public ReceiverClause exactly(int count) {
        ReceiverClause ret = super.exactly(count);
        addCurrentExpectation();
        return ret;
    }

    @Override
    public ReceiverClause atLeast(int count) {
        ReceiverClause ret = super.atLeast(count);
        addCurrentExpectation();
        return ret;
    }

    @Override
    public ReceiverClause between(int minCount, int maxCount) {
        ReceiverClause ret = super.between(minCount, maxCount);
        addCurrentExpectation();
        return ret;
    }

    @Override
    public ReceiverClause atMost(int count) {
        ReceiverClause ret = super.atMost(count);
        addCurrentExpectation();
        return ret;
    }

}
