package net.ttt.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.ttt.main.Main;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

public class SkullBuilder {

    private Main plugin;
    private HashMap<String, String> cachedTextures;

    public SkullBuilder(Main plugin) {
        this.plugin = plugin;
        cachedTextures = new HashMap<>();
    }

    public ItemStack getCustomTextureHead(String value) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", value));
        Field profileField = null;
        try {
            profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        head.setItemMeta(meta);
        return head;
    }

    public ItemStack HeadItemDisplay(OfflinePlayer p, String displayName) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta pSkull = (SkullMeta) item.getItemMeta();
        pSkull.setDisplayName(displayName);
        pSkull.setOwner(p.getName());
        item.setItemMeta(pSkull);
        return item;
    }

    public String getHeadValueByName(String name) {
        try {
            String result = getURLContent("https://api.minetools.eu/uuid/" + name);
            Gson g = new Gson();
            JsonObject obj = g.fromJson(result, JsonObject.class);
            String uid = obj.get("id").toString().replace("\"", "");
            String signature = getURLContent("https://api.minetools.eu/profile/" + uid);
            obj = g.fromJson(signature, JsonObject.class);
            return obj.getAsJsonObject("raw").getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHeadValueByUUID(String uuid) {
        try {
            String signature = getURLContent("https://api.minetools.eu/profile/" + uuid);
            JsonObject obj = new Gson().fromJson(signature, JsonObject.class);
            return obj.getAsJsonObject("raw").getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCachedHeadValueByUUID(String uuid) {
        if (cachedTextures.containsKey(uuid))
            return cachedTextures.get(uuid);
        else
            cachedTextures.put(uuid, getHeadValueByUUID(uuid));
            return cachedTextures.get(uuid);
    }

    private String getURLContent(String urlStr) {
        try {
            Scanner sc = new Scanner(new URL(urlStr).openStream(), "UTF-8").useDelimiter("\\A");
            String content = sc.next();
            sc.close();
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

}
