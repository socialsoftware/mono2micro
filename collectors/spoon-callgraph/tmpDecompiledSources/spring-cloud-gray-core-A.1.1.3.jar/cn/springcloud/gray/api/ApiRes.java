/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.api;

public class ApiRes<T> {
    public static final String CODE_SUCCESS = "0";
    public static final String CODE_NOT_FOUND = "404";
    private String code = "0";
    private String message;
    private T data;

    public static <T> ApiResBuilder<T> builder() {
        return new ApiResBuilder();
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public T getData() {
        return this.data;
    }

    public ApiRes(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public ApiRes() {
    }

    public static class ApiResBuilder<T> {
        private String code;
        private String message;
        private T data;

        ApiResBuilder() {
        }

        public ApiResBuilder<T> code(String code) {
            this.code = code;
            return this;
        }

        public ApiResBuilder<T> message(String message) {
            this.message = message;
            return this;
        }

        public ApiResBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ApiRes<T> build() {
            return new ApiRes<T>(this.code, this.message, this.data);
        }

        public String toString() {
            return "ApiRes.ApiResBuilder(code=" + this.code + ", message=" + this.message + ", data=" + this.data + ")";
        }
    }

}

