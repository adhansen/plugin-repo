package com.WildernessPlayerAlarm;

import java.awt.*;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;

public class AlarmOverlay extends OverlayPanel
{
    private final Client client;
    private final Color transparent = new Color(0, 0, 0, 0);

    private Color flashColor;
    private FlashControl flashControl;

    @Inject
    private AlarmOverlay(WildernessPlayerAlarmConfig config, Client client)
    {
        this.client = client;
        updateConfig(config);

        setPosition(OverlayPosition.DYNAMIC);
        //Alternatively use other OverlayLayers to e.g. not render on top of the inventory, chatbox, minimap etc
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    public void updateConfig(WildernessPlayerAlarmConfig config) {
        this.flashColor = config.flashColor();
        this.flashControl = config.flashControl();
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        switch (this.flashControl)
        {
            case OFF:
                graphics.setColor(transparent);
                break;
            case SOLID:
                graphics.setColor(flashColor);
                break;
            default:
                if ((client.getGameCycle() % flashControl.getRate()) >= (flashControl.getRate() / 2))
                {
                    graphics.setColor(flashColor);
                } else
                {
                    graphics.setColor(transparent);
                }
                break;
        }
        graphics.fillRect(0, 0, client.getCanvasWidth(), client.getCanvasHeight());

        return null;
    }
}
