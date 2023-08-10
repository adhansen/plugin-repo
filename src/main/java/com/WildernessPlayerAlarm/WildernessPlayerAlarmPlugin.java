package com.WildernessPlayerAlarm;

import com.google.inject.Provides;
import java.util.List;
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

	@Subscribe
	public void onClientTick(ClientTick clientTick) {
		List<Player> players = client.getPlayers();
		boolean shouldAlarm = false;
		Player self = client.getLocalPlayer();
		LocalPoint currentPosition = client.getLocalPlayer().getLocalLocation();

		if (client.getVarbitValue(Varbits.IN_WILDERNESS) == 1 || (config.pvpWorldAlerts() && isInPvp()))
		{
			boolean foundDangerousPlayer = false;
			for (Player player : players) {

				if (player.getId() != self.getId() && (player.getLocalLocation().distanceTo(currentPosition) / 128) <= config.alarmRadius())
				{
					if (config.ignoreClan() && player.isClanMember()){
						continue;
					}
					if (config.ignoreFriends() && player.isFriend()){
						continue;
					}
					foundDangerousPlayer = true;
				}

			}
			shouldAlarm = foundDangerousPlayer;
		}

		if (shouldAlarm && !overlayOn)
		{
			if (config.desktopNotification()){
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
