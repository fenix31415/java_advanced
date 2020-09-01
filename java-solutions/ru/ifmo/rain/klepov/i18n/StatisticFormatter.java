package ru.ifmo.rain.klepov.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class StatisticFormatter {
    Locale outLocal;
    ResourceBundle bundle;

    public StatisticFormatter (Locale locale) {
        outLocal = locale;
    }

    private String getFileHeader() {
        return " <head>\n" +
                "  <meta charset=\"utf-8\">\n" +
                "  <title>" + readFromBundle("fileHeader") + "</title>\n" +
                " </head>\n";
    }

    private String getHeader(final String inFileName) {
        return "  <h1>" + readFromBundle("header", inFileName) + "</h1>\n";
    }

    private String readFromBundle(final String request, final Object ... v) {
        return new MessageFormat(bundle.getString(request), outLocal).format(v);
    }

    private String getCommon(final Map<TextStatistics.STATMODE, StatisticContainer> allStat) {
        final StringBuilder ans = new StringBuilder("  <p><b>" + readFromBundle("common") + "</b><br>\n");

        for (final Map.Entry<TextStatistics.STATMODE, StatisticContainer> i : allStat.entrySet()) {
            ans.append(readFromBundle(i.getKey().getCommon(), i.getValue().elements)).append("<br>\n");
        }
        ans.append("  </p>\n");
        return ans.toString();
    }

    private String getMin(final Map.Entry<TextStatistics.STATMODE, StatisticContainer> i) {
        if (i.getValue().min == null)
            return bundle.getString(i.getKey().getMinUndef());
        return i.getValue().getMinMax(bundle.getString(i.getKey().getMin()), i.getValue().min);
    }

    private String getMax(final Map.Entry<TextStatistics.STATMODE, StatisticContainer> i) {
        if (i.getValue().max == null)
            return bundle.getString(i.getKey().getMaxUndef());
        return i.getValue().getMinMax(bundle.getString(i.getKey().getMax()), i.getValue().max);
    }

    private String getOtherStat(final ResourceBundle bundle, final Map<TextStatistics.STATMODE, StatisticContainer> allStat) {
        final StringBuilder ans = new StringBuilder();
        for (final Map.Entry<TextStatistics.STATMODE, StatisticContainer> i : allStat.entrySet()) {
            ans.append("<p><b>").append(readFromBundle(i.getKey().toString().toLowerCase())).append("</b><br>\n");
            ans.append(readFromBundle(i.getKey().getNumber(), i.getValue().elements, i.getValue().getDistinct())).append("<br>\n");
            ans.append(getMin(i)).append("<br>\n");
            ans.append(getMax(i)).append("<br>\n");
            ans.append(i.getValue().getAvg(bundle.getString(i.getKey().getAvg()), bundle.getString(i.getKey().getAvgUndef())));
            ans.append("</p>\n");
        }
        return ans.toString();
    }

    public String getAns(final Locale locale, final String inFileName, final Map<TextStatistics.STATMODE, StatisticContainer> allStat) {
        bundle = ResourceBundle.getBundle("ru.ifmo.rain.klepov.i18n.UsageResourceBundle", locale);

        final StringBuilder builder = new StringBuilder();

        builder.append("<html>\n");
        builder.append(getFileHeader());
        builder.append(" <body>\n\n");
        builder.append(getHeader(inFileName));
        builder.append(getCommon(allStat));
        builder.append(getOtherStat(bundle, allStat));
        builder.append(" </body>\n" + "</html>");

        return builder.toString();
    }
}
