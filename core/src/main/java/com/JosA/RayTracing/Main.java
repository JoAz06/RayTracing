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
    static double cameraAngXZ;
    static double cameraAngXY;
    
    static double cameraSpeedPosVertical = 100;
    static double cameraSpeedPosHorizonal = 100;
    
    static double cameraSpeedAngVerticalDEG = 0.1;
    static double cameraSpeedAngHorizontalDEG = 0.1;
    static double cameraSpeedAngVertical;
    static double cameraSpeedAngHorizontal;
    
    static double FOV = 80;
    static double dAng;

    static double[] vectorDirection;

    static double deltaTime;
    
    public Main(int OriginWidth, int OriginHeight) {
    	Main.OriginWidth = OriginWidth;
    	Main.OriginHeight = OriginHeight;
    }

    public UniverseObjects[] universeObjects = new UniverseObjects[0];
    public Sphere sphere1;
    public Sphere sphere2;

    //Optimising ray tracing loop
    public double halfWidth;
    public double halfHeight;
    double tempOptimizer;
    double totalAzimuthCos[];
    double totalAzimuthSin[];
    double totalElevationCos[];
    double totalElevationSin[];

    //Non changebal stuff
    public final double rad90 = Math.toRadians(90);
    public final double rad180 = Math.toRadians(180);
    public final double rad270 = Math.toRadians(270);
    public final double rad360 = Math.toRadians(360);

    
    @Override
    public void create() {
        cameraSpeedAngVertical = Math.toRadians(cameraSpeedAngVerticalDEG);
        cameraSpeedAngHorizontal = Math.toRadians(cameraSpeedAngHorizontalDEG);
        cameraAngXZ = Math.toRadians(cameraAngXZDEG);
        cameraAngXY = Math.toRadians(cameraAngXYDEG);
        dAng = Math.toRadians(FOV) / OriginWidth;

        halfWidth = OriginWidth/2.0;
        halfHeight = OriginHeight/2.0;
        totalAzimuthCos = new double[OriginWidth];
        totalAzimuthSin = new double[OriginWidth];
        totalElevationCos = new double[OriginHeight];
        totalElevationSin = new double[OriginHeight];


        batch = new SpriteBatch();
        viewport = new FitViewport(10,10);
        pixmap = new Pixmap( OriginWidth, OriginHeight, Format.RGBA8888 );
        pixmaptex = new Texture(pixmap);
        sphere1 = new Sphere(500,500,500,100,Color.RED);
        sphere2 = new Sphere(500,500,700,70,Color.BLUE);
        universeObjects = new UniverseObjects[] {sphere1, sphere2};
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
            cameraAngXZ += Gdx.input.getDeltaX() * cameraSpeedAngHorizontal;
            cameraAngXZ = cameraAngXZ % rad360;
            cameraAngXY += Gdx.input.getDeltaY() * cameraSpeedAngVertical;
            if(cameraAngXY < -rad90) {
                cameraAngXY = -rad90;
            } else if(cameraAngXY > rad90) {
                cameraAngXY = rad90;
            }
        }

        long startTime = System.nanoTime();

        double closestT = Double.MAX_VALUE;
        Color closestColor = Color.BLACK;
        double temperaryT;

        // Optimizing the ray tracing loop
        
        for(int i = 0 ; i < OriginWidth ; i++){
            tempOptimizer = cameraAngXZ + (i - halfWidth) * dAng;
            totalAzimuthCos[i] = Math.cos(tempOptimizer);
            totalAzimuthSin[i] = Math.sin(tempOptimizer);
        }
        for(int j = 0 ; j < OriginHeight ; j++){
            tempOptimizer = cameraAngXY + (j - halfHeight) * dAng;
            totalElevationCos[j] = Math.cos(tempOptimizer);
            totalElevationSin[j] = Math.sin(tempOptimizer);
        }


        for(int j = 0 ; j < OriginHeight ; j++) {
                
                vectorDirection[1] = -totalElevationSin[j];  // Y  
                
	        	for(int i = 0 ; i < OriginWidth ; i++) {
                    closestT = Double.MAX_VALUE;
                    closestColor = Color.BLACK.cpy();

	        		// Proper 3D rotation
	        		vectorDirection[0] = totalElevationCos[j] * totalAzimuthCos[i];  // X
	        		vectorDirection[2] = -totalElevationCos[j] * totalAzimuthSin[i];  // Z
	        		
                    for(UniverseObjects obj : universeObjects){
                        temperaryT = obj.intersects(vectorDirection);
                        if(temperaryT != -1 && temperaryT < closestT) {
                            closestT = temperaryT;
                            closestColor = obj.color.cpy();
                        }
                    }
                    
                    pixmap.setColor(closestColor);
                    pixmap.drawPixel(i, j);
	        	}
	        }

        long endTime = System.nanoTime();
        double elapsedMs = (endTime - startTime) / 1_000_000.0;
        System.out.println("Time taken: " + elapsedMs + " ms");
            
        pixmaptex.draw(pixmap, 0, 0);
        batch.draw(pixmaptex, 0, 0, 10, 10);
        batch.end();
    }
    
    @Override
    public void resize(int width, int height) {
    	viewport.update(width, height, true);
        dAng = Math.toRadians(FOV) / width;
        halfWidth = OriginWidth/2.0;
        halfHeight = OriginHeight/2.0;
        totalAzimuthCos = new double[width];
        totalAzimuthSin = new double[width];
        totalElevationCos = new double[height];
        totalElevationSin = new double[height];
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
