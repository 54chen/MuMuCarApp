package com.chen.mumucarapp.control;

public class Commands {
    public static final String AUTHORIZE_ACCEPT = "*A";

    public static final String AUTHORIZE_NONE = "*?password";

    public static final String AUTHORIZE_PREFIX = "*";

    public static final String AUTHORIZE_REFUSE = "*R";

    public static final String AUTHORIZE_ZERO = "*Z";

    public static final String BACKWARD = "$5";

    public static final String BACKWARD_LEFT = "$7";

    public static final String BACKWARD_RIGHT = "$6";

    public static final String FORWARD = "$0";

    public static final String FORWARD_LEFT = "$3";

    public static final String FORWARD_RIGHT = "$4";

    public static final String KEEP_ALIVE = "$?";

    public static final String LEFT = "$8";

    public static final String POWER = "$!";

    public static final String RIGHT = "$9";

    public static String getCommand(boolean[] paramArrayOfboolean) {
        String str = null;
        return paramArrayOfboolean[0] ? (paramArrayOfboolean[2] ? "$3" : (paramArrayOfboolean[3] ? "$4" : "$0")) : (paramArrayOfboolean[1] ? (paramArrayOfboolean[2] ? "$7" : (paramArrayOfboolean[3] ? "$6" : "$5")) : (paramArrayOfboolean[2] ? "$8" : (paramArrayOfboolean[3] ? "$9" : str)));
    }
}
