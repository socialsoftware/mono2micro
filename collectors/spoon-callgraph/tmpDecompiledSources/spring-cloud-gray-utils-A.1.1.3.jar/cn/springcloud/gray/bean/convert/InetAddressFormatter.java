/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.format.Formatter
 */
package cn.springcloud.gray.bean.convert;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Locale;
import org.springframework.format.Formatter;

final class InetAddressFormatter
implements Formatter<InetAddress> {
    InetAddressFormatter() {
    }

    public String print(InetAddress object, Locale locale) {
        return object.getHostAddress();
    }

    public InetAddress parse(String text, Locale locale) throws ParseException {
        try {
            return InetAddress.getByName(text);
        }
        catch (UnknownHostException ex) {
            throw new IllegalStateException("Unknown host " + text, ex);
        }
    }
}

