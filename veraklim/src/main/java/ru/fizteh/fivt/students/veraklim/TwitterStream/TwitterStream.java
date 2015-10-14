package ru.fizteh.fivt.students.veraklim.TwitterStream;

import twitter4j.*;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.JCommander;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.Bounds;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

import java.util.Date;
import java.util.List;

import static java.lang.Thread.sleep;

//import com.beust.jcommander.JCommander;

/**
 * Created by Vera on 21.09.2015.
 */

class Parameters {

    public static final int HUNDRED = 100;
    @Parameter(names = {"-q", "--query"},
            description = "Запрос или ключевые слова")
    private String query = "";
    @Parameter(names = {"-p", "--place"},
            description = "Место поиска")
    private String place = "";
    @Parameter(names = {"-s", "--stream"},
            description = "Стрим")
    private boolean stream = false;
    @Parameter(names = {"--hideRetwitts"},
            description = "Прятать ретвиты")
    private boolean hideRetwitts = false;
    @Parameter(names = {"-h", "--help"},
            description = "Выводит подсказку")
    private boolean help = false;
    @Parameter(names = {"-l", "--limit"},
            description = "Ограничение на количество" + " выводимых твитов (не в стриме)")
    private Integer limit = HUNDRED;

    public final String getQuery() {
        return query;
    }

    public final String getPlace() {
        return place;
    }

    public final Integer getLimit() {
        return limit;
    }

    public final boolean isStream() {
        return stream;
    }

    public final boolean isHideRetwitts() {
        return hideRetwitts;
    }

    public final boolean isHelp() {
        return help;
    }
};

class FindPlace {
    private static final double R = 6371;
    private GeocodingResult[] result;
    private double radius;

    FindPlace(String place) {
        GeoApiContext context = new GeoApiContext()
                .setApiKey("AIzaSyCAhkvmjepUzQUh9pA7g0K4QoQY2ncBno8");
        try {
            result = GeocodingApi.geocode(context, place).await();
            radius = calculateRadius();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    private double calculateRadius() {
        double x1 = Math.toRadians(result[0].geometry.bounds.northeast.lat);
        double x2 = Math.toRadians(result[0].geometry.bounds.southwest.lat);
        double dx = x1 - x2;
        double lambda1;
        lambda1 = Math.toRadians(result[0].geometry.bounds.northeast.lng);
        double lambda2;
        lambda2 = Math.toRadians(result[0].geometry.bounds.southwest.lng);
        double dLambda = lambda1 - lambda2;

        double a = Math.sin(dx / 2) * Math.sin(dx / 2)
                + Math.cos(x1) * Math.cos(x2)
                * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        return distance / 2;
    }

    public final LatLng getLocation() {
        return result[0].geometry.location;
    }
    public final double getRadius() {
        return radius;
    }
    public final Bounds getBounds() {
        return result[0].geometry.bounds;
    }
};

public class TwitterStream {

    public static final long MIN = 1000 * 60;
    public static final long HOUR = 1000 * 60 * 60;
    public static final long DAY = 1000 * 60 * 60 * 24;
    public static final short FIVE = 5;
    public static final short TEN = 10;
    public static final short ELEVEN = 11;
    public static final int TWELVE = 12;
    public static final int PAUSE = 1000;
    public static final short MINUTS_ID = 0;
    public static final short HOURS_ID = 1;
    public static final short DAYS_ID = 2;

    public static String strTime(long time, short ID){
        if (time % TEN == 1 && time != ELEVEN) {
            if (ID == MINUTS_ID) {
                return "минуту";
            } else {
                if (ID == HOURS_ID) {
                    return "час";
                } else {
                    return "день";
                }
            }
        } else {
            if (time % TEN > 1 && time % TEN < FIVE
                    && time != TWELVE) {
                if (ID == MINUTS_ID) {
                    return "минут";
                } else {
                    if (ID == HOURS_ID) {
                        return "часа";
                    } else {
                        return "дня";
                    }
                }
            } else {
                if (ID == MINUTS_ID) {
                    return "минут";
                } else {
                    if (ID == HOURS_ID) {
                        return "часов";
                    } else {
                        return "дней";
                    }
                }
            }
        }
    }

    public static String strRetweet(long retweets) {
        if (retweets == 0) {
            return "";
        }
        if (retweets % TEN == 1 && retweets != ELEVEN) {
            return  " (" + retweets + " ретвит)";
        }
        if (retweets % TEN > 1 && retweets % TEN < FIVE && retweets!=TWELVE) {
            return  " (" + retweets + " ретвита)";
        } else
            return " (" + retweets + " ретвитов)";
    }

    public static boolean today(Status status) {
        Date date = new Date();
        long currentTime = date.getTime();
        long tweetTime = status.getCreatedAt().getTime();
        long currentDay = currentTime / DAY;
        return (currentTime - currentDay * DAY) >= currentTime - tweetTime;
    }

    public static boolean yesterday(Status status) {
        Date date = new Date();
        long currentTime = date.getTime();
        long tweetTime = status.getCreatedAt().getTime();
        long previousDay = (currentTime - DAY) / DAY;
        return (currentTime - previousDay * DAY) >= currentTime - tweetTime;
    }

    public static void printTime(Status tweet) {
        Date date = new Date();
        long currentTime = date.getTime();
        long tweetTime = tweet.getCreatedAt().getTime();
        long dmin = (currentTime - tweetTime) / MIN;
        long dhour = (currentTime - tweetTime) / HOUR;
        long dday = (currentTime - tweetTime) / DAY;
        System.out.print("[");
        if (dmin <= 2) {
            System.out.print("только что");
        } else {
            if (dhour < 1) {
                System.out.print(dmin +" " +strTime(dmin, MINUTS_ID) + " назад");
            } else {
                if (today(tweet)) {
                    System.out.print(dhour +" "+  strTime(dhour, HOURS_ID) + " назад");
                } else {
                    if (yesterday(tweet)) {
                        System.out.print(" вчера");
                    } else {
                        System.out.print(dday +" "+ strTime(dday, DAYS_ID) + " назад");
                    }
                }
            }
        }
        System.out.print("]");
    }

    public static void printTweet(Status status, boolean hideRetweets) {
        if (status.isRetweet()) {
            if (!hideRetweets) {
                printTime(status);
                System.out.println("@" + status.getUser().getName() + " ретвитнул: @" + status.getRetweetedStatus().getUser().getName()
                        + ": " + status.getRetweetedStatus().getText());
            }
        } else {
            printTime(status);
            System.out.println("@" + status.getUser().getName() + ": " + status.getText() + strRetweet(status.getRetweetCount()));
            System.out.println();
        }
    }

    public static Query setQuery(Parameters param) throws Exception {
        Query query = new Query(param.getQuery());
        if (!param.getPlace().isEmpty()) {
            FindPlace googleFindPlace;
            googleFindPlace = new FindPlace(param.getPlace());
            GeoLocation geoLocation;
            geoLocation = new GeoLocation(googleFindPlace.getLocation().lat, googleFindPlace.getLocation().lng);
            query.setGeoCode(geoLocation, googleFindPlace.getRadius(), Query.KILOMETERS);
        }
        return query;
    }

    public static void search(Parameters param) throws Exception {
        Twitter twitter = new TwitterFactory().getInstance();
        Query query = setQuery(param);
        QueryResult result;
        int limit = param.getLimit();
        int statusCount = 0;
        do {
            result = twitter.search(query);
            List<Status> tweets = result.getTweets();
            for (Status status : tweets) {
                if (status.isRetweet() && param.isHideRetwitts())
                    continue;
                printTime(status);
                printTweet(status, param.isHideRetwitts());
                statusCount++;
                limit--;
                if (limit == 0) {
                    break;
                }
            }
            query = result.nextQuery();
        } while (query != null && limit > 0);
        if (statusCount == 0) {
            System.out.println("Подходящих твитов нет");
        }
    }

    public static FilterQuery setFilter(Parameters param) throws Exception{
        String[] track = new String[1];
        track[0] = param.getQuery();
        long[] follow = new long[0];
        FilterQuery filter = new FilterQuery(0, follow, track);
        if (!param.getPlace().isEmpty()) {
            FindPlace googleFindPlace;
            googleFindPlace = new FindPlace(param.getPlace());
            double[][] bounds = {{googleFindPlace.getBounds().southwest.lng, googleFindPlace.getBounds().southwest.lat},
                    {googleFindPlace.getBounds().northeast.lng, googleFindPlace.getBounds().northeast.lat}};
            filter.locations(bounds);
        }
        return filter;
    }

    public static void stream(Parameters param, StatusListener listener) throws Exception {
        twitter4j.TwitterStream twitterStream;
        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(listener);
        if (param.getQuery() == "" && param.getPlace() == "") {
            twitterStream.sample();
        } else {
            FilterQuery filter = setFilter(param);
            twitterStream.filter(filter);
        }
    }

    public static void main(String[] args) throws Exception {
        final Parameters param = new Parameters();
        JCommander cmd = new JCommander(param, args);
        if (param.isHelp()) {
            cmd.usage();
            System.exit(0);
        }
        if (param.isStream()) {
            StatusListener listener = new StatusAdapter() {
                @Override
                public void onStatus(Status status) {
                    printTweet(status, param.isHideRetwitts());
                    try {
                        sleep(PAUSE);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                }
            };
            try {
                stream(param, listener);
            } catch (TwitterException e) {
                System.out.println(e.getMessage());
                stream(param, listener);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.exit(-1);
            }
        } else {
            try {
                search(param);
            } catch (TwitterException e) {
                System.out.println(e.getMessage());
                search(param);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.exit(-1);
            }
        }
    }
};

