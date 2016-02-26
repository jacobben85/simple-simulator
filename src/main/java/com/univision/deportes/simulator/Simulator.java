package com.univision.deportes.simulator;

import java.io.IOException;

/**
 * Simulator
 */
public class Simulator {

    public static void main(String[] args) throws IOException, InterruptedException {
        String eventId = "840462";
        String startTime = "P10M";

        Long sleep = 500L;
        String postUrl = "http://sports.dev.y.univision.com/feeds/xml-team-backfile";

        if (args.length > 0) {
            eventId = args[0];
            System.out.println("Event id : " + eventId);
        }

        if (args.length > 1) {
            sleep = Long.parseLong(args[1]);
            System.out.println("With sleep : " + sleep.toString());
        }

        if (args.length > 2) {
            postUrl = args[2];
            System.out.println("To server : " + postUrl);
        }

        String fixtureKeys = "event-stats,event-stats-progressive,event-commentary";

        //noinspection UnusedAssignment
        String url = "http://staging.xmlteam.com/api/feeds?" +
                "start=" + startTime +
                "&publisher-keys=optasports.com" +
                "&sport-keys=15054000" +
                "&fixture-keys=" + fixtureKeys +
                //"&last-files-by=event-key&fixture-keys=event-stats,event-commentary,event-reports" +
                "&format=xml" +
                "&event-keys=EFBO" + eventId;
        url = "http://staging.xmlteam.com/api/feeds?" +
                "publisher-keys=optasports.com&" +
                "format=xml&" +
                "fixture-keys=schedule-results,event-stats,event-commentary,event-reports" +
                "&start=P3Y&last-files-by=event-key";

        System.out.println(url);

        PostToLocal postToLocal = new PostToLocal();
        postToLocal.setSleepTime(sleep);
        postToLocal.setPostUrl(postUrl);

        postToLocal.fetchLinksAndProcess(url, true);
    }
}
