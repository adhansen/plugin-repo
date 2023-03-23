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
			keyName = "ignoreFriends",
			name = "Ignore friends",
			description = "Do not alarm for players on your friends list",
			position = 1
	)
	default boolean ignoreFriends()
	{
		return true;
	}

	@ConfigItem(
			keyName = "ignoreClan",
			name = "Ignore clan",
			description = "Do not alarm for players in your clan",
			position = 2
	)
	default boolean ignoreClan()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
			keyName = "flashColor",
			name = "Flash color",
			description = "Sets the color of the alarm flashes",
			position = 3
	)
	default Color flashColor()
	{
		return new Color(255, 255, 0, 70);
	}

	@ConfigItem(
			keyName = "toggleNotification",
			name = "Desktop Notification",
			description = "Receive a desktop notification when the alarm triggers",
			position = 4
	)
	default boolean toggleNotification()
	{
		return false;
	}


}
