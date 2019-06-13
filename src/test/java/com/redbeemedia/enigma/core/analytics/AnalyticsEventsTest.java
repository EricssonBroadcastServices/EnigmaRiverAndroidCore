package com.redbeemedia.enigma.core.analytics;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class AnalyticsEventsTest {
    @Test
    public void testAnalyticsEventsNames() throws IllegalAccessException {
        Set<String> uniqueEventNames = new HashSet<>();
        forEachDeclaredStaticFieldOfType(AnalyticsEvents.class, IAnalyticsEventType.class, field -> {
            String fieldRef = AnalyticsEvents.class.getSimpleName()+"."+field.getName();
            int modifiers = field.getModifiers();

            Assert.assertTrue(fieldRef+" is not declared final", Modifier.isFinal(modifiers));
            Assert.assertTrue(fieldRef+" is not declared public",Modifier.isPublic(modifiers));
            String eventName = IAnalyticsEventType.class.cast(field.get(null)).getName();
            Assert.assertFalse(fieldRef+".getName() returns "+eventName+" which is also returned by a different event.", uniqueEventNames.contains(eventName));
            uniqueEventNames.add(eventName);
        });
    }

    @Test
    public void testAnalyticsEventProperties() throws IllegalAccessException {
        for(Class<?> declaredClass : AnalyticsEvents.class.getDeclaredClasses()) {
            if(IAnalyticsEventType.class.isAssignableFrom(declaredClass) && !Modifier.isAbstract(declaredClass.getModifiers())) {
                forEachDeclaredField(declaredClass, field -> {
                    String fieldRef = AnalyticsEvents.class.getSimpleName()+"."+declaredClass.getSimpleName()+"."+field.getName();
                    int modifiers = field.getModifiers();
                    if(IEventProperty.class.isAssignableFrom(field.getType())) {
                        Assert.assertFalse(fieldRef+" should not be declared static", Modifier.isStatic(modifiers));
                        Assert.assertTrue(fieldRef+" is not declared final", Modifier.isFinal(modifiers));
                        Assert.assertTrue(fieldRef+" is not declared public",Modifier.isPublic(modifiers));

                        Type type = field.getGenericType();
                        Assert.assertTrue(type instanceof ParameterizedType);
                        ParameterizedType parameterizedType = (ParameterizedType) type;
                        Type[] genericArguments = parameterizedType.getActualTypeArguments();
                        Type ownerEvent = genericArguments[0];
                        String fieldTypeRef = field.getType().getSimpleName()+"<"+((Class<?>) genericArguments[0]).getSimpleName()+",...>";
                        Assert.assertEquals("EventProperties should belong to the class they "
                                        +"are declared in. "+fieldRef+" has "+fieldTypeRef
                                        +" but is declared inside "+declaredClass.getSimpleName()+".",
                                        declaredClass,ownerEvent);
                    }
                });
            }
        }
    }

    private interface IFieldAction {
        void onField(Field field) throws IllegalAccessException;
    }

    private static void forEachDeclaredField(Class<?> clazz, IFieldAction action) throws IllegalAccessException {
        for(Field field : clazz.getDeclaredFields()) {
            final boolean accessible = field.isAccessible();
            try {
                field.setAccessible(true);
                action.onField(field);
            } finally {
                field.setAccessible(accessible);
            }
        }
    }

    private static void forEachDeclaredStaticFieldOfType(Class<?> clazz, Class<?> fieldSuperType, IFieldAction action) throws IllegalAccessException {
        forEachDeclaredField(clazz, field -> {
            if(Modifier.isStatic(field.getModifiers()) && fieldSuperType.isAssignableFrom(field.getType())) {
                action.onField(field);
            }
        });
    }
}
