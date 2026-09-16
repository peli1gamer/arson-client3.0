package io.arson.client.module;

import io.arson.client.settings.BooleanSetting;
import io.arson.client.settings.StringSetting;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Lightweight friend list used by combat targeting modules. */
public final class FriendsModule extends Module {
    private final BooleanSetting protectFriends = setting(new BooleanSetting("protect-friends", "Protect Friends", true));
    private final StringSetting friends = setting(new StringSetting("friends", "Friends", "", 1024));
    private final Set<String> cachedNames = new HashSet<>();
    private String cachedValue = null;

    public FriendsModule() {
        super("friends", "Friends", Category.COMBAT);
    }

    public boolean protectFriends() {
        return protectFriends.enabled();
    }

    public boolean isFriend(Player player) {
        if (player == null || !protectFriends()) return false;
        rebuildCache();
        return cachedNames.contains(player.getName().getString().toLowerCase(Locale.ROOT));
    }

    public void addFriend(String name) {
        if (name == null || name.isBlank()) return;
        rebuildCache();
        cachedNames.add(name.trim().toLowerCase(Locale.ROOT));
        writeCache();
    }

    public void removeFriend(String name) {
        if (name == null) return;
        rebuildCache();
        cachedNames.remove(name.trim().toLowerCase(Locale.ROOT));
        writeCache();
    }

    private void rebuildCache() {
        String value = friends.get();
        if (value.equals(cachedValue)) return;
        cachedNames.clear();
        Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .forEach(cachedNames::add);
        cachedValue = value;
    }

    private void writeCache() {
        cachedNames.stream().sorted().reduce((a, b) -> a + "," + b).ifPresentOrElse(
                value -> friends.set(value),
                () -> friends.set("")
        );
        cachedValue = null;
    }
}
