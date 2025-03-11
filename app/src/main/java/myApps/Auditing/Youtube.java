package myApps.Auditing;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Iterator;

import java.util.ArrayList;

public class Youtube {
  public static String API_KEY = "AIzaSyBqBInwyq36rEw1qOBJFYyRwlGoDeInEJY";
  public static String API = "https://www.googleapis.com/youtube/v3/";
  
  public static class YoutubeObject{
    String kind;
    YoutubeObject(String k){
     kind = k;
    }
  }
  public static class Video extends YoutubeObject{
    String id;
    private String title;
    private String description;
    private String date;
    private String channelId;
    private String duration;

    Video (String id){
      super("video");
      this.id=id;
    }
    private void load_snippet(){
      String link = API+"videos";
      link += "?part=snippet";
      link += "&id="+id;
      link += "&key="+API_KEY;
      String response = Network.requestGet(link);
      try {
        JSONObject json = new JSONObject(response);
        JSONObject snippet = json.getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
        title = snippet.getString("title");
        description = snippet.getString("description");
        channelId = snippet.getString("channelId");
        date = snippet.getString("publishedAt");
      } catch (Exception e){
         CmdActivity.println(e.toString());
      }
    }
    String getTitle(){
      if (title==null)
        load_snippet();
      return title;
    }
    @Override
    public String toString(){
      return id;
    }
  }

  public static class Playlist extends YoutubeObject{
    public String id;
    private String title;
    private String description;
    private String channelId;
    private ArrayList<String> itemsId = new ArrayList<>();
    private ArrayList<Video> items = new ArrayList<>();
    private int itemCount;

    Playlist(String id){
      super("playlist");
      this.id = id;
    }
    private void load_snippet() {
      String link = API+"playlists";
      link += "?part=snippet";
      link += "&id="+id;
      link += "&key="+API_KEY;
      String response = Network.requestGet(link);
      try {
        JSONObject json = new JSONObject(response);
        JSONObject snippet = json.getJSONArray("items").getJSONObject(0).getJSONObject("snippet");
        title = snippet.getString("title");
        description = snippet.getString("description");
        channelId = snippet.getString("channelId");
      } catch (Exception e){
         CmdActivity.println(e.toString());
      }
    }
    public String[] getItemsId(){
      if (itemsId.size() != 0)
        getItems();
      return (String[]) itemsId.toArray(new String[itemsId.size()]);
    }
    public Video[] getItems(){
      if (this.items.size() != 0)
        return (Video[]) this.items.toArray(new Video[this.items.size()]);
      String request = API+"playlistItems"
        +"?part=snippet"
        +"maxResults=50"
        +"&playlistId="+id
        +"&key="+API_KEY;
      String response = Network.requestGet(request);
      try {
        JSONObject json = new JSONObject(response);
        JSONArray items = json.getJSONArray("items");
        JSONObject item;
        JSONObject snippet;
        for (int i=0; i<items.length(); i++){
          item = items.getJSONObject(i);
          snippet = item.getJSONObject("snippet").getJSONObject("resourceId");
          this.itemsId.add(snippet.getString("videoId"));
          this.items.add(new Video(snippet.getString("videoId")));
        }
        
      } catch (Exception e){
         CmdActivity.println(e.toString());
      }
      return (Video[]) this.items.toArray(new Video[this.items.size()]);
    }
    @Override
    public String toString(){
      return id;
    }
  }
  public static class Search extends YoutubeObject{
    String query;
    ArrayList<YoutubeObject> items = new ArrayList<>();

    Search(String q){
      super("search");
      query = q;
    }
    YoutubeObject[] getItems() {
      if (this.items.size() != 0)
        return (YoutubeObject[]) this.items.toArray(new YoutubeObject[this.items.size()]);
      //else
      String request = API+"search"
        +"?part=snippet"
        +"&q="+query.replace(" ","+")
        +"&maxResults=50"
        +"&key="+API_KEY;
        //+"&type=video"
        //+"&order=viewCount"
        //+"&regionCode=US"
        //+"&safeSearch=strict";
          //afer ? order is not nessisary and & is the seperator
      String response = Network.requestGet(request);
      try {
        JSONObject json = new JSONObject(response);
        JSONArray items = json.getJSONArray("items");
        JSONObject item;
        String kind;
        
        for (int i=0; i<items.length(); i++){
          item = items.getJSONObject(i);
          kind = item.getJSONObject("id").getString("kind");
          kind = kind.substring(8,kind.length());
          if ("video".equals(kind)){
            this.items.add(new Video(item.getJSONObject("id").getString("videoId")));
          }
          else if ("playlist".equals(kind)){
            this.items.add(new Playlist(item.getJSONObject("id").getString("playlistId")));
          }
        }
      } catch (Exception e){
         CmdActivity.println("Exception on Youtube.Search.getItems(): "+e.toString());
      }
      return (YoutubeObject[]) this.items.toArray(new YoutubeObject[this.items.size()]);
    }
  }
  public static void main(String[] args) {
        System.out.println("Hello, World!");
      //search("fire");
    for (YoutubeObject item: new Search("كتاب الفتن لابن كثير").getItems()){
      if ("video".equals(item.kind)){
        System.out.println("video: "+((Video) item).id);
      }
      else if ("playlist".equals(item.kind)){
        System.out.println("playlist: "+((Playlist) item).id);
      }
    }
  }
}

class Network {
  private Network(){}
  static String requestGet(String link){
    HttpURLConnection connection = null;
    BufferedReader reader = null;
        
    try {
      // Create URL object
      URL url = new URL(link);

      // Open connection
      connection = (HttpURLConnection) url.openConnection();

      // Set request method
      connection.setRequestMethod("GET");

      // Read response
      reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder response = new StringBuilder();
      String line;

      while ((line = reader.readLine()) != null) {
        response.append(line);
      }

      return response.toString();
    } catch (Exception e) {
       CmdActivity.println("Exception on Network.requestGet(): "+e.toString());
    } finally {
      // Close resources
      if (connection != null) {
        connection.disconnect();
      }
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (Exception e) {
          CmdActivity.println("Exeption when closing reader on Network.requeatGet(): "+e.toString());
      }
    }
    return null;
  }
}
