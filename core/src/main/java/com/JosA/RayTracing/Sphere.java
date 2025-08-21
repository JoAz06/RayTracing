package com.JosA.RayTracing;

import com.badlogic.gdx.graphics.Color;

//import com.badlogic.gdx.graphics.Color;

public class Sphere extends UniverseObjects {
    public double centerX;
    public double centerY;
    public double centerZ;
    public double radius;

    public Sphere(double centerX, double centerY, double centerZ, double radius, Color color) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
        this.color = new Color(color);
    }
    
    public Object[] intersects(double[] vectorDirection) {
        Object[] result = new Object[2];
        result[0] = (double)-1.0;
        result[1] = color.cpy();

        // Ray origin = camera position
        double ox = Main.cameraPosX;
        double oy = Main.cameraPosY;
        double oz = Main.cameraPosZ;

        // Ray direction
        double dx = vectorDirection[0];
        double dy = vectorDirection[1];
        double dz = vectorDirection[2];

        // Vector from sphere center to ray origin
        double cx = ox - centerX;
        double cy = oy - centerY;
        double cz = oz - centerZ;

        // Quadratic coefficients
        double a = dx*dx + dy*dy + dz*dz;
        double b = 2 * (dx*cx + dy*cy + dz*cz);
        double c = cx*cx + cy*cy + cz*cz - radius*radius;

        // Discriminant
        double disc = b*b - 4*a*c;
        if (disc < 0) {
            result[0] = -1.0;
            return result; // No intersection
        }

        // Solve quadratic for t
        double sqrtDisc = Math.sqrt(disc);
        double t1 = (-b - sqrtDisc) / (2*a);
        double t2 = (-b + sqrtDisc) / (2*a);

        // At least one positive t means the ray hits in front of the camera
        if (t1 > 0 && t2 > 0) {
            result[0] = Math.min(t1, t2);
            //result[1] = ((Color) result[1]).mul((float)(distance3D(ox + t1 * dx, oy + t1 * dy, oz + t1 * dz, ox + t2 * dx, oy + t2 * dy, oz + t2 * dz)/(radius)*0.5));
        }else if (t1 > 0) {
            result[0] = t1;
        }else if (t2 > 0) {
            result[0] = t2;
        }
        return result;
    }

    public static double distance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
    double dx = x2 - x1;
    double dy = y2 - y1;
    double dz = z2 - z1;
    return Math.sqrt(dx*dx + dy*dy + dz*dz);
}
}
