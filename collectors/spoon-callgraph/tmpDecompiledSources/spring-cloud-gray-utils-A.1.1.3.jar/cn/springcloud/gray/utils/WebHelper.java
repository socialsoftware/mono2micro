/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletResponse
 */
package cn.springcloud.gray.utils;

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebHelper {
    private static final Logger LOG = LoggerFactory.getLogger(WebHelper.class);
    public static final String CHARSET_UTF8 = "UTF-8";

    public static void responceJson(HttpServletResponse response, String content) throws IOException {
        response.setCharacterEncoding(CHARSET_UTF8);
        response.setContentType("application/json; charset=UTF-8");
        String jsonString = content;
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(jsonString.getBytes(CHARSET_UTF8));
    }

    public static void writerResponceContent(HttpServletResponse response, String content) {
        try {
            WebHelper.responceJson(response, content);
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}

