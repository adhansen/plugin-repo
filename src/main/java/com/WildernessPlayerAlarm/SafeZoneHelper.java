package com.WildernessPlayerAlarm;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;

public class SafeZoneHelper
{
    @RequiredArgsConstructor
    private static class Edge
    {
        public final int x1;
        public final int y1;
        public final int x2;
        public final int y2;
    }

    private static final List<Edge> FEROX_ENCLAVE = new ArrayList<>();

    SafeZoneHelper()
    {
        FEROX_ENCLAVE.add(new Edge(3125, 3639, 3138, 3639));
        FEROX_ENCLAVE.add(new Edge(3138, 3639, 3138, 3647));
        FEROX_ENCLAVE.add(new Edge(3138, 3647, 3156, 3647));
        FEROX_ENCLAVE.add(new Edge(3156, 3647, 3156, 3636));
        FEROX_ENCLAVE.add(new Edge(3156, 3636, 3154, 3636));
        FEROX_ENCLAVE.add(new Edge(3154, 3636, 3154, 3626));
        FEROX_ENCLAVE.add(new Edge(3154, 3626, 3151, 3622));
        FEROX_ENCLAVE.add(new Edge(3151, 3622, 3144, 3620));
        FEROX_ENCLAVE.add(new Edge(3144, 3620, 3142, 3618));
        FEROX_ENCLAVE.add(new Edge(3142, 3618, 3138, 3618));
        FEROX_ENCLAVE.add(new Edge(3138, 3618, 3138, 3617));
        FEROX_ENCLAVE.add(new Edge(3138, 3617, 3125, 3617));
        FEROX_ENCLAVE.add(new Edge(3125, 3617, 3125, 3627));
        FEROX_ENCLAVE.add(new Edge(3125, 3627, 3123, 3627));
        FEROX_ENCLAVE.add(new Edge(3123, 3627, 3123, 3633));
        FEROX_ENCLAVE.add(new Edge(3123, 3633, 3125, 3633));
        FEROX_ENCLAVE.add(new Edge(3125, 3633, 3125, 3639));
    }

    // Counter-clockwise order of points 1, 2, 3
    private static boolean ccw(int x1, int y1, int x2, int y2, int x3, int y3)
    {
        return (y3 - y1) * (x2 - x1) > (y2 - y1) * (x3 - x1);
    }

    private static boolean HasIntersection(Edge lhs, Edge rhs)
    {
        return (ccw(lhs.x1, lhs.y1, rhs.x1, rhs.y1, rhs.x2, rhs.y2) != ccw(lhs.x2, lhs.y2, rhs.x1, rhs.y1, rhs.x2, rhs.y2))
                && (ccw(lhs.x1, lhs.y1, lhs.x2, lhs.y2, rhs.x1, rhs.y1) != ccw(lhs.x1, lhs.y1, lhs.x2, lhs.y2, rhs.x2, rhs.y2));
    }

    public boolean PointInsideFerox(WorldPoint test)
    {
        Edge testRay = new Edge(test.getX(), test.getY(), 0, 0);
        int intersections = 0;
        for (Edge i : FEROX_ENCLAVE)
        {
            if (HasIntersection(testRay, i))
            {
                intersections++;
            }
        }
        return intersections % 2 == 1;
    }

}


