package com.WildernessPlayerAlarm;

import com.google.inject.Provides;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
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

		if (client.getVarbitValue(Varbits.IN_WILDERNESS) == 1)
		{
			for (Player player : players) {

				if (player.getId() != self.getId() && (player.getLocalLocation().distanceTo(currentPosition) / 128) <= config.alarmRadius())
				{
					shouldAlarm = true;

					if (config.combatCheck() && !combatCheck(player)) {
						shouldAlarm = false;
					}
					if (config.ignoreClan() && player.isClanMember()){
						shouldAlarm = false;
					}
					if (config.ignoreFriends() && player.isFriend()){
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

	private boolean combatCheck(Player player)
	{
		String wildernessLevel = null;
		int maxCombat = 0;
		int minCombat = 0;

		Widget wildernessLevelWidget = client.getWidget(WidgetInfo.PVP_WILDERNESS_LEVEL);

		if (wildernessLevelWidget != null && !wildernessLevelWidget.isHidden())
		{
			wildernessLevel = wildernessLevelWidget.getText();
		}

		Pattern pattern = Pattern.compile("\\d+-\\d+");
		Matcher matcher = pattern.matcher(wildernessLevel);

		if (matcher.find())
		{
			String[] numbers = matcher.group(0).split("-");
			minCombat = Integer.parseInt(numbers[0]);
			maxCombat = Integer.parseInt(numbers[1]);
		}

		if (player.getCombatLevel() >= minCombat && player.getCombatLevel() <= maxCombat)
		{
			return true;
		}

		return false;
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
