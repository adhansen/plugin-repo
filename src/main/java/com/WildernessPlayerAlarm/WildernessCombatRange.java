package com.WildernessPlayerAlarm;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.WorldType;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WildernessCombatRange {
    private static final Pattern WILDERNESS_LEVEL_PATTERN = Pattern.compile("^Level: (\\d+).*");

    private final boolean isPlayerInWilderness;
    private final int wildernessLevel;
    private final int playerLevel;

    private WildernessCombatRange() {
        this.isPlayerInWilderness = false;
        this.wildernessLevel = 0;
        this.playerLevel = 0;
    }

    private WildernessCombatRange(int wildernessLevel, int playerLevel) {
        this.isPlayerInWilderness = true;
        this.wildernessLevel = wildernessLevel;
        this.playerLevel = playerLevel;
    }

    public boolean isOtherPlayerInCombatRange(Player otherPlayer) {
        if (!isPlayerInWilderness) {
            return false;
        }
        int otherPlayerCombatLevel = otherPlayer.getCombatLevel();
        return otherPlayerCombatLevel >= (playerLevel - wildernessLevel)
                && otherPlayerCombatLevel <= (playerLevel + wildernessLevel);
    }

    /**
     * Parses the attack range from the wilderness level widget. This feels fragile, but appears to be the de-facto
     * way of getting this information based on the official CombatLevel plugin.
     */
    public static WildernessCombatRange getCurrentCombatRange(Client client) {
        final Widget wildernessLevelWidget = client.getWidget(WidgetInfo.PVP_WILDERNESS_LEVEL);
        if (wildernessLevelWidget == null)
        {
            return new WildernessCombatRange();
        }

        final String wildernessLevelText = wildernessLevelWidget.getText();
        final Matcher m = WILDERNESS_LEVEL_PATTERN.matcher(wildernessLevelText);
        if (!m.matches() || WorldType.isPvpWorld(client.getWorldType()))
        {
            return new WildernessCombatRange();
        }

        final int wildernessLevel = Integer.parseInt(m.group(1));
        final int combatLevel = client.getLocalPlayer().getCombatLevel();
        return new WildernessCombatRange(wildernessLevel, combatLevel);
    }
}
