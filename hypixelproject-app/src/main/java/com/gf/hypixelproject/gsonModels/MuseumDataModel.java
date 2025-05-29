package com.gf.hypixelproject.gsonModels;

import java.util.Map;

public class MuseumDataModel {
    // donation xp for all no armor sets
    public int donation_xp;

    // donation xp for all armor sets (Maps the armor set to donation xp amount)
    public Map<String, Integer> armor_set_donation_xp;

    // Map to see the items parent 
    public Map<String, String> parent;

    // type of item 
    public String type;
}
