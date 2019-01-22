package rest.create;

import core.TokenManager;
import json.Registration;
import org.springframework.ui.Model;
import utils.Logging;
import utils.Utils;

public class DeviceSubscriber {

    public static String addToken(String registration, Model model) throws Exception {

        String token = Utils.gson.fromJson(registration, Registration.class).getToken();

        TokenManager.getInstance().addToken(token);

        Logging.log(String.format("Token %s registered!", token));

        return "Token successfully Registered!";
    }

}
