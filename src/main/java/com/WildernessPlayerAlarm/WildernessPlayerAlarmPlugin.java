package com.WildernessPlayerAlarm;

import com.google.inject.Provides;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.Notifier;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;


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
	private KeyManager keyManager;

	@Inject
	private Notifier notifier;

	@Inject
	private SilencedState silencedState;

	private boolean overlayOn = false;
	private final Set<Integer> currentlyOffendingPlayers = Collections.checkedSet(new HashSet<>(), Integer.class);


	@Override
	protected void startUp() throws Exception {
		keyManager.registerKeyListener(new HotkeyListener(() -> config.silenceKey()) {
			@Override
			public void hotkeyPressed() {
				silencedState.silence(currentlyOffendingPlayers);
			}
		});
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick) {
		List<Player> players = client.getPlayers();
		currentlyOffendingPlayers.clear();
		Player self = client.getLocalPlayer();
		LocalPoint currentPosition = client.getLocalPlayer().getLocalLocation();
		WildernessCombatRange combatRange = WildernessCombatRange.getCurrentCombatRange(client);
		if (client.getVarbitValue(Varbits.IN_WILDERNESS) == 1)
		{
			for (Player player : players) {
				if (player.getId() != self.getId() && (player.getLocalLocation().distanceTo(currentPosition) / 128) <= config.alarmRadius())
				{
					if (config.ignoreClan() && player.isClanMember()){
						continue;
					}
					if (config.ignoreFriends() && player.isFriend()){
						continue;
					}
					if (config.ignorePlayersOutsideCombatRange() && !combatRange.isOtherPlayerInCombatRange(player)) {
						continue;
					}
					currentlyOffendingPlayers.add(player.getId());
				}

			}
		}

		if (currentlyOffendingPlayers.isEmpty() || silencedState.updateAndCheck(currentlyOffendingPlayers))
		{
			overlayOn = false;
			overlayManager.remove(overlay);
		} else if (!overlayOn)
		{
			if (config.desktopNotification()){
				notifier.notify("Player spotted!");
			}
			overlayOn = true;
			overlayManager.add(overlay);
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
