package com.dwayne.monitor.ui.user;

import android.content.Context;

import com.dwayne.monitor.server.data.UserLoginData;
import com.dwayne.monitor.server.net.HttpTaskClient;

/**
 * @author YouLiang.Ji
 *
 * 用户设置中心Model
 */
public interface IUserSettingModel {

    /**
     * 注销
     * @param mobile 登录用户手机号
     */
    void logout(String mobile, int reqCode, Context context, HttpTaskClient.OnHttpResponseListener<UserLoginData> listener);
}
