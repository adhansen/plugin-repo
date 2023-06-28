package com.WildernessPlayerAlarm;

import com.google.inject.Provides;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.Notifier;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;


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

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WildernessPlayerAlarmPlugin.class);

	@Subscribe
	public void onClientTick(ClientTick clientTick) {
		List<Player> players = client.getPlayers();
		boolean shouldAlarm = false;
		Player self = client.getLocalPlayer();
		LocalPoint currentPosition = client.getLocalPlayer().getLocalLocation();

		if (client.getVarbitValue(Varbits.IN_WILDERNESS) == 1)
		{
			for (Player player : players) {

				if (player.getId() != self.getId() && (player.getLocalLocation().distanceTo(currentPosition) / 128) <= config.alarmRadius())
				{
					shouldAlarm = true;
					if (config.ignoreClan() && player.isClanMember()){
						shouldAlarm = false;
					}
					if (config.ignoreFriends() && player.isFriend()){
						shouldAlarm = false;
					}
					if (config.ignoreIgnored() && client.getIgnoreContainer().findByName(player.getName()) != null) {
						shouldAlarm = false;
					}
				}

			}
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
