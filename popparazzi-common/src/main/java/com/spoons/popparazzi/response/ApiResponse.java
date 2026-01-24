package com.spoons.popparazzi.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private String status;
    private String errorCode;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "0", data);
    }

    public static ApiResponse<Void> error(String errorCode) {
        return new ApiResponse<>("ERROR", errorCode, null);
    }
}
