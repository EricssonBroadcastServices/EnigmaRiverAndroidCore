package com.redbeemedia.enigma.core.login;

public class UserLoginRequest extends AbstractLoginRequest implements ILoginRequest {


    public UserLoginRequest(String username, String password) {
        super("auth/login");
        
        //TODO maybe have the customer unit and businessUnit in the EnigmaLogin?
        //This class is then closer to the ApiLoginRequest
    }
}
