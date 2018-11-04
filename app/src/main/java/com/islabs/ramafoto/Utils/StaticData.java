package com.islabs.ramafoto.Utils;

/**
 * Created by shanky on 30/5/17.
 */

public class StaticData {

    public static final String HOST = "http://ramaphotoexpress.com";


    public static final String GET_ALBUM = HOST.concat("/site/api");
    public static final String SIGN_UP = HOST.concat("/site/apisignup");
    public static final String GET_VIEW_COUNT = HOST.concat("/site/getviewcount");
    public static final String CONTACT_US = HOST.concat("/site/apicontact");
    public static final String PORTFOLIO = HOST.concat("/site/photographerdetailapi");


    public static final String ALBUM_PIN = "album_pin";
    public static final String PREF = "user";
    public static final String BROADCAST_ACTION = "com.islabs.photobook.BROADCAST";

    public static boolean checkString(String str) {
        return str != null && str.length() > 0 && !str.equals("null");
    }
}
