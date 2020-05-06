/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.api.ApiRes
 *  cn.springcloud.gray.api.ApiRes$ApiResBuilder
 *  org.springframework.http.HttpStatus
 */
package cn.springcloud.gray.server.utils;

import cn.springcloud.gray.api.ApiRes;
import org.springframework.http.HttpStatus;

public class ApiResHelper {
    public static <T> ApiRes<T> notAuthority() {
        return ApiRes.builder().code(String.valueOf(HttpStatus.FORBIDDEN.value())).message("has not authority to operation this service").build();
    }

    public static <T> ApiRes<T> notFound() {
        return ApiResHelper.notFound("resource is  not found");
    }

    public static <T> ApiRes<T> notFound(String msg) {
        return ApiRes.builder().code(String.valueOf(HttpStatus.NOT_FOUND.value())).message(msg).build();
    }

    public static <T> ApiRes failed() {
        return ApiResHelper.failed("operation is failed");
    }

    public static <T> ApiRes failed(String msg) {
        return ApiRes.builder().code("500").message(msg).build();
    }

    public static <T> ApiRes success() {
        return ApiResHelper.success("operation is success");
    }

    public static <T> ApiRes success(String msg) {
        return ApiRes.builder().code("0").message(msg).build();
    }

    public static <T> ApiRes successData(T data) {
        return ApiResHelper.successData("operation is success", data);
    }

    public static <T> ApiRes successData(String msg, T data) {
        return ApiRes.builder().code("0").message(msg).data(data).build();
    }
}

