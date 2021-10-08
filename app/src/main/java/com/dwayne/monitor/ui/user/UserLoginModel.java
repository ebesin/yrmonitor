package com.dwayne.monitor.ui.user;

import android.content.Context;

import com.dwayne.monitor.server.data.UserLoginData;
import com.dwayne.monitor.server.net.HttpTaskClient;
import com.dwayne.monitor.util.security.ValidateUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YouLiang.Ji
 */
public class UserLoginModel implements IUserLoginModel {

    @Override
    public void sendSms(String phone, int reqCode, Context context, HttpTaskClient.OnHttpResponseListener<UserLoginData> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("mobile", phone);
        HttpTaskClient.getInstance().sendSms(params, reqCode, UserLoginData.class, context, listener);
    }

    @Override
    public boolean verifyPhone(String phone) {
        return ValidateUtil.isPhone(phone);
    }
}
