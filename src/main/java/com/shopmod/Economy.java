package com.shopmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Saldos dos jogadores, guardados em config/simpleshop_balances.json */
public final class Economy {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Double> BAL = new HashMap<>();

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("simpleshop_balances.json");
    }

    public static synchronized void load() {
        BAL.clear();
        try {
            if (Files.exists(file())) {
                try (Reader r = Files.newBufferedReader(file())) {
                    Map<String, Double> m = GSON.fromJson(r, new TypeToken<Map<String, Double>>() {}.getType());
                    if (m != null) BAL.putAll(m);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static synchronized void save() {
        try (Writer w = Files.newBufferedWriter(file())) {
            GSON.toJson(BAL, w);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static synchronized double get(UUID id) { return BAL.getOrDefault(id.toString(), 0.0); }

    public static synchronized void add(UUID id, double amount) {
        BAL.put(id.toString(), Math.round((get(id) + amount) * 100.0) / 100.0);
        save();
    }

    public static synchronized boolean take(UUID id, double amount) {
        if (get(id) + 1e-9 < amount) return false;
        add(id, -amount);
        return true;
    }

    /** 1500 -> $1.5K, 2000000 -> $2M */
    public static String fmt(double v) {
        String[] suf = {"", "K", "M", "B", "T"};
        int i = 0;
        while (v >= 1000 && i < suf.length - 1) { v /= 1000; i++; }
        String s = (i == 0) ? String.format("%.2f", v) : String.format("%.2f", v);
        if (s.contains(".")) s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return "$" + s + suf[i];
    }
}
