package com.redbeemedia.enigma.core.entitlement;

import android.util.JsonReader;

import com.redbeemedia.enigma.core.asset.JsonMapper;

import java.io.IOException;

public class EntitlementMapper implements JsonMapper<Entitlement> {

    private final JsonMapper.Util util = new Util();


    public EntitlementMapper() {
    }


    @Override
    public Entitlement create() {
        return new Entitlement();
    }

    @Override
    public void map(final JsonReader jsonReader, final Entitlement entitlement, final int position) throws IOException {
        switch (jsonReader.nextName()) {
            case "entitlementType":
                entitlement.setEntitlementType(jsonReader.nextString());
                break;
            case "productId":
                entitlement.setProductId(jsonReader.nextString());
                break;
            case "playSessionId":
                entitlement.setPlaySessionId(jsonReader.nextString());
                break;
            case "playToken":
                entitlement.setPlayToken(jsonReader.nextString());
                break;
            case "playTokenExpiration":
                entitlement.setPlayTokenExpiration(jsonReader.nextLong());
                break;
            case "live":
                entitlement.setLive(jsonReader.nextBoolean());
                break;
            case  "entitlements":
//                while (jsonReader.hasNext()) {
//                    entitlementMapper.map(jsonReader, entitlement, position);
//                }
//                jsonReader.endObject();
//
//                entitlementMapper.onMapComplete(entitlement, position);

//                jsonReader.beginObject();
//                while (jsonReader.hasNext()) {
//                }
//                jsonReader.endObject();

//                entitlement.setAirplayEnabled(jsonReader.);
                break;
            case "customData":
            default:
                jsonReader.skipValue();
        }
    }
    /*
    "entitlements":{
"airplayEnabled":false,
"timeshiftEnabled":false,
"rwEnabled":false,
"ffEnabled":false
}
     */

    @Override
    public Entitlement onMapComplete(final Entitlement entitlement, final int position) {
        return entitlement;
    }

    // Can be used with search API
    public static class Wrapped implements JsonMapper<Entitlement> {

        private final EntitlementMapper entitlementMapper;

        public Wrapped() {
            this.entitlementMapper = new EntitlementMapper();
        }

        @Override
        public Entitlement create() {
            return entitlementMapper.create();
        }

        @Override
        public void map(final JsonReader jsonReader, final Entitlement entitlement, final int position) throws IOException {
            //TODO:
            if (jsonReader.nextName().equals("entitlement")) {
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    entitlementMapper.map(jsonReader, entitlement, position);
                }
                jsonReader.endObject();

                entitlementMapper.onMapComplete(entitlement, position);
            } else {
                jsonReader.skipValue();
            }
        }

        @Override
        public Entitlement onMapComplete(final Entitlement entitlement, final int position) {
            return entitlement;
        }
    }
}
