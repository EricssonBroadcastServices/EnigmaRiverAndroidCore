// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ErrorCodeTest {
    @Test
    public void testUniqueness() throws IllegalAccessException {
        Map<Integer, Field> errorCodes = new HashMap<>();
        for(Field field : ErrorCode.class.getDeclaredFields()) {
            field.setAccessible(true);
            int modifiers = field.getModifiers();
            if(Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && field.getType() == Integer.TYPE) {
                int value = field.getInt(null);
                Field otherField = errorCodes.get(value);
                if(otherField != null) {
                    Assert.fail("Both "+ErrorCode.class.getSimpleName()+"."+otherField.getName()+" and "+ErrorCode.class.getSimpleName()+"."+field.getName()+" have the value "+value);
                } else {
                    errorCodes.put(value, field);
                }
            }
        }
        Assert.assertTrue("Expected more than 10 error codes. Something must be wrong.", errorCodes.size() > 10);
    }
}
