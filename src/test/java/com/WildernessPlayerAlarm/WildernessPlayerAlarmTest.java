package com.WildernessPlayerAlarm;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class WildernessPlayerAlarmTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(WildernessPlayerAlarmPlugin.class);
		RuneLite.main(args);
	}
}