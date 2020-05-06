/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.convert.converter.Converter
 *  org.springframework.util.Assert
 */
package cn.springcloud.gray.server.resources.converter;

import java.text.ParseException;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;

public class StringToDateConverter
implements Converter<String, Date> {
    private static Logger log = LoggerFactory.getLogger(StringToDateConverter.class);
    private static final String[] dateFormats = new String[]{"EEE, d MMM yyyy HH:mm:ss z", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ssZ", "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd"};

    public Date convert(String source) {
        source = StringUtils.strip(source);
        Assert.hasText((String)source, (String)"Null or emtpy date string");
        Date date = null;
        try {
            date = DateUtils.parseDate(source, dateFormats);
        }
        catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        if (date == null) {
            String errMsg = String.format("Failed to convert [%s] to [%s] for value '%s'", String.class.toString(), Date.class.toString(), source);
            log.error(errMsg);
            throw new IllegalArgumentException(errMsg);
        }
        return date;
    }
}

