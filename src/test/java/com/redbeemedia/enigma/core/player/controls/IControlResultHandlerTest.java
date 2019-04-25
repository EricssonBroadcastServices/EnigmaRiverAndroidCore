package com.redbeemedia.enigma.core.player.controls;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class IControlResultHandlerTest {
    @Test
    public void testRejectReasonTypeNames() throws IllegalAccessException {
        Class<IControlResultHandler.RejectReasonType> rejectReasonTypeClass = IControlResultHandler.RejectReasonType.class;
        for(Field field : rejectReasonTypeClass.getDeclaredFields()) {
            boolean accessible = field.isAccessible();
            try {
                field.setAccessible(true);
                int modifier = field.getModifiers();
                if(Modifier.isPublic(modifier) && Modifier.isStatic(modifier)) {
                    if(rejectReasonTypeClass.equals(field.getType())) {
                        Assert.assertTrue("Expected \"public static *final* "+ rejectReasonTypeClass.getSimpleName()+" "+field.getName()+" = ...\"", Modifier.isFinal(modifier));
                        IControlResultHandler.RejectReasonType rejectReasonType = rejectReasonTypeClass.cast(field.get(null));
                        Assert.assertEquals("Field name and toString()-value does not match",field.getName(), rejectReasonType.toString());
                    }
                }
            } finally {
                field.setAccessible(accessible);
            }
        }
    }
}
