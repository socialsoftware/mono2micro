/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.bean.properties.bind;

abstract class BeanPropertyName {
    private BeanPropertyName() {
    }

    public static String toDashedForm(String name) {
        return BeanPropertyName.toDashedForm(name, 0);
    }

    public static String toDashedForm(String name, int start) {
        StringBuilder result = new StringBuilder();
        char[] chars = name.replace("_", "-").toCharArray();
        for (int i = start; i < chars.length; ++i) {
            char ch = chars[i];
            if (Character.isUpperCase(ch) && result.length() > 0 && result.charAt(result.length() - 1) != '-') {
                result.append("-");
            }
            result.append(Character.toLowerCase(ch));
        }
        return result.toString();
    }
}

