package com.paletiers.tiertagger.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.paletiers.tiertagger.PaleTiers;
import com.paletiers.tiertagger.util.GamemodeUtil;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class PaleTiersApi {
    private static final String API_BASE_URL = "https://paletiers.xyz/api/tiers/";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private PaleTiersApi() {}

    public static CompletableFuture<PaleTiersData> fetchPlayerTier(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String encodedName = URLEncoder.encode(playerName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
                String url = API_BASE_URL + encodedName;
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    PaleTiers.LOGGER.warn("Failed to fetch tier for {}: HTTP {}", playerName, response.statusCode());
                    return null;
                }

                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                return parsePlayerData(json, playerName);
            } catch (Exception e) {
                PaleTiers.LOGGER.error("Error fetching tier for {}: {}", playerName, e.getMessage());
                return null;
            }
        });
    }

    private static PaleTiersData parsePlayerData(JsonObject json, String fallbackPlayerName) {
        try {
            String playerName = stringOr(json, "ingame_username", fallbackPlayerName);
            PaleTiersData tierData = new PaleTiersData(playerName);

            if (json.has("tiers") && json.get("tiers").isJsonObject()) {
                JsonObject tiers = json.getAsJsonObject("tiers");
                for (Map.Entry<String, JsonElement> entry : tiers.entrySet()) {
                    if (!entry.getValue().isJsonObject()) {
                        continue;
                    }

                    JsonObject tierObject = entry.getValue().getAsJsonObject();
                    String gamemodeName = stringOr(tierObject, "gamemode_name", entry.getKey());
                    if ("nethpot".equalsIgnoreCase(entry.getKey())) {
                        gamemodeName = "Netherite";
                    }
                    if (gamemodeName.isBlank()) {
                        continue;
                    }

                    String normalizedGamemode = GamemodeUtil.normalize(gamemodeName);
                    String tier = stringOr(tierObject, "tier", "Unranked");
                    if (tier.isBlank()) {
                        tier = "Unranked";
                    }

                    boolean retired = booleanOr(tierObject, "retired", false);
                    tierData.setTierForGamemode(normalizedGamemode, tier, retired);
                }
            }

            PaleTiers.registerAvailableGamemodes(tierData.getAllTiers().keySet());
            return tierData;
        } catch (Exception e) {
            PaleTiers.LOGGER.error("Error parsing player data: {}", e.getMessage());
            return null;
        }
    }

    private static String stringOr(JsonObject json, String key, String fallback) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }
        return json.get(key).getAsString();
    }

    private static boolean booleanOr(JsonObject json, String key, boolean fallback) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return fallback;
        }
        return json.get(key).getAsBoolean();
    }

    private static int getTierValue(String tier) {
        if (tier == null || tier.equals("Unranked")) {
            return 999;
        }
        if (tier.startsWith("R")) {
            tier = tier.substring(1);
        }
        if (tier.startsWith("HT")) {
            return (Integer.parseInt(tier.substring(2)) * 2) - 1;
        }
        if (tier.startsWith("LT")) {
            return Integer.parseInt(tier.substring(2)) * 2;
        }
        return 999;
    }

    public static final class PaleTiersData {
        public final String playerName;
        public final long fetchTime;
        private final Map<String, TierInfo> gamemodeTiers = new ConcurrentHashMap<>();

        public PaleTiersData(String playerName) {
            this.playerName = playerName;
            this.fetchTime = System.currentTimeMillis();
        }

        public void setTierForGamemode(String gamemode, String tier, boolean retired) {
            gamemodeTiers.put(gamemode, new TierInfo(tier, retired));
        }

        public String getTierForGamemode(String gamemode) {
            TierInfo info = gamemodeTiers.get(gamemode);
            return info != null ? info.tier : "Unranked";
        }

        public boolean isRetired(String gamemode) {
            TierInfo info = gamemodeTiers.get(gamemode);
            return info != null && info.retired;
        }

        public boolean hasTierForGamemode(String gamemode) {
            TierInfo info = gamemodeTiers.get(gamemode);
            return info != null && !info.tier.equals("Unranked");
        }

        public Map<String, TierInfo> getAllTiers() {
            return gamemodeTiers;
        }

        public String getHighestTier() {
            String highestTier = "Unranked";
            int highestTierValue = 999;
            for (TierInfo info : gamemodeTiers.values()) {
                int tierValue = getTierValue(info.tier);
                if (tierValue < highestTierValue) {
                    highestTierValue = tierValue;
                    highestTier = info.tier;
                }
            }
            return highestTier;
        }

        public String getHighestTierGamemode() {
            String highestGamemode = null;
            int highestTierValue = 999;
            for (Map.Entry<String, TierInfo> entry : gamemodeTiers.entrySet()) {
                int tierValue = getTierValue(entry.getValue().tier);
                if (tierValue < highestTierValue) {
                    highestTierValue = tierValue;
                    highestGamemode = entry.getKey();
                }
            }
            return highestGamemode;
        }

        public boolean isExpired(long cacheTimeMs) {
            return System.currentTimeMillis() - fetchTime > cacheTimeMs;
        }
    }

    public static final class TierInfo {
        public final String tier;
        public final boolean retired;

        public TierInfo(String tier, boolean retired) {
            this.tier = tier;
            this.retired = retired;
        }
    }
}
