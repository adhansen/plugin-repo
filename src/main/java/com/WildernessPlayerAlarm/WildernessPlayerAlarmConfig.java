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
			keyName = "customizableNotification",
			name = "Customizable Notification",
			description = "Customizable RuneLite notification (it is recommended to not doubly-enable flashing)",
			position = 1
	)
	default Notification customizableNotification()
	{
		return new Notification();
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

	@ConfigItem(
			keyName = "timeoutToIgnore",
			name = "Timeout",
			description = "Ignores players after they've been present for the specified time (in seconds)." +
					" A value of 0 means players won't be ignored regardless of how long they are present.",
			position = 7
	)
	default int timeoutToIgnore()
	{
		return 0;
	}

	@Alpha
	@ConfigItem(
			keyName = "flashColor",
			name = "Flash color",
			description = "Sets the color of the alarm flashes",
			position = 8
	)
	default Color flashColor()
	{
		return new Color(255, 255, 0, 70);
	}

	@ConfigItem(
			keyName = "flashSpeed",
			name = "Flash speed",
			description = "Control the cadence at which the screen will flash with the chosen color",
			position = 9
	)
	default FlashSpeed flashSpeed() { return FlashSpeed.NORMAL; }

	@ConfigItem(
			keyName = "flashLayer",
			name = "Flash layer",
			description = "Advanced: control the layer that the flash renders on",
			position = 10
	)
	default FlashLayer flashLayer() { return FlashLayer.ABOVE_SCENE; }

	@ConfigItem(
			keyName = "customIgnores",
			name = "Custom list of players to ignore:",
			description = "Comma-separated list of players that shouldn't trigger the alarm (case-insensitive)",
			position = 11
	)
	default String customIgnoresList()
	{
		return "";
	}
}
