package com.WildernessPlayerAlarm;

import java.awt.*;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;


public class AlarmOverlay extends OverlayPanel
{
    private final WildernessPlayerAlarmConfig config;
    private final Client client;

    @Inject
    private AlarmOverlay(WildernessPlayerAlarmConfig config, Client client)
    {
        this.config = config;
        this.client = client;

        panelComponent.getChildren().clear();
        panelComponent.setPreferredSize(new Dimension(client.getCanvasWidth(), client.getCanvasHeight()));
        for(int i = 0; i < 100; ++i)
        {
            panelComponent.getChildren().add((LineComponent.builder())
                    .left(" ")
                    .build());
        }
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (config.enableFlash() && client.getGameCycle() % 20 >= 10)
        {
            panelComponent.setBackgroundColor(config.flashColor());
        } else
        {
            panelComponent.setBackgroundColor(new Color(0, 0, 0, 0));
        }
        return panelComponent.render(graphics);
    }
}