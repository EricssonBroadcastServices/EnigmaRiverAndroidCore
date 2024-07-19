// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.restriction.BasicContractRestriction;
import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.restriction.IContractRestrictionsValueSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/*package-protected*/ class EnigmaContractRestrictions implements IContractRestrictions  {
    private static final ContractRestrictionsDefaults DEFAULTS_FOR_RESTRICTIONS = new ContractRestrictionsDefaults();

    private final IContractRestrictionsValueSource valueSource;

    public EnigmaContractRestrictions(IContractRestrictionsValueSource valueSource) {
        if(valueSource != null) {
            this.valueSource = valueSource;
        } else {
            this.valueSource = new EmptyValueSource();
        }
    }

    @Override
    public <T> T getValue(IContractRestriction<T> restriction, T fallback) {
        T value = restriction.getValue(valueSource);
        if(value != null) {
            return value;
        } else {
            return fallback;
        }
    }

    private static class EmptyValueSource implements IContractRestrictionsValueSource {
        @Override
        public <T> boolean hasValue(String name, Class<T> type) {
            return false;
        }

        @Override
        public <T> T getValue(String name, Class<T> type) {
            return null;
        }
    }

    public static EnigmaContractRestrictions createWithDefaults(JSONObject contractRestrictions) {
        return new EnigmaContractRestrictions(DEFAULTS_FOR_RESTRICTIONS.overrideWith(new JsonObjectValueSource(contractRestrictions)));
    }


    private static class ContractRestrictionsDefaults implements IContractRestrictionsValueSource {
        private final Map<String, Object> defaults = new HashMap<>();

        public ContractRestrictionsDefaults() {
            addDefault(ContractRestriction.FASTFORWARD_ENABLED, true);
            addDefault(ContractRestriction.REWIND_ENABLED, true);
            addDefault(ContractRestriction.TIMESHIFT_ENABLED, true);
        }

        private <T> void addDefault(BasicContractRestriction<T> contractRestriction, T defaultValue) {
            defaults.put(contractRestriction.getPropertyName(), defaultValue);
        }

        @Override
        public <T> boolean hasValue(String name, Class<T> type) {
            if(defaults.containsKey(name)) {
               return getValue(name, type) != null;
            }
            return false;
        }

        @Override
        public <T> T getValue(String name, Class<T> type) {
            try {
                return type.cast(defaults.get(name));
            } catch (ClassCastException e) {
                return null;
            }
        }

        public IContractRestrictionsValueSource overrideWith(final IContractRestrictionsValueSource primary) {
            return new IContractRestrictionsValueSource() {
                @Override
                public <T> boolean hasValue(String name, Class<T> type) {
                    return primary.hasValue(name, type) || ContractRestrictionsDefaults.this.hasValue(name, type);
                }

                @Override
                public <T> T getValue(String name, Class<T> type) {
                    T value = primary.getValue(name, type);
                    if(value == null) {
                        value = ContractRestrictionsDefaults.this.getValue(name, type);
                    }
                    return value;
                }
            };
        }
    }
}

/*package-protected*/ class JsonObjectValueSource implements IContractRestrictionsValueSource {
    private final JSONObject jsonObject;

    public JsonObjectValueSource(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    @Override
    public <T> boolean hasValue(String name, Class<T> type) {
        if(jsonObject == null) {
            return false;
        }
        try {
            T value = getValueInternal(name, type);
            return value != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public <T> T getValue(String name, Class<T> type) {
        if(jsonObject == null) {
            return null;
        }
        return getValueInternal(name, type);
    }

    private <T> T getValueInternal(String name, Class<T> type) {
        if(!jsonObject.has(name)) {
            return null;
        }
        try {
            try {
                return type.cast(jsonObject.get(name));
            } catch (ClassCastException e) {
                if(String.class.equals(type)) {
                    return type.cast(jsonObject.getString(name));
                } else if(Integer.class.equals(type)) {
                    return type.cast(Integer.parseInt(jsonObject.getString(name))); //Accounts for overflow
                } else if(Boolean.class.equals(type)) {
                    return type.cast(jsonObject.getBoolean(name));
                } else if(Double.class.equals(type)) {
                    return type.cast(jsonObject.getDouble(name));
                } else if(JSONArray.class.equals(type)) {
                    return type.cast(jsonObject.getJSONArray(name));
                } else if(JSONObject.class.equals(type)) {
                    return type.cast(jsonObject.getJSONObject(name));
                } else if(Long.class.equals(type)) {
                    return type.cast(jsonObject.getLong(name));
                } else {
                    return null;
                }
            }
        } catch (JSONException e) {
            return null;
        }
    }
}
