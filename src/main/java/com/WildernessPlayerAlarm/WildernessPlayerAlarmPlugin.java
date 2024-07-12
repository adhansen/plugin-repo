package com.WildernessPlayerAlarm;

import com.google.inject.Provides;

import java.util.ArrayList;
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
import net.runelite.api.widgets.ComponentID;
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

	private List<Player> previousTickPlayersInRange = new ArrayList<Player>();

	private final SafeZoneHelper zoneHelper = new SafeZoneHelper();

	@Subscribe
	public void onGameTick(GameTick event) {
		boolean isInWilderness = client.getVarbitValue(Varbits.IN_WILDERNESS) == 1;
		boolean isInDangerousPvpArea = config.pvpWorldAlerts() && isInPvp();
		if (!isInWilderness && !isInDangerousPvpArea)
		{
			notified = false;
			return;
		}

		List<Player> dangerousPlayers = getPlayersInRange()
				.stream()
				.filter(player->shouldPlayerTriggerAlarm(player, isInWilderness))
				.collect(Collectors.toList());
		boolean shouldAlarm = (isInWilderness || isInDangerousPvpArea) && dangerousPlayers.size() > 0;
		notified = previousTickPlayersInRange.containsAll(dangerousPlayers);

		if (shouldAlarm && !notified)
		{
			notifier.notify(config.notification(), "Player spotted!");
		}
		previousTickPlayersInRange = dangerousPlayers;
		notified = shouldAlarm;
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

		return true;
	}

	private boolean isInPvp()
	{
		boolean pvp = WorldType.isPvpWorld(client.getWorldType()) && (client.getVarbitValue(Varbits.PVP_SPEC_ORB) == 1);
		String widgetText = client.getWidget(ComponentID.PVP_WILDERNESS_LEVEL).getText();
		pvp &= !widgetText.startsWith("Protection");
		pvp &= !widgetText.startsWith("Guarded");
		return pvp;
	}

	@Provides
	WildernessPlayerAlarmConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WildernessPlayerAlarmConfig.class);
	}
}
