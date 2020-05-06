/*
 * Decompiled with CFR 0.146.
 * 
 * Could not load the following classes:
 *  org.springframework.util.Assert
 */
package cn.springcloud.gray.bean.properties.source;

import cn.springcloud.gray.bean.properties.source.InvalidConfigurationPropertyNameException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.springframework.util.Assert;

public final class ConfigurationPropertyName
implements Comparable<ConfigurationPropertyName> {
    private static final String EMPTY_STRING = "";
    public static final ConfigurationPropertyName EMPTY = new ConfigurationPropertyName(Elements.EMPTY);
    private Elements elements;
    private final CharSequence[] uniformElements;
    private String string;

    private ConfigurationPropertyName(Elements elements) {
        this.elements = elements;
        this.uniformElements = new CharSequence[elements.getSize()];
    }

    public boolean isEmpty() {
        return this.elements.getSize() == 0;
    }

    public boolean isLastElementIndexed() {
        int size = this.getNumberOfElements();
        return size > 0 && this.isIndexed(size - 1);
    }

    boolean isIndexed(int elementIndex) {
        return this.elements.getType(elementIndex).isIndexed();
    }

    public boolean isNumericIndex(int elementIndex) {
        return this.elements.getType(elementIndex) == ElementType.NUMERICALLY_INDEXED;
    }

    public String getLastElement(Form form) {
        int size = this.getNumberOfElements();
        return size != 0 ? this.getElement(size - 1, form) : EMPTY_STRING;
    }

    public String getElement(int elementIndex, Form form) {
        CharSequence element = this.elements.get(elementIndex);
        ElementType type = this.elements.getType(elementIndex);
        if (type.isIndexed()) {
            return element.toString();
        }
        if (form == Form.ORIGINAL) {
            if (type != ElementType.NON_UNIFORM) {
                return element.toString();
            }
            return this.convertToOriginalForm(element).toString();
        }
        if (form == Form.DASHED) {
            if (type == ElementType.UNIFORM || type == ElementType.DASHED) {
                return element.toString();
            }
            return this.convertToDashedElement(element).toString();
        }
        CharSequence uniformElement = this.uniformElements[elementIndex];
        if (uniformElement == null) {
            uniformElement = type != ElementType.UNIFORM ? this.convertToUniformElement(element) : element;
            this.uniformElements[elementIndex] = uniformElement.toString();
        }
        return uniformElement.toString();
    }

    private CharSequence convertToOriginalForm(CharSequence element) {
        return this.convertElement(element, false, (ch, i) -> ch == '_' || ElementsParser.isValidChar(Character.toLowerCase(ch), i));
    }

    private CharSequence convertToDashedElement(CharSequence element) {
        return this.convertElement(element, true, (arg_0, arg_1) -> ElementsParser.isValidChar(arg_0, arg_1));
    }

    private CharSequence convertToUniformElement(CharSequence element) {
        return this.convertElement(element, true, (ch, i) -> ElementsParser.isAlphaNumeric(ch));
    }

    private CharSequence convertElement(CharSequence element, boolean lowercase, ElementCharPredicate filter) {
        StringBuilder result = new StringBuilder(element.length());
        for (int i = 0; i < element.length(); ++i) {
            char ch;
            char c = ch = lowercase ? Character.toLowerCase(element.charAt(i)) : element.charAt(i);
            if (!filter.test(ch, i)) continue;
            result.append(ch);
        }
        return result;
    }

    public int getNumberOfElements() {
        return this.elements.getSize();
    }

    public ConfigurationPropertyName append(String elementValue) {
        if (elementValue == null) {
            return this;
        }
        Elements additionalElements = ConfigurationPropertyName.of((CharSequence)elementValue).elements;
        return new ConfigurationPropertyName(this.elements.append(additionalElements));
    }

    public ConfigurationPropertyName chop(int size) {
        if (size >= this.getNumberOfElements()) {
            return this;
        }
        return new ConfigurationPropertyName(this.elements.chop(size));
    }

    public boolean isParentOf(ConfigurationPropertyName name) {
        Assert.notNull((Object)name, (String)"Name must not be null");
        if (this.getNumberOfElements() != name.getNumberOfElements() - 1) {
            return false;
        }
        return this.isAncestorOf(name);
    }

    public boolean isAncestorOf(ConfigurationPropertyName name) {
        Assert.notNull((Object)name, (String)"Name must not be null");
        if (this.getNumberOfElements() >= name.getNumberOfElements()) {
            return false;
        }
        for (int i = 0; i < this.elements.getSize(); ++i) {
            if (this.elementEquals(this.elements, name.elements, i)) continue;
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ConfigurationPropertyName other) {
        return this.compare(this, other);
    }

    private int compare(ConfigurationPropertyName n1, ConfigurationPropertyName n2) {
        int l1 = n1.getNumberOfElements();
        int l2 = n2.getNumberOfElements();
        int i1 = 0;
        int i2 = 0;
        while (i1 < l1 || i2 < l2) {
            try {
                ElementType type1;
                String e2;
                ElementType type2;
                String e1 = i1 < l1 ? n1.getElement(i1++, Form.UNIFORM) : null;
                int result = this.compare(e1, type1 = i1 < l1 ? n1.elements.getType(i1) : null, e2 = i2 < l2 ? n2.getElement(i2++, Form.UNIFORM) : null, type2 = i2 < l2 ? n2.elements.getType(i2) : null);
                if (result == 0) continue;
                return result;
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                throw new RuntimeException(ex);
            }
        }
        return 0;
    }

    private int compare(String e1, ElementType type1, String e2, ElementType type2) {
        if (e1 == null) {
            return -1;
        }
        if (e2 == null) {
            return 1;
        }
        int result = Boolean.compare(type2.isIndexed(), type1.isIndexed());
        if (result != 0) {
            return result;
        }
        if (type1 == ElementType.NUMERICALLY_INDEXED && type2 == ElementType.NUMERICALLY_INDEXED) {
            long v1 = Long.parseLong(e1);
            long v2 = Long.parseLong(e2);
            return Long.compare(v1, v2);
        }
        return e1.compareTo(e2);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ConfigurationPropertyName other = (ConfigurationPropertyName)obj;
        if (this.getNumberOfElements() != other.getNumberOfElements()) {
            return false;
        }
        if (this.elements.canShortcutWithSource(ElementType.UNIFORM) && other.elements.canShortcutWithSource(ElementType.UNIFORM)) {
            return this.toString().equals(other.toString());
        }
        for (int i = 0; i < this.elements.getSize(); ++i) {
            if (this.elementEquals(this.elements, other.elements, i)) continue;
            return false;
        }
        return true;
    }

    private boolean elementEquals(Elements e1, Elements e2, int i) {
        int l1 = e1.getLength(i);
        int l2 = e2.getLength(i);
        boolean indexed1 = e1.getType(i).isIndexed();
        boolean indexed2 = e2.getType(i).isIndexed();
        int i1 = 0;
        int i2 = 0;
        while (i1 < l1) {
            char ch2;
            if (i2 >= l2) {
                return false;
            }
            char ch1 = indexed1 ? e1.charAt(i, i1) : Character.toLowerCase(e1.charAt(i, i1));
            char c = ch2 = indexed2 ? e2.charAt(i, i2) : Character.toLowerCase(e2.charAt(i, i2));
            if (!indexed1 && !ElementsParser.isAlphaNumeric(ch1)) {
                ++i1;
                continue;
            }
            if (!indexed2 && !ElementsParser.isAlphaNumeric(ch2)) {
                ++i2;
                continue;
            }
            if (ch1 != ch2) {
                return false;
            }
            ++i1;
            ++i2;
        }
        while (i2 < l2) {
            char ch2 = Character.toLowerCase(e2.charAt(i, i2++));
            if (!indexed2 && !ElementsParser.isAlphaNumeric(ch2)) continue;
            return false;
        }
        return true;
    }

    public int hashCode() {
        return 0;
    }

    public String toString() {
        if (this.string == null) {
            this.string = this.buildToString();
        }
        return this.string;
    }

    private String buildToString() {
        if (this.elements.canShortcutWithSource(ElementType.UNIFORM, ElementType.DASHED)) {
            return this.elements.getSource().toString();
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.getNumberOfElements(); ++i) {
            boolean indexed = this.isIndexed(i);
            if (result.length() > 0 && !indexed) {
                result.append('.');
            }
            if (indexed) {
                result.append("[");
                result.append(this.getElement(i, Form.ORIGINAL));
                result.append("]");
                continue;
            }
            result.append(this.getElement(i, Form.DASHED));
        }
        return result.toString();
    }

    public static boolean isValid(CharSequence name) {
        return ConfigurationPropertyName.of(name, true) != null;
    }

    public static ConfigurationPropertyName of(CharSequence name) {
        return ConfigurationPropertyName.of(name, false);
    }

    static ConfigurationPropertyName of(CharSequence name, boolean returnNullIfInvalid) {
        if (name == null) {
            Assert.isTrue((boolean)returnNullIfInvalid, (String)"Name must not be null");
            return null;
        }
        if (name.length() == 0) {
            return EMPTY;
        }
        if (name.charAt(0) == '.' || name.charAt(name.length() - 1) == '.') {
            if (returnNullIfInvalid) {
                return null;
            }
            throw new InvalidConfigurationPropertyNameException(name, Collections.singletonList(Character.valueOf('.')));
        }
        Elements elements = new ElementsParser(name, '.').parse();
        for (int i = 0; i < elements.getSize(); ++i) {
            if (elements.getType(i) != ElementType.NON_UNIFORM) continue;
            if (returnNullIfInvalid) {
                return null;
            }
            throw new InvalidConfigurationPropertyNameException(name, ConfigurationPropertyName.getInvalidChars(elements, i));
        }
        return new ConfigurationPropertyName(elements);
    }

    private static List<Character> getInvalidChars(Elements elements, int index) {
        ArrayList<Character> invalidChars = new ArrayList<Character>();
        for (int charIndex = 0; charIndex < elements.getLength(index); ++charIndex) {
            char ch = elements.charAt(index, charIndex);
            if (ElementsParser.isValidChar(ch, charIndex)) continue;
            invalidChars.add(Character.valueOf(ch));
        }
        return invalidChars;
    }

    static ConfigurationPropertyName adapt(CharSequence name, char separator) {
        return ConfigurationPropertyName.adapt(name, separator, null);
    }

    static ConfigurationPropertyName adapt(CharSequence name, char separator, Function<CharSequence, CharSequence> elementValueProcessor) {
        Assert.notNull((Object)name, (String)"Name must not be null");
        if (name.length() == 0) {
            return EMPTY;
        }
        Elements elements = new ElementsParser(name, separator).parse(elementValueProcessor);
        if (elements.getSize() == 0) {
            return EMPTY;
        }
        return new ConfigurationPropertyName(elements);
    }

    private static interface ElementCharPredicate {
        public boolean test(char var1, int var2);
    }

    private static enum ElementType {
        EMPTY(false),
        UNIFORM(false),
        DASHED(false),
        NON_UNIFORM(false),
        INDEXED(true),
        NUMERICALLY_INDEXED(true);
        
        private final boolean indexed;

        private ElementType(boolean indexed) {
            this.indexed = indexed;
        }

        public boolean isIndexed() {
            return this.indexed;
        }
    }

    private static class ElementsParser {
        private static final int DEFAULT_CAPACITY = 6;
        private final CharSequence source;
        private final char separator;
        private int size;
        private int[] start;
        private int[] end;
        private ElementType[] type;
        private CharSequence[] resolved;

        ElementsParser(CharSequence source, char separator) {
            this(source, separator, 6);
        }

        ElementsParser(CharSequence source, char separator, int capacity) {
            this.source = source;
            this.separator = separator;
            this.start = new int[capacity];
            this.end = new int[capacity];
            this.type = new ElementType[capacity];
        }

        public Elements parse() {
            return this.parse(null);
        }

        public Elements parse(Function<CharSequence, CharSequence> valueProcessor) {
            int length = this.source.length();
            int openBracketCount = 0;
            int start = 0;
            ElementType type = ElementType.EMPTY;
            for (int i = 0; i < length; ++i) {
                char ch = this.source.charAt(i);
                if (ch == '[') {
                    if (openBracketCount == 0) {
                        this.add(start, i, type, valueProcessor);
                        start = i + 1;
                        type = ElementType.NUMERICALLY_INDEXED;
                    }
                    ++openBracketCount;
                    continue;
                }
                if (ch == ']') {
                    if (--openBracketCount != 0) continue;
                    this.add(start, i, type, valueProcessor);
                    start = i + 1;
                    type = ElementType.EMPTY;
                    continue;
                }
                if (!type.isIndexed() && ch == this.separator) {
                    this.add(start, i, type, valueProcessor);
                    start = i + 1;
                    type = ElementType.EMPTY;
                    continue;
                }
                type = this.updateType(type, ch, i - start);
            }
            if (openBracketCount != 0) {
                type = ElementType.NON_UNIFORM;
            }
            this.add(start, length, type, valueProcessor);
            return new Elements(this.source, this.size, this.start, this.end, this.type, this.resolved);
        }

        private ElementType updateType(ElementType existingType, char ch, int index) {
            if (existingType.isIndexed()) {
                if (existingType == ElementType.NUMERICALLY_INDEXED && !ElementsParser.isNumeric(ch)) {
                    return ElementType.INDEXED;
                }
                return existingType;
            }
            if (existingType == ElementType.EMPTY && ElementsParser.isValidChar(ch, index)) {
                return index == 0 ? ElementType.UNIFORM : ElementType.NON_UNIFORM;
            }
            if (existingType == ElementType.UNIFORM && ch == '-') {
                return ElementType.DASHED;
            }
            if (!ElementsParser.isValidChar(ch, index)) {
                if (existingType == ElementType.EMPTY && !ElementsParser.isValidChar(Character.toLowerCase(ch), index)) {
                    return ElementType.EMPTY;
                }
                return ElementType.NON_UNIFORM;
            }
            return existingType;
        }

        private void add(int start, int end, ElementType type, Function<CharSequence, CharSequence> valueProcessor) {
            if (end - start < 1 || type == ElementType.EMPTY) {
                return;
            }
            if (this.start.length <= end) {
                this.start = this.expand(this.start);
                this.end = this.expand(this.end);
                this.type = this.expand(this.type);
                this.resolved = this.expand(this.resolved);
            }
            if (valueProcessor != null) {
                CharSequence resolved;
                Elements resolvedElements;
                if (this.resolved == null) {
                    this.resolved = new CharSequence[this.start.length];
                }
                Assert.state((boolean)((resolvedElements = new ElementsParser(resolved = valueProcessor.apply(this.source.subSequence(start, end)), '.').parse()).getSize() == 1), (String)"Resolved element must not contain multiple elements");
                this.resolved[this.size] = resolvedElements.get(0);
                type = resolvedElements.getType(0);
            }
            this.start[this.size] = start;
            this.end[this.size] = end;
            this.type[this.size] = type;
            ++this.size;
        }

        private int[] expand(int[] src) {
            int[] dest = new int[src.length + 6];
            System.arraycopy(src, 0, dest, 0, src.length);
            return dest;
        }

        private ElementType[] expand(ElementType[] src) {
            ElementType[] dest = new ElementType[src.length + 6];
            System.arraycopy(src, 0, dest, 0, src.length);
            return dest;
        }

        private CharSequence[] expand(CharSequence[] src) {
            if (src == null) {
                return null;
            }
            CharSequence[] dest = new CharSequence[src.length + 6];
            System.arraycopy(src, 0, dest, 0, src.length);
            return dest;
        }

        public static boolean isValidChar(char ch, int index) {
            return ElementsParser.isAlpha(ch) || ElementsParser.isNumeric(ch) || index != 0 && ch == '-';
        }

        public static boolean isAlphaNumeric(char ch) {
            return ElementsParser.isAlpha(ch) || ElementsParser.isNumeric(ch);
        }

        private static boolean isAlpha(char ch) {
            return ch >= 'a' && ch <= 'z';
        }

        private static boolean isNumeric(char ch) {
            return ch >= '0' && ch <= '9';
        }
    }

    private static class Elements {
        private static final int[] NO_POSITION = new int[0];
        private static final ElementType[] NO_TYPE = new ElementType[0];
        public static final Elements EMPTY = new Elements("", 0, NO_POSITION, NO_POSITION, NO_TYPE, null);
        private final CharSequence source;
        private final int size;
        private final int[] start;
        private final int[] end;
        private final ElementType[] type;
        private final CharSequence[] resolved;

        Elements(CharSequence source, int size, int[] start, int[] end, ElementType[] type, CharSequence[] resolved) {
            this.source = source;
            this.size = size;
            this.start = start;
            this.end = end;
            this.type = type;
            this.resolved = resolved;
        }

        public Elements append(Elements additional) {
            Assert.isTrue((boolean)(additional.getSize() == 1), (String)("Element value '" + additional.getSource() + "' must be a single item"));
            ElementType[] type = new ElementType[this.size + 1];
            System.arraycopy(this.type, 0, type, 0, this.size);
            type[this.size] = additional.type[0];
            CharSequence[] resolved = this.newResolved(this.size + 1);
            resolved[this.size] = additional.get(0);
            return new Elements(this.source, this.size + 1, this.start, this.end, type, resolved);
        }

        public Elements chop(int size) {
            CharSequence[] resolved = this.newResolved(size);
            return new Elements(this.source, size, this.start, this.end, this.type, resolved);
        }

        private CharSequence[] newResolved(int size) {
            CharSequence[] resolved = new CharSequence[size];
            if (this.resolved != null) {
                System.arraycopy(this.resolved, 0, resolved, 0, Math.min(size, this.size));
            }
            return resolved;
        }

        public int getSize() {
            return this.size;
        }

        public CharSequence get(int index) {
            if (this.resolved != null && this.resolved[index] != null) {
                return this.resolved[index];
            }
            int start = this.start[index];
            int end = this.end[index];
            return this.source.subSequence(start, end);
        }

        public int getLength(int index) {
            if (this.resolved != null && this.resolved[index] != null) {
                return this.resolved[index].length();
            }
            int start = this.start[index];
            int end = this.end[index];
            return end - start;
        }

        public char charAt(int index, int charIndex) {
            if (this.resolved != null && this.resolved[index] != null) {
                return this.resolved[index].charAt(charIndex);
            }
            int start = this.start[index];
            return this.source.charAt(start + charIndex);
        }

        public ElementType getType(int index) {
            return this.type[index];
        }

        public CharSequence getSource() {
            return this.source;
        }

        public boolean canShortcutWithSource(ElementType requiredType) {
            return this.canShortcutWithSource(requiredType, requiredType);
        }

        public boolean canShortcutWithSource(ElementType requiredType, ElementType alternativeType) {
            if (this.resolved != null) {
                return false;
            }
            for (int i = 0; i < this.size; ++i) {
                ElementType type = this.type[i];
                if (type != requiredType && type != alternativeType) {
                    return false;
                }
                if (i <= 0 || this.end[i - 1] + 1 == this.start[i]) continue;
                return false;
            }
            return true;
        }
    }

    public static enum Form {
        ORIGINAL,
        DASHED,
        UNIFORM;
        
    }

}

