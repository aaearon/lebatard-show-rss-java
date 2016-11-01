package com.iosharp;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.XmlReader;
import org.apache.log4j.Logger;

import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class App {
    final static private String FEED_URL = "http://espn.go.com/espnradio/feeds/rss/podcast.xml?id=9941853";
    private static String FILEPATH;

    final static Logger logger = Logger.getLogger(App.class);

    public static void main(String[] args) {

        try {
            FILEPATH = args[0];
        } catch (Exception e) {
            logger.fatal("No filepath as argument!", e);
        }

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
            logger.fatal(e);
        }
    }

    private static SyndFeed getFeed(String feedUrl) {

        SyndFeed feed = null;

        try {
            URL url = new URL(feedUrl);

            SyndFeedInput input = new SyndFeedInput();
            feed = input.build(new XmlReader(url));
            logger.debug(String.format("Getting feed from %s", feedUrl));
            return feed;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Something went wrong getting the feed", e);
        }

        return feed;
    }

    private static List<SyndEntry> getGoodEntries(SyndFeed feed) {
        List<SyndEntry> goodEntries = new ArrayList<SyndEntry>();

        for (SyndEntry e : feed.getEntries()) {
            if (e.getTitle().toLowerCase().contains("hour")) {
                SyndEntry entry = e;
                String entryTitle = entry.getTitle().toLowerCase();

                int offset;

                if (entryTitle.contains("local hour")) {
                    offset = -1;
                } else if (entryTitle.contains("hour 1")) {
                    offset = 1;
                } else if (entryTitle.contains("hour 2")) {
                    offset = 2;
                } else if (entryTitle.contains("hour 3")) {
                    offset = 3;
                } else {
                    offset = 0;
                }

                if (offset != 0) {
                    logger.debug(String.format("Found episode %s, setting publish date %s hour", e.getTitle(), offset));
                    entry.setPublishedDate(modifyPublishedDate(e.getPublishedDate(), offset));
                } else {
                    logger.warn(String.format("Unsure what to do with title: %s, not setting publish date", e.getTitle()));
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
