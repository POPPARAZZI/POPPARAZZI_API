package com.spoons.popparazzi.common;

public enum YesNo {

    YES("Y"),
    NO("N");

    private final String code;

    YesNo(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static YesNo from(String code) {
        if (code == null) return null;

        for (YesNo yn : values()) {
            if (yn.code.equalsIgnoreCase(code)) {
                return yn;
            }
        }

        throw new IllegalArgumentException("Invalid YesNo value: " + code);
    }

    public boolean isYes() { return this == YES; }

    public boolean isNo() { return this == NO; }
}
