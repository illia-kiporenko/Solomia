package bots.bot.music.SpotifyParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyPlaylistParser {
    public static String clientTokenUpdate() throws IOException {
        String clientToken;
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://clienttoken.spotify.com/v1/clienttoken");

        // Set request headers
        httpPost.addHeader("accept", "application/json");
        httpPost.addHeader("accept-language", "uk-UA,uk;q=0.9,en-US;q=0.8,en;q=0.7,ru;q=0.6");
        httpPost.addHeader("content-type", "application/json");
        httpPost.addHeader("origin", "https://open.spotify.com");
        httpPost.addHeader("priority", "u=1, i");
        httpPost.addHeader("referer", "https://open.spotify.com/");
        httpPost.addHeader("sec-ch-ua", "\"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\", \"Not-A.Brand\";v=\"99\"");
        httpPost.addHeader("sec-ch-ua-mobile", "?0");
        httpPost.addHeader("sec-ch-ua-platform", "\"Windows\"");
        httpPost.addHeader("sec-fetch-dest", "empty");
        httpPost.addHeader("sec-fetch-mode", "cors");
        httpPost.addHeader("sec-fetch-site", "same-site");
        httpPost.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
        String jsonInputString = "{\"client_data\":{\"client_version\":\"1.2.36.924.g1c1debd7\",\"client_id\":\"d8a5ed958d274c2e8ee717e6a4b0971d\",\"js_sdk_data\":{\"device_brand\":\"unknown\",\"device_model\":\"unknown\",\"os\":\"windows\",\"os_version\":\"NT 10.0\",\"device_id\":\"46dcbc93-7ae9-4696-b941-997405e94749\",\"device_type\":\"computer\"}}}";

        HttpEntity entity = EntityBuilder.create()
                .setText(jsonInputString)
                .build();
        httpPost.setEntity(entity);

        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseBody = EntityUtils.toString(responseEntity);
                JSONObject jsonObject = new JSONObject(responseBody);
                clientToken = jsonObject.getJSONObject("granted_token").getString("token");
                System.out.println(" client token is - " +clientToken);
                return clientToken;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String authorizationUpdate(String refreshToken) {
        try {

            HttpClient httpClient = HttpClients.createDefault();
            String auth = "7f59c534989f4bfaa6a0f9302a10a0c7:934a19f6d028422cb4e6f1076a1401d0";
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            HttpPost request = new HttpPost("https://accounts.spotify.com/api/token");

            // Set headers
            request.setHeader("Authorization", "Basic " + encodedAuth);
            request.setHeader("Content-Type", "application/x-www-form-urlencoded");

            // Add form data
            List<NameValuePair> formParams = new ArrayList<>();
            formParams.add(new BasicNameValuePair("grant_type", "refresh_token"));
            formParams.add(new BasicNameValuePair("refresh_token", refreshToken));

            // Attach form entity
            request.setEntity(new UrlEncodedFormEntity(formParams));


            HttpResponse response = httpClient.execute(request);

            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String inputLine;
            StringBuilder responseCont = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                responseCont.append(inputLine);
            }
            in.close();
            JSONObject jsonObject = new JSONObject(responseCont.toString());
            return jsonObject.getString("access_token");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> parsePlaylist(String link, int compositionAmount, String refresh) throws IOException {

        String accessToken = authorizationUpdate(refresh);
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet("https://api.spotify.com/v1/playlists/" + parsePlaylistId(link)+"?limit="+compositionAmount);

        request.addHeader("authorization", "Bearer "+accessToken);

        HttpResponse response = httpClient.execute(request);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        String line;
        StringBuilder responseContent = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            responseContent.append(line);
        }
        String js = new String(responseContent);


        return (ArrayList<String>) parseSongs(js);
    }

    private static List<String> parseSongs(String jsonResponse) {
        List<String> songsList = new ArrayList<>();

        // Parse the JSON response using JSONObject
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray tracks = jsonObject.getJSONObject("tracks").getJSONArray("items");

        // Iterate through each track and extract name and artist
        for (int i = 0; i < tracks.length(); i++) {
            JSONObject trackObject = tracks.getJSONObject(i).getJSONObject("track");
            String songName = trackObject.getString("name");
            String artistName = trackObject.getJSONArray("artists").getJSONObject(0).getString("name");
            songsList.add(songName + " " + artistName);
        }

        return songsList;
    }

    public static String parsePlaylistId(String link){
        // Regular expression pattern to match the playlist ID
        Pattern pattern = Pattern.compile("/playlist/([a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(link);

        // Find the first match
        if (matcher.find()) {
            // Return the captured group which corresponds to the playlist ID
            return matcher.group(1);
        } else {
            // If no match found, return null or throw an exception as needed
            return null;
        }
    }
    public static String parseTrackId(String spotifyTrackURL) {
        // https://open.spotify.com/track/0o1BqDDRcXxGmCSjZeHUcc?si=d4ad06cc38714fbe
        // Regular expression pattern to match the playlist ID
        String regex = "https://open\\.spotify\\.com/track/(\\w+)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(spotifyTrackURL);

        if (matcher.find()) {
            // Return the captured track ID
            return matcher.group(1);
        } else {
            // If no match found, return null or throw an exception as needed
            return null;
        }
    }
    public static String parseSoloTrack(String link, String refresh) throws IOException {
        String accessToken = authorizationUpdate(refresh);
        //String clientToken = tokenUpdate();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet("https://api.spotify.com/v1/tracks/" + parseTrackId(link));

        request.addHeader("Authorization", "Bearer "+accessToken);

        HttpResponse response = ((CloseableHttpClient) httpClient).execute(request);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        String line;
        StringBuilder responseContent = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            responseContent.append(line);
        }
        String js = new String(responseContent);

        JSONObject jsonObject = new JSONObject(js);

        String songName = jsonObject.get("name").toString();
        String author = jsonObject.getJSONArray("artists").getJSONObject(0).getString("name");

        return songName + " " + author;
    }

    private static String tokenUpdate() {
        try {

            HttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet("https://clienttoken.spotify.com/v1/clienttoken");

            // Set request headers
            request.addHeader("accept", "application/json");
            request.addHeader("accept-language", "uk-UA,uk;q=0.9,en-US;q=0.8,en;q=0.7,ru;q=0.6");
            request.addHeader("content-type", "application/json");
            request.addHeader("origin", "https://open.spotify.com");
            request.addHeader("priority", "u=1, i");
            request.addHeader("referer", "https://open.spotify.com/");
            request.addHeader("sec-ch-ua", "\"Chromium\";v=\"124\", \"Google Chrome\";v=\"124\", \"Not-A.Brand\";v=\"99\"");
            request.addHeader("sec-ch-ua-mobile", "?0");
            request.addHeader("sec-ch-ua-platform", "\"Windows\"");
            request.addHeader("sec-fetch-dest", "empty");
            request.addHeader("sec-fetch-mode", "cors");
            request.addHeader("sec-fetch-site", "same-site");
            request.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
            HttpResponse response = httpClient.execute(request);
            System.out.println(response.getStatusLine().getStatusCode());
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String inputLine;
            StringBuilder responseCont = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                responseCont.append(inputLine);
            }
            in.close();
            Document doc = Jsoup.parse(String.valueOf(responseCont));

            // Select script elements
            Elements scripts = doc.select("script");
            Element scriptElement = doc.getElementById("session");
            String json = scriptElement.html();
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject.getString("accessToken");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
