package com.WildernessPlayerAlarm;

import com.google.common.base.Splitter;
import com.google.inject.Provides;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Constants;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.Client;
import net.runelite.api.WorldType;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.Notifier;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Wilderness Player Alarm"
)
public class WildernessPlayerAlarmPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private WildernessPlayerAlarmConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AlarmOverlay overlay;

	@Inject
	private Notifier notifier;

	private boolean overlayOn = false;

	private HashSet<String> customIgnores = new HashSet<>();

	private Splitter CONFIG_SPLITTER = Splitter
			.onPattern("([,\n])")
			.omitEmptyStrings()
			.trimResults();

	private final HashMap<String, Integer> playerNameToTimeInRange = new HashMap<>();

	private final SafeZoneHelper zoneHelper = new SafeZoneHelper();

	@Subscribe
	public void onPlayerDespawned(PlayerDespawned event)
	{
		playerNameToTimeInRange.remove(event.getPlayer().getName());
	}

	@Subscribe
	public void onGameTick(GameTick event) {
		boolean isInWilderness = client.getVarbitValue(Varbits.IN_WILDERNESS) == 1;
		boolean isInDangerousPvpArea = config.pvpWorldAlerts() && isInPvp();
		if (!isInWilderness && !isInDangerousPvpArea)
		{
			if (overlayOn)
			{
				removeOverlay();
			}
			return;
		}

		boolean dangerousPlayers = getPlayersInRange()
				.anyMatch(player -> shouldPlayerTriggerAlarm(player, isInWilderness));

		// Keep track of how long players have been in range if timeout is enabled
		if (config.timeoutToIgnore() > 0)
		{
			updatePlayersInRange();
		}

		boolean shouldAlarm = (isInWilderness || isInDangerousPvpArea) && dangerousPlayers;
		if (shouldAlarm && !overlayOn)
		{
			if (config.customizableNotification().isEnabled())
			{
				notifier.notify(config.customizableNotification(), "Player spotted!");
			}
			addOverlay();
		}

		if (!shouldAlarm)
		{
			removeOverlay();
		}
	}

	private Stream<? extends Player> getPlayersInRange()
	{
		LocalPoint currentPosition = client.getLocalPlayer().getLocalLocation();
		int alarmRadius = config.alarmRadius();
		return client.getTopLevelWorldView().players()
				.stream()
				.filter(player -> (player.getLocalLocation().distanceTo(currentPosition) / 128) <= alarmRadius);
	}

	private boolean shouldPlayerTriggerAlarm(Player player, boolean inWilderness)
	{
		// Don't trigger for yourself
		if (player.getId() == client.getLocalPlayer().getId())
		{
			return false;
		}

		// Don't trigger for clan members if option is selected
		if (config.ignoreClan() && player.isClanMember())
		{
			return false;
		}

		// Don't trigger for friends if option is selected
		if (config.ignoreFriends() && player.isFriend())
		{
			return false;
		}

		// Don't trigger for friends if option is selected
		if (config.ignoreFriendsChat() && player.isFriendsChatMember())
		{
			return false;
		}

		// Don't trigger for ignored players if option is selected
		if (config.ignoreIgnored() && client.getIgnoreContainer().findByName(player.getName()) != null)
		{
			return false;
		}

		// Don't trigger for players in the custom ignore list
		if (customIgnores.contains(player.getName().toLowerCase()))
		{
			return false;
		}

		// Ignore players that have been on screen longer than the timeout
		if (config.timeoutToIgnore() > 0)
		{
			int timePlayerIsOnScreen = playerNameToTimeInRange.getOrDefault(player.getName(), 0);
			if (timePlayerIsOnScreen > config.timeoutToIgnore() * 1000)
			{
				return false;
			}
		}

		// Don't trigger for players inside Ferox Enclave (short-circuit to only check from wildy)
		if (inWilderness && zoneHelper.PointInsideFerox(player.getWorldLocation()))
		{
			return false;
		}

		return true;
	}

	private void updatePlayersInRange()
	{
		// Update players that are still in range
		Collection<String> playerNames = new HashSet<>();
		getPlayersInRange().forEach(player -> {
			String playerName = player.getName();
			playerNames.add(playerName);
			playerNameToTimeInRange.merge(playerName, Constants.GAME_TICK_LENGTH, Integer::sum);
		});

		// Remove players that are out of range
		playerNameToTimeInRange.keySet().removeIf(name -> !playerNames.contains(name));
	}

	private void resetCustomIgnores()
	{
		customIgnores.clear();
		customIgnores.addAll(CONFIG_SPLITTER.splitToList(config.customIgnoresList().toLowerCase()));
	}

	private boolean isInPvp()
	{
		boolean pvp = WorldType.isPvpWorld(client.getWorldType()) && (client.getVarbitValue(Varbits.PVP_SPEC_ORB) == 1);
		Widget widget = client.getWidget(ComponentID.PVP_WILDERNESS_LEVEL);
		if (widget == null)
		{
			return pvp;
		}
		String widgetText = widget.getText();
		pvp &= !widgetText.startsWith("Protection");
		pvp &= !widgetText.startsWith("Guarded");
		return pvp;
	}

	private void addOverlay()
	{
		overlayOn = true;
		overlayManager.add(overlay);
	}

	private void removeOverlay()
	{
		overlayOn = false;
		overlayManager.remove(overlay);
	}

	@Override
	protected void startUp()
	{
		overlay.setLayer(config.flashLayer().getLayer());
		resetCustomIgnores();
	}

	@Override
	protected void shutDown() throws Exception
	{
		if (overlayOn)
		{
			removeOverlay();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("WildernessPlayerAlarm"))
		{
			if ("timeoutToIgnore".equals(event.getKey()) && config.timeoutToIgnore() <= 0)
			{
				playerNameToTimeInRange.clear();
			}

			overlay.setLayer(config.flashLayer().getLayer());
			if(overlayOn)
			{
				removeOverlay();
				addOverlay();
			}
			resetCustomIgnores();
		}
	}

	@Provides
	WildernessPlayerAlarmConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WildernessPlayerAlarmConfig.class);
	}
}
