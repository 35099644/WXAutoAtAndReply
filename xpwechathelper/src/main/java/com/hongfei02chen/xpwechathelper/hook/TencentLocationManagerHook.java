package com.hongfei02chen.xpwechathelper.hook;


import com.hongfei02chen.xpwechathelper.utils.Constant;
import com.hongfei02chen.xpwechathelper.utils.PropertiesUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by su on 2017/8/30.
 * 腾讯定位 hook
 */

public class TencentLocationManagerHook {

    private boolean fakeLocation;
    private String latitude;
    private String longitude;

    private String methodName;

    public TencentLocationManagerHook(String versionName) {

        //6.6.1 6.6.0 通过
        if (versionName.startsWith("6.6")) {        // 6.6.x
            methodName = "a";
        } else if (versionName.startsWith("6.5")) { // 6.5.x
            methodName = "b";
        }
    }

    public void hook(ClassLoader classLoader) {
        try {
            Class managerClazz = XposedHelpers.findClass("com.tencent.map.geolocation.TencentLocationManager", classLoader);
            XposedBridge.hookAllMethods(managerClazz, "requestLocationUpdates", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Object tencentLocationListener = param.args[1];
                    XposedBridge.hookAllMethods(tencentLocationListener.getClass(), methodName, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            reload();
                            if (fakeLocation) {
                                param.args[1] = Double.valueOf(latitude);
                                param.args[2] = Double.valueOf(longitude);
                            }
                            super.beforeHookedMethod(param);
                        }
                    });
                    super.beforeHookedMethod(param);
                }
            });
        } catch (Error | Exception e) {
            e.printStackTrace();
        }
    }


    private void reload() {
        fakeLocation = Boolean.valueOf(PropertiesUtils.getValue(Constant.PRO_FILE, "fake_location", "false"));
        latitude = PropertiesUtils.getValue(Constant.PRO_FILE, "latitude", "39.908860");
        longitude = PropertiesUtils.getValue(Constant.PRO_FILE, "longitude", "116.397390");
    }
}
