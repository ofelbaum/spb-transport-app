package com.emal.android.transport.spb.model;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 2.0
 */
public enum Theme {
    HOLO_BLACK(android.R.style.Theme_Holo),
    HOLO_LIGHT(android.R.style.Theme_Holo_Light);
    private int code;

    Theme(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
