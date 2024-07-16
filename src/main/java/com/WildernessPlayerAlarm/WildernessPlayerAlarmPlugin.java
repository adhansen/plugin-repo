package com.WildernessPlayerAlarm;

import com.google.inject.Provides;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Constants;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.Client;
import net.runelite.api.WorldType;
import net.runelite.api.Actor;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.ComponentID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
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

	private final HashMap<String, Integer> playerNameToTimeInRange = new HashMap<>();

	private final SafeZoneHelper zoneHelper = new SafeZoneHelper();

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

		List<Player> dangerousPlayers = getPlayersInRange()
				.stream()
				.filter(player->shouldPlayerTriggerAlarm(player, isInWilderness))
				.collect(Collectors.toList());

		// Keep track of how long players have been in range if timeout is enabled
		if (config.timeoutToIgnore() > 0)
		{
			updatePlayersInRange();
		}

		boolean shouldAlarm = (isInWilderness || isInDangerousPvpArea) && dangerousPlayers.size() > 0;
		if (shouldAlarm && !overlayOn)
		{
			if (config.desktopNotification())
			{
				notifier.notify("Player spotted!");
			}
			addOverlay();
		}

		if (!shouldAlarm)
		{
			removeOverlay();
		}
	}

	private List<Player> getPlayersInRange()
	{
		LocalPoint currentPosition = client.getLocalPlayer().getLocalLocation();
		return client.getPlayers()
				.stream()
				.filter(player -> (player.getLocalLocation().distanceTo(currentPosition) / 128) <= config.alarmRadius())
				.collect(Collectors.toList());
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

		// Don't trigger for players inside Ferox Enclave (short-circuit to only check from wildy)
		if (inWilderness && zoneHelper.PointInsideFerox(player.getWorldLocation()))
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

		return true;
	}

	private void updatePlayersInRange()
	{
		List<Player> playersInRange = getPlayersInRange();

		// Update players that are still in range
		for (Player player : playersInRange) {
			String playerName = player.getName();
			int timeInRange = playerNameToTimeInRange.containsKey(playerName)
					? playerNameToTimeInRange.get(playerName) + Constants.GAME_TICK_LENGTH
					: Constants.GAME_TICK_LENGTH;
			playerNameToTimeInRange.put(playerName, timeInRange);
		}

		// Remove players that are out of range
		List<String> playerNames = playersInRange
				.stream()
				.map(Actor::getName)
				.collect(Collectors.toList());
		List<String> playersToReset = playerNameToTimeInRange
				.keySet()
				.stream()
				.filter(playerName -> !playerNames.contains(playerName))
				.collect(Collectors.toList());
		for (String playerName : playersToReset) {
			playerNameToTimeInRange.remove(playerName);
		}
	}

	private boolean isInPvp()
	{
		boolean pvp = WorldType.isPvpWorld(client.getWorldType()) && (client.getVarbitValue(Varbits.PVP_SPEC_ORB) == 1);
		String widgetText = client.getWidget(ComponentID.PVP_WILDERNESS_LEVEL).getText();
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
	protected void shutDown() throws Exception
	{
		if (overlayOn)
		{
			removeOverlay();
		}
	}

	@Provides
	WildernessPlayerAlarmConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WildernessPlayerAlarmConfig.class);
	}
}
