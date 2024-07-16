package com.WildernessPlayerAlarm;

import java.awt.*;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.components.LineComponent;

public class AlarmOverlay extends OverlayPanel
{
    private final WildernessPlayerAlarmConfig config;
    private final Client client;
    private final Color transparent = new Color(0, 0, 0, 0);

    @Inject
    private AlarmOverlay(WildernessPlayerAlarmConfig config, Client client)
    {
        this.config = config;
        this.client = client;
        setLayer(OverlayLayer.ABOVE_SCENE);
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
        FlashControl configuredSpeed = config.flashControl();
        panelComponent.setPreferredSize(new Dimension(client.getCanvasWidth(), client.getCanvasHeight()));

        switch (configuredSpeed)
        {
            case OFF:
                panelComponent.setBackgroundColor(transparent);
                break;
            case SOLID:
                panelComponent.setBackgroundColor(config.flashColor());
                break;
            default:
                if ((client.getGameCycle() % config.flashControl().getRate()) >= (config.flashControl().getRate() / 2))
                {
                    panelComponent.setBackgroundColor(config.flashColor());
                } else
                {
                    panelComponent.setBackgroundColor(transparent);
                }
                break;
        }

        return panelComponent.render(graphics);
    }
}
