package com.WildernessPlayerAlarm;

import com.google.inject.Provides;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.Client;
import net.runelite.api.WorldType;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.Notifier;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


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
	private Notifier notifier;

	private boolean notified = false;

	private final HashMap<String, Integer> playerNameToTimeInRange = new HashMap<>();

	@Subscribe
	public void onGameTick(GameTick event) {
		boolean isInWilderness = client.getVarbitValue(Varbits.IN_WILDERNESS) == 1;
		boolean isInDangerousPvpArea = config.pvpWorldAlerts() && isInPvp();
		if (!isInWilderness && !isInDangerousPvpArea)
		{
			return;
		}

		List<Player> dangerousPlayers = getPlayersInRange()
				.stream()
				.filter(this::shouldPlayerTriggerAlarm)
				.collect(Collectors.toList());
		boolean shouldAlarm = (isInWilderness || isInDangerousPvpArea) && dangerousPlayers.size() > 0;
		if (shouldAlarm && !notified)
		{
			notifier.notify(config.notification(), "Player spotted!");
			notified = true;
		}

		// Reset for next notification
		if (!shouldAlarm)
		{
			notified = false;
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

	private boolean shouldPlayerTriggerAlarm(Player player)
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

		return true;
	}

	private boolean isInPvp()
	{
		return WorldType.isPvpWorld(client.getWorldType()) && client.getVarbitValue(Varbits.PVP_SPEC_ORB) == 1;
	}

	@Provides
	WildernessPlayerAlarmConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WildernessPlayerAlarmConfig.class);
	}
}
