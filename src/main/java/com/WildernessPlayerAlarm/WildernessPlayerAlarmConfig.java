package com.WildernessPlayerAlarm;

import java.awt.Color;
import net.runelite.client.config.*;

@ConfigGroup("WildernessPlayerAlarm")
public interface WildernessPlayerAlarmConfig extends Config
{
	@Range(
		max = 30,
		min = 0
	)
	@ConfigItem(
			keyName = "alarmRadius",
			name = "Alarm radius",
			description = "Distance for another player to trigger the alarm. WARNING: Players within range that are " +
			"not rendered will not trigger the alarm.",
			position = 0
	)
	default int alarmRadius()
	{
		return 15;
	}

	@ConfigItem(
			keyName = "desktopNotification",
			name = "Desktop notification",
			description = "Receive a desktop notification when the alarm triggers",
			position = 1
	)
	default boolean desktopNotification()
	{
		return false;
	}

	@ConfigItem(
			keyName = "ignoreFriends",
			name = "Ignore friends",
			description = "Do not alarm for players on your friends list",
			position = 2
	)
	default boolean ignoreFriends()
	{
		return true;
	}

	@ConfigItem(
			keyName = "ignoreClan",
			name = "Ignore clan",
			description = "Do not alarm for players in your clan",
			position = 3
	)
	default boolean ignoreClan()
	{
		return true;
	}

	@ConfigItem(
			keyName = "ignorePlayersOutsideCombatRange",
			name = "Ignore players outside combat range",
			description = "Do not alarm for players who cannot attack you at the current wilderness level.",
			position = 4
	)
	default boolean ignorePlayersOutsideCombatRange()
	{
		return false;
	}

	@ConfigItem(
			keyName = "enableFlash",
			name = "Enable flash",
			description = "Enables screen flashing when a player is in range.",
			position = 5
	)
	default boolean enableFlash()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			keyName = "flashColor",
			name = "Flash color",
			description = "Sets the color of the alarm flashes",
			position = 6
	)
	default Color flashColor()
	{
		return new Color(255, 255, 0, 70);
	}

}
