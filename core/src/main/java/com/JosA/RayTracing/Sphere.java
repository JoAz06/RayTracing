package com.JosA.RayTracing;

//import com.badlogic.gdx.graphics.Color;

public class Sphere {
    public double radius;
    public double centerX;
    public double centerY;
    public double centerZ;

    public Sphere(double centerX, double centerY, double centerZ, double radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.radius = radius;
    }

    /*
    public boolean intersects(double[] vectorDirection){
        if((Math.pow((2*vectorDirection[0]*(Main.cameraPosX - centerX) + 2*vectorDirection[1]*(Main.cameraPosY - centerY) + 2*vectorDirection[2]*(Main.cameraPosZ - centerZ)), 2) - 4*(Math.pow(vectorDirection[0], 2) + Math.pow(vectorDirection[1], 2) + Math.pow(vectorDirection[2], 2))*(Math.pow((Main.cameraPosX - centerX), 2) + Math.pow((Main.cameraPosY - centerY), 2) + Math.pow((Main.cameraPosZ - centerZ), 2) - Math.pow(radius, 2))) >= 0){
            return true;
        }else{
            return false;
        }
    }*/
    
    public boolean intersects(double[] vectorDirection) {
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
        return false; // no intersection
    }

    // Solve quadratic for t
    double sqrtDisc = Math.sqrt(disc);
    double t1 = (-b - sqrtDisc) / (2*a);
    double t2 = (-b + sqrtDisc) / (2*a);

    // At least one positive t means the ray hits in front of the camera
    return (t1 >= 0 || t2 >= 0);
}
}
