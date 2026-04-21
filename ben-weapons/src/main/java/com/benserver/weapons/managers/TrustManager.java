package com.benserver.weapons.managers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TrustManager {

    // Maps each player's UUID to the set of UUIDs they have trusted
    private final Map<UUID, Set<UUID>> trustLists = new HashMap<>();

    public void trust(UUID owner, UUID trusted) {
        trustLists.computeIfAbsent(owner, k -> new HashSet<>()).add(trusted);
    }

    public void untrust(UUID owner, UUID trusted) {
        Set<UUID> list = trustLists.get(owner);
        if (list != null) list.remove(trusted);
    }

    public boolean isTrusted(UUID owner, UUID target) {
        Set<UUID> list = trustLists.get(owner);
        return list != null && list.contains(target);
    }

    public Set<UUID> getTrusted(UUID owner) {
        return trustLists.getOrDefault(owner, Collections.emptySet());
    }
}
