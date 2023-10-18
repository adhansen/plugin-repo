package com.WildernessPlayerAlarm;

import com.google.inject.Provides;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.WorldType;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
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

	@Subscribe
	public void onClientTick(ClientTick clientTick) {
		List<Player> dangerousPlayers = getPlayersInRange()
				.stream()
				.filter(this::shouldPlayerTriggerAlarm)
				.collect(Collectors.toList());

		boolean isInWilderness = client.getVarbitValue(Varbits.IN_WILDERNESS) == 1;
		boolean isInDangerousPvpArea = config.pvpWorldAlerts() && isInPvp();
		boolean shouldAlarm = (isInWilderness || isInDangerousPvpArea) && dangerousPlayers.size() > 0;
		if (shouldAlarm && !overlayOn)
		{
			if (config.desktopNotification())
			{
				notifier.notify("Player spotted!");
			}
			overlayOn = true;
			overlayManager.add(overlay);
		}

		if (!shouldAlarm)
		{
			overlayOn = false;
			overlayManager.remove(overlay);
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
		if (config.ignoreClan() && player.isClanMember()){
			return false;
		}

		// Don't trigger for friends if option is selected
		if (config.ignoreFriends() && player.isFriend()){
			return false;
		}

		// Don't trigger for friends if option is selected
		if (config.ignoreFriendsChat() && player.isFriendsChatMember()){
			return false;
		}

		return true;
	}

	private boolean isInPvp()
	{
		return WorldType.isPvpWorld(client.getWorldType()) && client.getVarbitValue(Varbits.PVP_SPEC_ORB) == 1;
	}

	@Override
	protected void shutDown() throws Exception
	{
		if (overlayOn)
		{
			overlayOn = false;
			overlayManager.remove(overlay);
		}
	}

	@Provides
	WildernessPlayerAlarmConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WildernessPlayerAlarmConfig.class);
	}
}
