package eu.toldi.infinityforlemmy.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import android.util.Log;

public class LemmyUtils {

    private static final String TAG = "LemmyUtils";

    public static String actorID2FullName(String url) {
        String[] splitURL = url.split("/");
        if (splitURL.length < 3) {
            Log.e(TAG, "Invalid URL format: " + url);
            return "defaultUser@defaultDomain";
        }
        String userName = splitURL[splitURL.length - 1];
        String domain = splitURL[2];
        return (userName.contains("@")) ? userName : userName + "@" + domain;
    }

    public static String qualifiedCommunityName2ActorId(String qualifiedName) {
        Log.d(TAG, "qualifiedCommunityName2ActorId called with: " + qualifiedName);
        String[] splitQualifiedName = qualifiedName.split("@");
        if (splitQualifiedName.length < 2) {
            Log.e(TAG, "Invalid qualified name format: " + qualifiedName);
            return "https://defaultDomain/c/defaultUser";
        }
        String userName = splitQualifiedName[0];
        String domain = splitQualifiedName[1];
        return "https://" + domain + "/c/" + userName;
    }

    public static String qualifiedUserName2ActorId(String qualifiedName) {
        Log.d(TAG, "qualifiedUserName2ActorId called with: " + qualifiedName);
        String[] splitQualifiedName = qualifiedName.split("@");
        if (splitQualifiedName.length < 2) {
            Log.e(TAG, "Invalid qualified name format: " + qualifiedName);
            return "https://defaultDomain/u/defaultUser";
        }
        String userName = splitQualifiedName[0];
        String domain = splitQualifiedName[1];
        return "https://" + domain + "/u/" + userName;
    }

    public static String qualifiedMagazineName2ActorId(String qualifiedName) {
        Log.d(TAG, "qualifiedMagazineName2ActorId called with: " + qualifiedName);
        String[] splitQualifiedName = qualifiedName.split("@");
        if (splitQualifiedName.length < 2) {
            Log.e(TAG, "Invalid qualified name format: " + qualifiedName);
            return "https://defaultDomain/m/defaultUser";
        }
        String userName = splitQualifiedName[0];
        String domain = splitQualifiedName[1];
        return "https://" + domain + "/m/" + userName;
    }

    public static Long dateStringToMills(String dateStr) {
        long postTimeMillis = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                postTimeMillis = ZonedDateTime.parse(dateStr,
                        DateTimeFormatter.ISO_DATE_TIME.withZone(ZoneId.of("Z"))).toInstant().toEpochMilli();
            } catch (Exception e) {
                Log.e(TAG, "Invalid date format: " + dateStr, e);
                return 0L; // 기본값
            }
        } else {
            dateStr = dateStr.substring(0, dateStr.lastIndexOf(".") + 4) + 'Z';
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date date = sdf.parse(dateStr);
                if (date != null) {
                    postTimeMillis = date.getTime();
                }
            } catch (ParseException e) {
                Log.e(TAG, "Invalid date format: " + dateStr, e);
                return 0L; // 기본값
            }
        }
        return postTimeMillis;
    }
}
