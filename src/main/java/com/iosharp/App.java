package com.iosharp;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.XmlReader;

import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class App {
    final private static String FEED_URL = "http://espn.go.com/espnradio/feeds/rss/podcast.xml?id=9941853";
    private static String FILEPATH;

    public static void main(String[] args) {
        FILEPATH = args[0];

        SyndFeed feed = getFeed(FEED_URL);
        List<SyndEntry> goodEntries = getGoodEntries(feed);

        SyndFeed newFeed;

        try {
            newFeed = (SyndFeed) feed.clone();
            newFeed.setEntries(goodEntries);

            Writer writer = new FileWriter(FILEPATH);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(newFeed, writer);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SyndFeed getFeed(String feedUrl) {

        SyndFeed feed = null;

        try {
            URL url = new URL(feedUrl);

            SyndFeedInput input = new SyndFeedInput();
            feed = input.build(new XmlReader(url));

            return feed;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return feed;
    }

    private static List<SyndEntry> getGoodEntries(SyndFeed feed) {
        List<SyndEntry> goodEntries = new ArrayList<SyndEntry>();

        for (SyndEntry e : feed.getEntries()) {
            if (e.getTitle().toLowerCase().contains("hour")) {
                SyndEntry entry = e;
                String entryTitle = entry.getTitle().toLowerCase();

                if (entryTitle.contains("local hour")) {
                    entry.setPublishedDate(modifyPublishedDate(entry.getPublishedDate(), -1));
                } else if (entryTitle.contains("hour 1")) {
                    entry.setPublishedDate(modifyPublishedDate(entry.getPublishedDate(), 1));
                } else if (entryTitle.contains("hour 2")) {
                    entry.setPublishedDate(modifyPublishedDate(entry.getPublishedDate(), 2));
                } else if (entryTitle.contains("hour 3")) {
                    entry.setPublishedDate(modifyPublishedDate(entry.getPublishedDate(), 3));
                } else {
                    System.out.println("No relevant episode title found");
                }

                goodEntries.add(entry);
            }
        }

        return goodEntries;
    }

    private static Date modifyPublishedDate(Date originalDate, int hoursToAdjust) {
        Calendar newPublishedDate = Calendar.getInstance();
        newPublishedDate.setTime(originalDate);
        newPublishedDate.add(Calendar.HOUR, hoursToAdjust);

        return newPublishedDate.getTime();
    }
}
