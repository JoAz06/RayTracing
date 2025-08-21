package com.JosA.RayTracing;


import java.nio.ByteBuffer;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
	public static int OriginWidth;
	public static int OriginHeight;
    private SpriteBatch batch;
    private Pixmap pixmap;
    FitViewport viewport;
    Texture pixmaptex;

    static long startTime;
    
    //Camera and Objects
    static double universeWidth = 1000;
    static double universeHieght = 1000;
    
    public static double cameraPosX = 0.0;
    public static double cameraPosY = 500.0;
    public static double cameraPosZ = 500.0;
    
    static double cameraAngXZDEG = 0.0;
    static double cameraAngXYDEG = 0.0;
    static double cameraAngXZ;
    static double cameraAngXY;
    
    static double cameraSpeedPosVertical = 200;
    static double cameraSpeedPosHorizonal = 200;
    
    static double cameraSpeedAngVerticalDEG = 0.1;
    static double cameraSpeedAngHorizontalDEG = 0.1;
    static double cameraSpeedAngVertical;
    static double cameraSpeedAngHorizontal;
    
    static double FOV = 80;
    static double dAng;

    static double deltaTime;

    public UniverseObjects[] universeObjects = new UniverseObjects[0];
    public Sphere sphere1;
    public Sphere sphere2;
    public Sphere sphere3;
    public Sphere sphere4;

    //Optimising ray tracing loop
    public double halfWidth;
    public double halfHeight;
    static double tempOptimizer;
    static double[] angleYCos;
    static double[] angleYSin;
    static double[] angleXCos;
    static double[] angleXSin;
    static double cosXZ;
    static double sinXZ;
    static double cosXY;
    static double sinXY;


    //Non changebal stuff
    public final static double rad90 = Math.toRadians(90);
    public final static double rad180 = Math.toRadians(180);
    public final static double rad270 = Math.toRadians(270);
    public final static double rad360 = Math.toRadians(360);

    public Main(int OriginWidth, int OriginHeight) {
    	Main.OriginWidth = OriginWidth;
    	Main.OriginHeight = OriginHeight;
    }

    @Override
    public void create() {
        cameraSpeedAngVertical = Math.toRadians(cameraSpeedAngVerticalDEG);
        cameraSpeedAngHorizontal = Math.toRadians(cameraSpeedAngHorizontalDEG);
        cameraAngXZ = Math.toRadians(cameraAngXZDEG);
        cameraAngXY = Math.toRadians(cameraAngXYDEG);
        dAng = Math.toRadians(FOV) / OriginWidth;

        halfWidth = OriginWidth/2.0;
        halfHeight = OriginHeight/2.0;

        batch = new SpriteBatch();
        viewport = new FitViewport(10,10);
        pixmap = new Pixmap( OriginWidth, OriginHeight, Format.RGBA8888 );
        pixmaptex = new Texture(pixmap);
        sphere1 = new Sphere(500,500,500,100,Color.RED);
        sphere2 = new Sphere(500,500,400,70,Color.BLUE);
        sphere3 = new Sphere(800,500,700,70,Color.GREEN);
        sphere4 = new Sphere(500,600,700,70,Color.YELLOW);
        universeObjects = new UniverseObjects[] {sphere1, sphere2, sphere3, sphere4};
        updateCameraAng();
    }

    @Override
    public void render() {
        startChrono();
    	Gdx.gl.glClearColor(0,0,0,1);
    	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    	ScreenUtils.clear(Color.BLACK);
    	viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        
        deltaTime = Gdx.graphics.getDeltaTime();

        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
        	UpdateCameraPos(0);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
        	UpdateCameraPos(rad270);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
        	UpdateCameraPos(rad180);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
        	UpdateCameraPos(rad90);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
        	cameraPosY += cameraSpeedPosVertical * deltaTime;
            if(cameraPosY > universeHieght) {
                cameraPosY = universeHieght;
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
        	cameraPosY -= cameraSpeedPosVertical * deltaTime;
            if(cameraPosY < 0) {
                cameraPosY = 0;
            }
        }
        if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        if(Gdx.input.isTouched()) {
            updateCameraAng();

        }
        
        //startChrono();
        //stopChrono();
        
        Color[][] pixelColors = new Color[OriginWidth][OriginHeight];

        //Mulithreading
        int numThreads = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[numThreads];
        
        for (int t = 0; t < numThreads; t++) {
            final int threadIndex = t;
            threads[t] = new Thread(() -> {
                double[] vectorDirection = new double[3];
                for (int j = threadIndex; j < OriginHeight; j += numThreads) {
                    for (int i = 0; i < OriginWidth; i++) {
                        vectorDirection[0] = angleYCos[j] * angleXCos[i]; // X
                        vectorDirection[1] = -angleYSin[j];                     // Y
                        vectorDirection[2] = -angleYCos[j] * angleXSin[i]; // Z

                // Apply vertical (XY) rotation
                double dirX = vectorDirection[0] * cosXY - vectorDirection[1] * sinXY;
                double dirY = vectorDirection[0] * sinXY + vectorDirection[1] * cosXY;
                vectorDirection[0] = dirX;
                vectorDirection[1] = dirY;

                // Apply horizontal (XZ) rotation
                double newX = vectorDirection[0] * cosXZ + vectorDirection[2] * sinXZ;
                double newZ = -vectorDirection[0] * sinXZ + vectorDirection[2] * cosXZ;
                vectorDirection[0] = newX;
                vectorDirection[2] = newZ;
                //End Chat Gpt

                double closestT = Double.MAX_VALUE;
                Object[] temperary;
                pixelColors[i][j] = Color.BLACK.cpy();
                for(UniverseObjects obj : universeObjects){
                    temperary = obj.intersects(vectorDirection);
                    if((double)temperary[0] != -1.0 && (double)temperary[0] < closestT) {
                        closestT = (double)temperary[0];
                        pixelColors[i][j] = ((Color)temperary[1]).cpy();
                    }
                }
                    }
                }
            });
            threads[t].start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        //startChrono();
        //stopChrono();
        
        ByteBuffer buffer = pixmap.getPixels();
        for (int j = 0; j < OriginHeight; j++) {
            for (int i = 0; i < OriginWidth; i++) {
                Color c = pixelColors[i][j];
                int r = (int)(c.r * 255);
                int g = (int)(c.g * 255);
                int b = (int)(c.b * 255);
                int a = (int)(c.a * 255);

                buffer.put((byte)r);
                buffer.put((byte)g);
                buffer.put((byte)b);
                buffer.put((byte)a);
            }
        }
        buffer.flip();
        pixmaptex.draw(pixmap, 0, 0);
        batch.draw(pixmaptex, 0, 0, 10, 10);
        batch.end();
        stopChrono();
    }
    
    @Override
    public void resize(int width, int height) {
        OriginWidth = width;
        OriginHeight = height;
        updateCameraAng();
    	viewport.update(width, height, true);
        dAng = Math.toRadians(FOV) / width;
        halfWidth = OriginWidth/2.0;
        halfHeight = OriginHeight/2.0;
        pixmap = new Pixmap( OriginWidth, OriginHeight, Format.RGBA8888 );
        pixmaptex = new Texture(pixmap);
        
    }

    @Override
    public void dispose() {
    	pixmap.dispose();
        pixmaptex.dispose();
        batch.dispose();
    }

    public static void UpdateCameraPos(double angOffset){
        cameraPosX += cameraSpeedPosHorizonal * Math.cos(cameraAngXZ+angOffset) * deltaTime;
        cameraPosZ -= cameraSpeedPosHorizonal * Math.sin(cameraAngXZ+angOffset) * deltaTime;
        if(cameraPosX < 0){
            cameraPosX = 0;
        }else if(cameraPosX > universeWidth) {
            cameraPosX = universeWidth;
        }
        if(cameraPosZ < 0){
            cameraPosZ = 0;
        }else if(cameraPosZ > universeHieght) { 
            cameraPosZ = universeHieght;
        }
    }

    public static void updateCameraAng(){
        cameraAngXZ += Gdx.input.getDeltaX() * cameraSpeedAngHorizontal;
        cameraAngXZ = cameraAngXZ % rad360;
        cameraAngXY -= Gdx.input.getDeltaY() * cameraSpeedAngVertical;
        if(cameraAngXY < -rad90) {
            cameraAngXY = -rad90;
        } else if(cameraAngXY > rad90) {
            cameraAngXY = rad90;
        }

        angleYCos = new double[OriginHeight];
        angleYSin = new double[OriginHeight];
        for(int j = 0; j < OriginHeight; j++){
            tempOptimizer = Math.toRadians(((j / (OriginHeight - 1.0)) * 2.0 - 1.0) * (FOV / 2.0));
            angleYCos[j] = Math.cos(tempOptimizer);
            angleYSin[j] = Math.sin(tempOptimizer);
        }
        angleXCos = new double[OriginWidth];
        angleXSin = new double[OriginWidth];
        for(int i = 0; i < OriginWidth; i++){
            tempOptimizer = Math.toRadians(((i / (OriginWidth - 1.0)) * 2.0 - 1.0) * (FOV / 2.0));
            angleXCos[i] = Math.cos(tempOptimizer);
            angleXSin[i] = Math.sin(tempOptimizer);
        }
        cosXZ = Math.cos(cameraAngXZ);
        sinXZ = Math.sin(cameraAngXZ);
        cosXY = Math.cos(cameraAngXY);
        sinXY = Math.sin(cameraAngXY);
    }

    public static void startChrono(){
        startTime = System.nanoTime();
    }
    public static void stopChrono(){
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        System.out.println("Elapsed time: " + elapsedTime / 1_000_000 + " ms");
    }
}
