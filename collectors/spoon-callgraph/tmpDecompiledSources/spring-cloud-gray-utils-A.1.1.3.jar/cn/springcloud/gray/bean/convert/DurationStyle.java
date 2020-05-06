/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.Assert
 *  org.springframework.util.StringUtils
 */
package cn.springcloud.gray.bean.convert;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public enum DurationStyle {
    SIMPLE("^([\\+\\-]?\\d+)([a-zA-Z]{0,2})$"){

        @Override
        public Duration parse(String value, ChronoUnit unit) {
            try {
                Matcher matcher = this.matcher(value);
                Assert.state((boolean)matcher.matches(), (String)"Does not match simple duration pattern");
                String suffix = matcher.group(2);
                return (StringUtils.hasLength((String)suffix) ? Unit.fromSuffix(suffix) : Unit.fromChronoUnit(unit)).parse(matcher.group(1));
            }
            catch (Exception ex) {
                throw new IllegalArgumentException("'" + value + "' is not a valid simple duration", ex);
            }
        }

        @Override
        public String print(Duration value, ChronoUnit unit) {
            return Unit.fromChronoUnit(unit).print(value);
        }
    }
    ,
    ISO8601("^[\\+\\-]?P.*$"){

        @Override
        public Duration parse(String value, ChronoUnit unit) {
            try {
                return Duration.parse(value);
            }
            catch (Exception ex) {
                throw new IllegalArgumentException("'" + value + "' is not a valid ISO-8601 duration", ex);
            }
        }

        @Override
        public String print(Duration value, ChronoUnit unit) {
            return value.toString();
        }
    };
    
    private final Pattern pattern;

    private DurationStyle(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    protected final boolean matches(String value) {
        return this.pattern.matcher(value).matches();
    }

    protected final Matcher matcher(String value) {
        return this.pattern.matcher(value);
    }

    public Duration parse(String value) {
        return this.parse(value, null);
    }

    public abstract Duration parse(String var1, ChronoUnit var2);

    public String print(Duration value) {
        return this.print(value, null);
    }

    public abstract String print(Duration var1, ChronoUnit var2);

    public static Duration detectAndParse(String value) {
        return DurationStyle.detectAndParse(value, null);
    }

    public static Duration detectAndParse(String value, ChronoUnit unit) {
        return DurationStyle.detect(value).parse(value, unit);
    }

    public static DurationStyle detect(String value) {
        Assert.notNull((Object)value, (String)"Value must not be null");
        for (DurationStyle candidate : DurationStyle.values()) {
            if (!candidate.matches(value)) continue;
            return candidate;
        }
        throw new IllegalArgumentException("'" + value + "' is not a valid duration");
    }

    static enum Unit {
        NANOS(ChronoUnit.NANOS, "ns", Duration::toNanos),
        MICROS(ChronoUnit.MICROS, "us", duration -> duration.toMillis() * 1000L),
        MILLIS(ChronoUnit.MILLIS, "ms", Duration::toMillis),
        SECONDS(ChronoUnit.SECONDS, "s", Duration::getSeconds),
        MINUTES(ChronoUnit.MINUTES, "m", Duration::toMinutes),
        HOURS(ChronoUnit.HOURS, "h", Duration::toHours),
        DAYS(ChronoUnit.DAYS, "d", Duration::toDays);
        
        private final ChronoUnit chronoUnit;
        private final String suffix;
        private Function<Duration, Long> longValue;

        private Unit(ChronoUnit chronoUnit, String suffix, Function<Duration, Long> toUnit) {
            this.chronoUnit = chronoUnit;
            this.suffix = suffix;
            this.longValue = toUnit;
        }

        public Duration parse(String value) {
            return Duration.of(Long.valueOf(value), this.chronoUnit);
        }

        public String print(Duration value) {
            return this.longValue(value) + this.suffix;
        }

        public long longValue(Duration value) {
            return this.longValue.apply(value);
        }

        public static Unit fromChronoUnit(ChronoUnit chronoUnit) {
            if (chronoUnit == null) {
                return MILLIS;
            }
            for (Unit candidate : Unit.values()) {
                if (candidate.chronoUnit != chronoUnit) continue;
                return candidate;
            }
            throw new IllegalArgumentException("Unknown unit " + chronoUnit);
        }

        public static Unit fromSuffix(String suffix) {
            for (Unit candidate : Unit.values()) {
                if (!candidate.suffix.equalsIgnoreCase(suffix)) continue;
                return candidate;
            }
            throw new IllegalArgumentException("Unknown unit '" + suffix + "'");
        }
    }

}

