package com.JosA.RayTracing;

import com.badlogic.gdx.graphics.Color;

public abstract class UniverseObjects {
    public Color color;
    public abstract double intersects(double[] vectorDirection);
}
