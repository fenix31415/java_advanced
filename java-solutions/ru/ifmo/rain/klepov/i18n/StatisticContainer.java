package ru.ifmo.rain.klepov.i18n;

import java.text.MessageFormat;
import java.util.*;

public class StatisticContainer {
    protected Comparator<String> cmp;

    public StatisticContainer (Locale out) {
        msg = new MessageFormat("");
        msg.setLocale(out);
        cmp = String::compareTo;
    }

    public void newItem(String s) {
        elements++;
        set.add(s.toLowerCase());
        if (min == null || cmp.compare(s, min) < 0) {
            min = s;
        }
        if (max == null || cmp.compare(max, s) < 0) {
            max = s;
        }
        sumLength = add(sumLength, s);
    }

    public int getDistinct() {
        return set.size();
    }

    protected String add(final String sum, final String s) {
        return Integer.toString(Integer.parseInt(sum) + s.length());
    }

    public String getAvg(final String format, final String format_undef) {
        if (elements == 0) {
            return format_undef;
        }
        Object[] messageArguments = getArgsAvg(sumLength, elements);
        msg.applyPattern(format);
        return msg.format(messageArguments);
    }

    protected Object[] getArgsAvg(final String sumLength, final int elements) {
        return new Object[]{Double.parseDouble(sumLength) / elements};
    }

    public String getMinMax(final String format, final String value) {
        msg.applyPattern(format);
        Object[] messageArguments = getArgsMinMax(value);
        return msg.format(messageArguments);
    }

    protected Object[] getArgsMinMax(final String value) {
        return new Object[]{value.length(), value};
    }

    private final Set<String> set = new HashSet<>();
    protected final MessageFormat msg;
    public int elements = 0;
    public String min = null;
    public String max = null;
    public String sumLength = "0";
}
