package ru.ifmo.rain.klepov.i18n;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.*;
import java.util.*;

public class TextStatistics {

    private static final int GAP = 10;

    private Locale locale, outLocal;
    private String text;
    NumberFormat isNumber;
    NumberFormat isCurrency;

    public enum STATMODE {

        SENTENCE {
            @Override
            public String uniform(String item, Locale locale) {
                return item.trim();
            }

            @Override
            public StatisticContainer initStat(Locale outLocal, Locale inLocal) {
                return new LineContainer(outLocal, inLocal);
            }

            @Override
            public int getGap(int gap) {
                return 1;
            }
        },
        LINE {
            @Override
            public String uniform(String item, Locale locale) {
                return item.trim();
            }

            @Override
            public StatisticContainer initStat(Locale outLocal, Locale inLocal) {
                return new LineContainer(outLocal, inLocal);
            }

            @Override
            public int getGap(int gap) {
                return 1;
            }
        },
        WORDS {
            @Override
            public String uniform(String item, Locale locale) {
                String ans = item.trim();
                if (ans.length() == 1)
                    return (Character.isLetterOrDigit(item.charAt(0)) ? ans : null);
                return (ans.equals("") ? null : ans);
            }

            @Override
            public StatisticContainer initStat(Locale outLocal, Locale inLocal) {
                return new LineContainer(outLocal, inLocal);
            }

            @Override
            public int getGap(int gap) {
                return 1;
            }
        },
        NUMBER {
            @Override
            public String uniform(final String item, Locale locale) {
                String anss = item.trim();
                if (anss.equals(""))
                    return null;
                NumberFormat isNumber = NumberFormat.getNumberInstance(locale);
                ParsePosition pos = new ParsePosition(0);
                Number ans = isNumber.parse(anss, pos);
                return (anss.length() != pos.getIndex() ? null : isNumber.format(ans));
            }

            @Override
            public StatisticContainer initStat(Locale outLocal, Locale inLocal) {
                return new NumberContainer(outLocal, NumberFormat.getNumberInstance(inLocal));
            }

            @Override
            public int getGap(int gap) {
                return gap;
            }
        },
        CURRENCY {
            @Override
            public String uniform(final String item, Locale locale) {
                String anss = item.trim();
                if (anss.equals(""))
                    return null;
                ParsePosition pos = new ParsePosition(0);
                NumberFormat isNumber = NumberFormat.getCurrencyInstance(locale);
                Number ans = isNumber.parse(anss, pos);
                return (anss.length() != pos.getIndex() ? null : isNumber.format(ans));
            }

            @Override
            public StatisticContainer initStat(Locale outLocal, Locale inLocal) {
                return new NumberContainer(outLocal, NumberFormat.getCurrencyInstance(inLocal));
            }

            @Override
            public int getGap(int gap) {
                return gap;
            }
        },
        DATE {
            @Override
            public String uniform(String item, Locale locale) {
                final String ans = item.trim();
                if (ans.equals(""))
                    return null;
                final List<DateFormat> dateFormat = List.of(DateFormat.getDateInstance(DateFormat.FULL, locale),
                        DateFormat.getDateInstance(DateFormat.LONG, locale),
                        DateFormat.getDateInstance(DateFormat.MEDIUM, locale),
                        DateFormat.getDateInstance(DateFormat.SHORT, locale));
                String answ = dateFormat.stream().map(i -> {
                    ParsePosition pos = new ParsePosition(0);
                    Date ansDate = i.parse(ans, pos);
                    return (ans.length() != pos.getIndex() ? "" : i.format(ansDate));
                }).reduce((l, r) -> !r.equals("") ? r : l).orElse("");

                return answ.equals("") ? null : answ;
            }

            @Override
            public StatisticContainer initStat(Locale outLocal, Locale inLocal) {
                return new DataContainer(inLocal, outLocal);
            }

            @Override
            public int getGap(int gap) {
                return gap;
            }
        };

        public abstract String uniform(String item, Locale locale);
        public abstract StatisticContainer initStat(Locale outLocal, Locale inLocal);
        public abstract int getGap(int gap);

        public String getCommon() {
            return "common_" + this.toString().toLowerCase();
        }
        public String getMin() {
            return this.toString().toLowerCase() + "_" + "min";
        }
        public String getMax() {
            return this.toString().toLowerCase() + "_" + "max";
        }
        public String getMinUndef() {
            return getMin() + "_undef";
        }
        public String getMaxUndef() {
            return getMax() + "_undef";
        }
        public String getNumber() {
            return this.toString().toLowerCase() + "_number";
        }
        public String getAvg() {
            return this.toString().toLowerCase() + "_avg";
        }
        public String getAvgUndef() {
            return getAvg() + "_undef";
        }
    }

    public TextStatistics(final Locale local, final String txt, final Locale out) {
        text = txt;
        locale = local;
        isNumber = NumberFormat.getNumberInstance(locale);
        isCurrency = NumberFormat.getCurrencyInstance(locale);
        outLocal = out;
    }

    private StatisticContainer getStat(final BreakIterator iterator, final STATMODE mode) {
        final StatisticContainer statistics = mode.initStat(outLocal, locale);

        iterator.setText(text);

        int start = iterator.first();
        int end;

        while (start != BreakIterator.DONE) {
            iterator.next(mode.getGap(GAP));
            end = iterator.current();
            while (end != start) {
                //System.out.printf("start=%d,end=%d, attemps=%d,current=%d\n", start, end,attempts,iterator.current());
                String item = text.substring(start, end);
                //System.out.println("Current item = '" + item + "'");

                //System.out.println("before='"+item+"'");
                item = mode.uniform(item, locale);
                if (item == null) {
                    end = iterator.previous();
                    continue;
                }
                if (mode == STATMODE.NUMBER)
                    System.out.println("NUMBER='"+item+"'");
                if (mode == STATMODE.CURRENCY)
                    System.out.println("CURRENCY='"+item+"'");
                if (mode == STATMODE.DATE)
                    System.out.println("DATE='"+item+"'");
                statistics.newItem(item);
                iterator.previous();
                break;
            }
            start = iterator.next();
        }
        return statistics;
    }

    private static boolean checkArgs(final String[] args) {
        if (args != null) {
            for (final String arg : args) {
                if (arg == null) {
                    System.err.println("Arguments mustn't be null");
                    return false;
                }
            }
        } else {
            System.err.println("Arguments are null");
            return false;
        }
        if (args.length < 4) {
            System.err.println("Usage: <Text Locale> <Report Locale> <Text file> <Report file>");
            return false;
        }
        return true;
    }

    private static Locale findLocale(final String name) {
        return Arrays.stream(Locale.getAvailableLocales()).filter(l -> l.toString().equals(name)).findFirst().orElse(null);
    }

    public StatisticContainer getLineStat() {
        final StatisticContainer stat = new LineContainer(outLocal, locale);
        Arrays.stream(text.split("\\R",-1)).forEach(stat::newItem);
        return stat;
    }

    public StatisticContainer getStatSentences() {
        return getStat(BreakIterator.getSentenceInstance(locale), STATMODE.SENTENCE);
    }

    public StatisticContainer getStatWords() {
        return getStat(BreakIterator.getWordInstance(locale), STATMODE.WORDS);
    }

    public StatisticContainer getStatDates() {
        return getStat(BreakIterator.getLineInstance(locale), STATMODE.DATE);
    }

    public StatisticContainer getStatCurrency() {
        return getStat(BreakIterator.getWordInstance(locale), STATMODE.CURRENCY);
    }

    public StatisticContainer getStatNumbers() {
        return getStat(BreakIterator.getWordInstance(locale), STATMODE.NUMBER);
    }

    public Map<STATMODE, StatisticContainer> getAllStat() {
        final StatisticContainer sentenceStat = getStatSentences();
        final StatisticContainer wordsStat = getStatWords();
        final StatisticContainer linesStat = getLineStat();
        final StatisticContainer dateStat = getStatDates();
        final StatisticContainer currencyStat = getStatCurrency();
        final StatisticContainer numbersStat = getStatNumbers();

        return Map.of(STATMODE.SENTENCE, sentenceStat,
                STATMODE.WORDS, wordsStat,
                STATMODE.LINE, linesStat,
                STATMODE.DATE, dateStat,
                STATMODE.CURRENCY, currencyStat,
                STATMODE.NUMBER, numbersStat);
    }

    public static void main(final String[] args) {
        if(!checkArgs(args))
            return;

        final String inLocaleName = args[0];
        final String outLocaleName = args[1];
        final String inFileName = args[2];
        final String outFileName = args[3];

        final Locale inLocale;
        final Locale outLocale;

        inLocale = findLocale(inLocaleName);
        outLocale = findLocale(outLocaleName);
        if (inLocale == null || outLocale == null) {
            System.out.println("Unsupported locale name. Choose one of:");
            Arrays.stream(Locale.getAvailableLocales()).map(Locale::toString).sorted().forEach(System.out::println);
            return;
        }

        final String text;
        try {
            text = Files.readString(Path.of(inFileName));
        } catch (final IOException e) {
            System.err.println("I/O Exception with input file.");
            return;
        } catch (final InvalidPathException e) {
            System.err.println("Invalid path of input file.");
            return;
        }

        final TextStatistics statistics = new TextStatistics(inLocale, text, outLocale);
        final StatisticFormatter formatter = new StatisticFormatter(outLocale);
        final Map<STATMODE, StatisticContainer> allStat = statistics.getAllStat();
        final String out =  formatter.getAns(outLocale, inFileName, allStat);

        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName));
            writer.write(out);
            writer.close();
        } catch (final IOException ignored) {
            System.out.println("A error while writing in out file");
        }
    }
}
