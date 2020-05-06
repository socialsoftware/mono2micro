/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  cn.springcloud.gray.utils.WebHelper
 *  javax.servlet.http.HttpServletResponse
 */
package cn.springcloud.gray.server.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class WebHelper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private WebHelper() {
    }

    public static void response(HttpServletResponse response, Object result) throws IOException {
        cn.springcloud.gray.utils.WebHelper.responceJson((HttpServletResponse)response, (String)objectMapper.writeValueAsString(result));
    }
}

