/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.core.convert.ConversionService
 *  org.springframework.core.convert.converter.ConverterFactory
 *  org.springframework.core.convert.converter.ConverterRegistry
 *  org.springframework.core.convert.converter.GenericConverter
 *  org.springframework.core.convert.support.DefaultConversionService
 *  org.springframework.format.Formatter
 *  org.springframework.format.FormatterRegistry
 *  org.springframework.format.support.DefaultFormattingConversionService
 *  org.springframework.format.support.FormattingConversionService
 *  org.springframework.util.StringValueResolver
 */
package cn.springcloud.gray.bean.convert;

import cn.springcloud.gray.bean.convert.ArrayToDelimitedStringConverter;
import cn.springcloud.gray.bean.convert.CharArrayFormatter;
import cn.springcloud.gray.bean.convert.CollectionToDelimitedStringConverter;
import cn.springcloud.gray.bean.convert.DelimitedStringToArrayConverter;
import cn.springcloud.gray.bean.convert.DelimitedStringToCollectionConverter;
import cn.springcloud.gray.bean.convert.DurationToNumberConverter;
import cn.springcloud.gray.bean.convert.DurationToStringConverter;
import cn.springcloud.gray.bean.convert.InetAddressFormatter;
import cn.springcloud.gray.bean.convert.IsoOffsetFormatter;
import cn.springcloud.gray.bean.convert.NumberToDataSizeConverter;
import cn.springcloud.gray.bean.convert.NumberToDurationConverter;
import cn.springcloud.gray.bean.convert.StringToDataSizeConverter;
import cn.springcloud.gray.bean.convert.StringToDurationConverter;
import cn.springcloud.gray.bean.convert.StringToEnumIgnoringCaseConverterFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.util.StringValueResolver;

public class ApplicationConversionService
extends FormattingConversionService {
    private static volatile ApplicationConversionService sharedInstance;

    public ApplicationConversionService() {
        this(null);
    }

    public ApplicationConversionService(StringValueResolver embeddedValueResolver) {
        if (embeddedValueResolver != null) {
            this.setEmbeddedValueResolver(embeddedValueResolver);
        }
        ApplicationConversionService.configure((FormatterRegistry)this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static ConversionService getSharedInstance() {
        ApplicationConversionService sharedInstance = ApplicationConversionService.sharedInstance;
        if (sharedInstance != null) return sharedInstance;
        Class<ApplicationConversionService> class_ = ApplicationConversionService.class;
        synchronized (ApplicationConversionService.class) {
            sharedInstance = ApplicationConversionService.sharedInstance;
            if (sharedInstance != null) return sharedInstance;
            {
                ApplicationConversionService.sharedInstance = sharedInstance = new ApplicationConversionService();
            }
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return sharedInstance;
        }
    }

    public static void configure(FormatterRegistry registry) {
        DefaultConversionService.addDefaultConverters((ConverterRegistry)registry);
        DefaultFormattingConversionService.addDefaultFormatters((FormatterRegistry)registry);
        ApplicationConversionService.addApplicationFormatters(registry);
        ApplicationConversionService.addApplicationConverters((ConverterRegistry)registry);
    }

    public static void addApplicationConverters(ConverterRegistry registry) {
        ApplicationConversionService.addDelimitedStringConverters(registry);
        registry.addConverter((GenericConverter)new StringToDurationConverter());
        registry.addConverter((GenericConverter)new DurationToStringConverter());
        registry.addConverter((GenericConverter)new NumberToDurationConverter());
        registry.addConverter((GenericConverter)new DurationToNumberConverter());
        registry.addConverter((GenericConverter)new StringToDataSizeConverter());
        registry.addConverter((GenericConverter)new NumberToDataSizeConverter());
        registry.addConverterFactory((ConverterFactory)new StringToEnumIgnoringCaseConverterFactory());
    }

    public static void addDelimitedStringConverters(ConverterRegistry registry) {
        ConversionService service = (ConversionService)registry;
        registry.addConverter((GenericConverter)new ArrayToDelimitedStringConverter(service));
        registry.addConverter((GenericConverter)new CollectionToDelimitedStringConverter(service));
        registry.addConverter((GenericConverter)new DelimitedStringToArrayConverter(service));
        registry.addConverter((GenericConverter)new DelimitedStringToCollectionConverter(service));
    }

    public static void addApplicationFormatters(FormatterRegistry registry) {
        registry.addFormatter((Formatter)new CharArrayFormatter());
        registry.addFormatter((Formatter)new InetAddressFormatter());
        registry.addFormatter((Formatter)new IsoOffsetFormatter());
    }
}

