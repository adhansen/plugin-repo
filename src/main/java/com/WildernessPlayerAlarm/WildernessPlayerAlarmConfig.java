package com.WildernessPlayerAlarm;

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
			keyName = "notification",
			name = "Notification",
			description = "Configurable notification when the alarm is triggered",
			position = 1
	)
	default Notification notification()
	{
		return new Notification().withEnabled(true);
	}

	@ConfigItem(
			keyName = "pvpWorldAlerts",
			name = "Pvp world alerts",
			description = "Will alert you anywhere when in PVP or DMM worlds",
			position = 2
	)
	default boolean pvpWorldAlerts() {
		return false;
	}

	@ConfigItem(
			keyName = "ignoreFriends",
			name = "Ignore friends",
			description = "Do not alarm for players on your friends list",
			position = 3
	)
	default boolean ignoreFriends()
	{
		return true;
	}

	@ConfigItem(
			keyName = "ignoreClan",
			name = "Ignore clan",
			description = "Do not alarm for players in your clan",
			position = 4
	)
	default boolean ignoreClan()
	{
		return true;
	}

	@ConfigItem(
			keyName = "ignoreFriendsChat",
			name = "Ignore friends chat",
			description = "Do not alarm for players in the same friends chat as you",
			position = 5
	)
	default boolean ignoreFriendsChat()
	{
		return false;
	}

	@ConfigItem(
			keyName = "ignoreIgnored",
			name = "Ignore 'ignore list'",
			description = "Do not alarm for players on your ignore list",
			position = 6
	)
	default boolean ignoreIgnored()
	{
		return false;
	}
}
