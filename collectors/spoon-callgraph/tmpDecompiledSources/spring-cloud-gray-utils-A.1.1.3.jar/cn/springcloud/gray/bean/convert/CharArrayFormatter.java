/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.format.Formatter
 */
package cn.springcloud.gray.bean.convert;

import java.text.ParseException;
import java.util.Locale;
import org.springframework.format.Formatter;

final class CharArrayFormatter
implements Formatter<char[]> {
    CharArrayFormatter() {
    }

    public String print(char[] object, Locale locale) {
        return new String(object);
    }

    public char[] parse(String text, Locale locale) throws ParseException {
        return text.toCharArray();
    }
}

