package com.paletiers.tiertagger.cache;

import com.paletiers.tiertagger.PaleTiers;
import com.paletiers.tiertagger.api.PaleTiersApi;
import com.paletiers.tiertagger.config.ModConfig;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class TierCache {
    private static final long FAILURE_RETRY_DELAY_MS = 60_000;
    private static final Map<String, PaleTiersApi.PaleTiersData> PLAYER_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, CompletableFuture<PaleTiersApi.PaleTiersData>> IN_FLIGHT = new ConcurrentHashMap<>();
    private static final Map<String, Long> FAILED_UNTIL = new ConcurrentHashMap<>();

    private TierCache() {}

    public static void init() {
        PLAYER_CACHE.clear();
        IN_FLIGHT.clear();
        FAILED_UNTIL.clear();
    }

    public static PaleTiersApi.PaleTiersData getTierData(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return null;
        }

        String key = playerName.toLowerCase();
        PaleTiersApi.PaleTiersData cached = PLAYER_CACHE.get(key);
        if (cached == null || cached.isExpired(ModConfig.getCacheTime())) {
            fetchIfNeeded(playerName);
        }
        return cached;
    }

    public static CompletableFuture<PaleTiersApi.PaleTiersData> fetchNow(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        String key = playerName.toLowerCase();
        PaleTiersApi.PaleTiersData cached = PLAYER_CACHE.get(key);
        if (cached != null && !cached.isExpired(ModConfig.getCacheTime())) {
            return CompletableFuture.completedFuture(cached);
        }
        return fetchIfNeeded(playerName);
    }

    public static int getCacheSize() {
        return PLAYER_CACHE.size();
    }

    private static CompletableFuture<PaleTiersApi.PaleTiersData> fetchIfNeeded(String playerName) {
        String key = playerName.toLowerCase();
        long now = System.currentTimeMillis();
        Long retryAt = FAILED_UNTIL.get(key);
        if (retryAt != null && now < retryAt) {
            return CompletableFuture.completedFuture(null);
        }

        return IN_FLIGHT.computeIfAbsent(key, ignored ->
            PaleTiersApi.fetchPlayerTier(playerName).whenComplete((result, throwable) -> {
                try {
                    if (throwable != null) {
                        PaleTiers.LOGGER.warn("Failed to refresh {}: {}", playerName, throwable.getMessage());
                        FAILED_UNTIL.put(key, System.currentTimeMillis() + FAILURE_RETRY_DELAY_MS);
                        return;
                    }
                    if (result != null) {
                        PLAYER_CACHE.put(key, result);
                        FAILED_UNTIL.remove(key);
                    } else {
                        FAILED_UNTIL.put(key, System.currentTimeMillis() + FAILURE_RETRY_DELAY_MS);
                    }
                } finally {
                    IN_FLIGHT.remove(key);
                }
            })
        );
    }
}
