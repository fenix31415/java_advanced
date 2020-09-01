package ru.ifmo.rain.klepov.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;


public class WebCrawler implements Crawler {
	private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final ConcurrentMap<String, HostDataStorage> hosts;
    private final int perHost;

    public WebCrawler(final int downloaders, final int extractors, final int perHost) throws IOException {
        this(new CachingDownloader(), downloaders, extractors, perHost);
    }
	
	public WebCrawler(final Downloader downloader, final int downloadersCount, final int extractorsCount, final int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;
        downloaders = Executors.newFixedThreadPool(downloadersCount);
        extractors = Executors.newFixedThreadPool(extractorsCount);
        hosts = new ConcurrentHashMap<>();
    }
	
	@Override
    public void close() {
        downloaders.shutdownNow();
        extractors.shutdownNow();
        // :NOTE: Нет ожидания
    }

    @Override
    public Result download(final String url, final int depth) {
        final Set<String> oks = ConcurrentHashMap.newKeySet();
        final ConcurrentMap<String, IOException> errs = new ConcurrentHashMap<>();
        final Set<String> used = ConcurrentHashMap.newKeySet();
        used.add(url);
        final Phaser phaser = new Phaser(1);

        goDownload(url, depth, used, oks, errs, phaser);

        phaser.arriveAndAwaitAdvance();
        return new Result(new ArrayList<>(oks), errs);
    }
	
	private void goDownload(final String url, final int depth, final Set<String> used, final Set<String> oks,
							final ConcurrentMap<String, IOException> errs, final Phaser phaser) {
		final String host;
        try {
            host = URLUtils.getHost(url);
        } catch (final MalformedURLException e) {
            errs.put(url, e);
            return;
        }

        phaser.register();
        // :NOTE: Два действия
		hosts.putIfAbsent(host, new HostDataStorage());
		final HostDataStorage data = hosts.get(host);
		data.add(() -> {
			try {
				final Document document = downloader.download(url);
				oks.add(url);
				if (depth > 1) {
					phaser.register();
					extractors.submit(() -> {
						try {
                            document.extractLinks().stream()
                                    .filter(used::add)
                                    .forEach(l -> goDownload(l, depth - 1, used, oks, errs, phaser));
						} catch (final IOException e) {
							errs.put(url, e);
						} finally {
							phaser.arrive();
						}
					});
				}
			} catch (final IOException e) {
				errs.put(url, e);
			} finally {
				phaser.arrive();
                // :NOTE: -> HDS
				data.goNext();
			}
		});
    }
	
	public static void main(final String[] args) {
        if (args == null || args.length >= 6 || args.length == 0 || args[0] == null) {
            System.out.println("Usage: WebCrawler url [depth [downloads [extractors [perHost]]]]");
			return;
        }
		final List<Integer> params;
		try {
			params = getParams(args);
		} catch (final NumberFormatException e) {
            System.out.println("Integer argument expected" + e.getMessage());
			return;
        }
		try (final WebCrawler crawler = new WebCrawler(params.get(2), params.get(3), params.get(4))) {
			crawler.download(args[0], params.get(1));
		} catch (final IOException e) {
            System.out.println("An error ocurred while creating an instance of downloader " + e.getMessage());
        }
    }
	
	private static ArrayList<Integer> getParams(final String[] args) {
		final ArrayList<Integer> ans = new ArrayList<>();
		for (int i = 1; i <= 4; i++) {
			ans.add(i, args[i] != null ? Integer.parseInt(args[i]) : 1);
		}
		return ans;
	}
	
	private class HostDataStorage {
        final Queue<Runnable> newTasks;
        int working;

        HostDataStorage() {
            newTasks = new ArrayDeque<>();
            working = 0;
        }

        private synchronized void add(final Runnable task) {
            if (working >= perHost) {
                newTasks.add(task);
            } else {
                ++working;
                downloaders.submit(task);
            }
        }

        private synchronized void goNext() {
            final Runnable curr = newTasks.poll();
            if (curr != null) {
                downloaders.submit(curr);
            } else {
                --working;
            }
        }
    }

}
