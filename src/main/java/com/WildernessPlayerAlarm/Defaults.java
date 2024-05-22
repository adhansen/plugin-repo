package com.WildernessPlayerAlarm;

import net.runelite.client.config.FlashNotification;
import net.runelite.client.config.Notification;

public class Defaults
{
	public static Notification DefaultNotification = Notification.ON
		.withSendWhenFocused(true)
		.withFlash(FlashNotification.FLASH_UNTIL_CANCELLED);
}
