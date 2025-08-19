package com.JosA.RayTracing;


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
    
    //Camera and Objects
    static double universeWidth = 1000;
    static double universeHieght = 1000;
    
    public static double cameraPosX = 0.0;
    public static double cameraPosY = 500.0;
    public static double cameraPosZ = 500.0;
    
    static double cameraAngXZDEG = 0.0;
    static double cameraAngXYDEG = 0.0;
    static double cameraAngXZ = Math.toRadians(cameraAngXZDEG);
    static double cameraAngXY = Math.toRadians(cameraAngXYDEG);
    
    static double cameraSpeedPosVertical = 100;
    static double cameraSpeedPosHorizonal = 100;
    
    static double cameraSpeedAngVerticalDEG = 0.1;
    static double cameraSpeedAngHorizontalDEG = 0.1;
    static double cameraSpeedAngVertical = Math.toRadians(cameraSpeedAngVerticalDEG);
    static double cameraSpeedAngHorizontal = Math.toRadians(cameraSpeedAngHorizontalDEG);
    
    static double FOV = 80;
    static double dAng = Math.toRadians(FOV) / OriginWidth;

    static double[] vectorDirection;

    static double deltaTime;
    
    public Main(int OriginWidth, int OriginHeight) {
    	Main.OriginWidth = OriginWidth;
    	Main.OriginHeight = OriginHeight;
    }

    public Sphere sphere1;
    public Sphere sphere2;
    
    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new FitViewport(10,10);
        pixmap = new Pixmap( OriginWidth, OriginHeight, Format.RGBA8888 );
        sphere1 = new Sphere(500,500,500,100);
        sphere2 = new Sphere(500,500,700,70);
        vectorDirection = new double[3];
    }

    @Override
    public void render() {
    	Gdx.gl.glClearColor(0,0,0,1);
    	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    	ScreenUtils.clear(Color.BLACK);
    	viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        
        deltaTime = Gdx.graphics.getDeltaTime();

        if(Gdx.input.isKeyPressed(Input.Keys.W)) {
        	UpdateCameraPos(Math.toRadians(0));
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
        	UpdateCameraPos(Math.toRadians(270));
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S)) {
        	UpdateCameraPos(Math.toRadians(180));
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
        	UpdateCameraPos(Math.toRadians(90));
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
            cameraAngXZ += Gdx.input.getDeltaX() * cameraSpeedAngHorizontal;
            cameraAngXZ = cameraAngXZ % Math.toRadians(360);
            cameraAngXY += Gdx.input.getDeltaY() * cameraSpeedAngVertical;
            if(cameraAngXY < Math.toRadians(-90)) {
                cameraAngXY = Math.toRadians(-90);
            } else if(cameraAngXY > Math.toRadians(90)) {
                cameraAngXY = Math.toRadians(90);
            }
        }

        long startTime = System.nanoTime();

        for(int j = 0 ; j < OriginHeight ; j++) {
	        	for(int i = 0 ; i < OriginWidth ; i++) {
	        		// Calculate pixel's angular offset from center
	        		double pixelOffsetH = (i - OriginWidth/2.0) * dAng;   // horizontal offset
	        		double pixelOffsetV = (j - OriginHeight/2.0) * dAng;  // vertical offset

	        		// Combine with camera rotation
	        		double totalAzimuth = cameraAngXZ + pixelOffsetH;
	        		double totalElevation = cameraAngXY + pixelOffsetV;

	        		// Proper 3D rotation
	        		vectorDirection[0] = Math.cos(totalElevation) * Math.cos(totalAzimuth);  // X
	        		vectorDirection[1] = -Math.sin(totalElevation);                           // Y  
	        		vectorDirection[2] = -Math.cos(totalElevation) * Math.sin(totalAzimuth);  // Z
	        		
	        		if(sphere1.intersects(vectorDirection) && sphere2.intersects(vectorDirection)){
                        pixmap.setColor(Color.PURPLE);
                    }else if(sphere2.intersects(vectorDirection)){
                        pixmap.setColor(Color.BLUE);
                    }else if(sphere1.intersects(vectorDirection)){
                        pixmap.setColor(Color.RED); 
                    }else{
                        pixmap.setColor(Color.BLACK);
                    }
                    pixmap.drawPixel(i, j);
                    
	        	}
	        }


            long endTime = System.nanoTime();
	        double elapsedMs = (endTime - startTime) / 1_000_000.0;
	        System.out.println("Time taken: " + elapsedMs + " ms");

        pixmaptex = new Texture(pixmap);
        batch.draw(pixmaptex, 0, 0, 10, 10);
        batch.end();
        pixmaptex.dispose();
    }
    
    @Override
    public void resize(int width, int height) {
    	viewport.update(width, height, true);
        dAng = Math.toRadians(FOV) / width;
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
}
