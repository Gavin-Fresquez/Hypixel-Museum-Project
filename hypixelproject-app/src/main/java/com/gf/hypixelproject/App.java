package com.gf.hypixelproject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gf.hypixelproject.gsonModels.ItemsModel;
import com.gf.hypixelproject.gsonModels.ProfilesModel;
import com.gf.hypixelproject.gsonModels.UuidModel;
import com.gf.hypixelproject.gsonModels.ItemsDataModel;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Minecraft username: ");
        String userName = sc.nextLine();
        
        //======= load api key from .env === //
        Dotenv dotenv = Dotenv.configure()
            .directory("hypixelproject-app/")
            .load();
        String hypixelKey = dotenv.get("HYPIXEL_API_KEY");
        System.out.println("Hypixel key: " + hypixelKey);
        //=================================== //

        System.out.print("Enter Profile Name: ");
        String profile = sc.nextLine();

        HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

        // ===========================================================
        // request to minecraft to get uuid
        UuidModel uuid = new UuidModel();
        try {

            String url = "https://api.mojang.com/users/profiles/minecraft/" + userName;
            URI minecraftUrl = new URI(url);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(minecraftUrl)
                .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            String body = response.body();
            uuid = gson.fromJson(body, UuidModel.class);
            System.out.println(userName + "'s uuid is: " + uuid.id);

        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
        // end of minecraft request 
        // ===========================================================

        // ===========================================================
        // get profile id using uuid
        ProfilesModel profileIds = new ProfilesModel();
        String userProfileId = ""; // should be instance variable
        try {

            String url = "https://api.hypixel.net/v2/skyblock/profiles?uuid=" + uuid.id; // dependent on a instance varabile of uuid (Scope)
            URI profileUrl = new URI(url); 

            HttpRequest request = HttpRequest.newBuilder()
                .uri(profileUrl)
                .header("API-key", hypixelKey)
                .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            String body = response.body();
            profileIds = gson.fromJson(body, ProfilesModel.class);

            for (int i = 0; i < profileIds.profiles.length; i++) {
                if (profileIds.profiles[i].cute_name.equalsIgnoreCase(profile)) {
                    userProfileId = profileIds.profiles[i].profile_id; 
                }
            }
            System.out.println(userProfileId + " should be : 75d7f5d3-b3f2-43e2-8c7e-28c5eb62c5e9");

        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
        // end of profile id request
        // ===========================================================

        // ===========================================================
        // get all items that have museum data and store them in a map
        ItemsModel allItems = new ItemsModel();
        try {

            URI itemsUrl = new URI("https://api.hypixel.net/resources/skyblock/items");

            HttpRequest request = HttpRequest.newBuilder()
                .uri(itemsUrl)
                .header("API-key", hypixelKey)
                .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            String body = response.body();
            allItems = gson.fromJson(body, ItemsModel.class);

            for (int i = 0; i < allItems.items.length; i++) { // maybe change naming little confusing
                if (allItems.items[i].museum_data != null && !allItems.items[i].museum_data.type.equals("ARMOR_SETS")) {
                    System.out.println(allItems.items[i].name + " | XP: " + allItems.items[i].museum_data.donation_xp);
                } else if (allItems.items[i].museum_data != null) {
                    for (Map.Entry<String, Integer> entry : allItems.items[i].museum_data.armor_set_donation_xp.entrySet()) {
                        System.out.println(allItems.items[i].name + " | Set: " + entry.getKey() + " | XP: " + entry.getValue());
                        // Makes a map entry and set it to the entry set of the given item
                    }
                }
                // Armor sets dont have regular donation xp they have set xp, 
                // so how do I combine the sets into one thing give a total price and dvide by xp amout for the set
                // plus each set has a custom variable for the xp 
                // ===========================================================
                // store in map
                // if it has museum_data but its type isnt "ARMOR_SET" then donation_xp is not null
                // if it has museum_data and its type is "ARMOR_SET" then armor_set_donation_xp must have a map entry.
                // ===========================================================
                // ===========================================================
                // Now merge both sets into one hash map to get all musuem items with their given set name (if exist) and xp
                // if it has armor_set_donation_xp then map that to the set name else if it has donation_xp then map it to the item name 
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
        
        // ===========================================================
        // compare to usernames museum and get missing items

        try {
           // need to get players musuem items stored in a map then compare to the allItems map
           String url = "https://api.hypixel.net/v2/skyblock/museum?profile=" + userProfileId;
           URI playerMuseumUrl = new URI(url);

           HttpRequest request = HttpRequest.newBuilder()
                .uri(playerMuseumUrl)
                .header("API-key", hypixelKey)
                .build();

            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            String body = response.body();
            // no array in the api call maybe make and fill one here with the items 

        } catch (URISyntaxException | IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
        sc.close();
    }
}
