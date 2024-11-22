package com.WildernessPlayerAlarm;

import java.awt.*;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;

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
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        FlashControl configuredSpeed = config.flashControl();

        switch (configuredSpeed)
        {
            case OFF:
                graphics.setColor(transparent);
                break;
            case SOLID:
                graphics.setColor(config.flashColor());
                break;
            default:
                if ((client.getGameCycle() % config.flashControl().getRate()) >= (config.flashControl().getRate() / 2))
                {
                    graphics.setColor(config.flashColor());
                } else
                {
                    graphics.setColor(transparent);
                }
                break;
        }
        //Fill the rectangle using the client width and height
        graphics.fillRect(0, 0, client.getCanvasWidth(), client.getCanvasHeight());
        return client.getCanvas().getSize();
    }
}
