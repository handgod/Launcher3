package com.android.soma.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.soma.R;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.RotateAroundAnimation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.cameras.ArcballCamera;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.cameras.ChaseCamera;
import org.rajawali3d.cameras.OrthographicCamera;
import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.plugins.FogMaterialPlugin;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.AlphaMapTexture;
import org.rajawali3d.materials.textures.NormalMapTexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.MathUtil;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.terrain.SquareTerrain;
import org.rajawali3d.terrain.TerrainGenerator;
import org.rajawali3d.util.debugvisualizer.DebugCamera;
import org.rajawali3d.util.debugvisualizer.DebugLight;
import org.rajawali3d.util.debugvisualizer.DebugVisualizer;
import org.rajawali3d.util.debugvisualizer.GridFloor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2016/1/27.
 */
public class WallpaperRenderer extends RajawaliRenderer implements StreamingTexture.ISurfaceListener{

    private AlphaMapTexture mTimeTexture;
    private Bitmap mTimeBitmap;
    private Canvas mTimeCanvas;
    private Paint mTextPaint;
    private SimpleDateFormat mDateFormat;
   // private int mFrameCount;
 //   private boolean mShouldUpdateTexture;
    private IRajawaliSurface mRajawaliSurface;
    private View mLayout;

//ViewTextureRenderer
    private int mFrameCount;
    private Surface mSurface;
    private StreamingTexture mStreamingTexture;
    private volatile boolean mShouldUpdateTexture;
    private final float[] mMatrix = new float[16];


    public WallpaperRenderer(Context context) {
        super(context);
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
    //    showLoader();
        super.onRenderSurfaceCreated(config, gl, width, height);
     //   hideLoader();
    }

    @Override
    protected void initScene() {
//      initScene_Orthographic();
     // initScene_Sphere();
    //  initScene_Cube();
//      initScene_ArcballCamera();
//      initScene_CanvasTexttoMaterial();
//      initScene_Skybox();
//        initScene_Terrain();
//        initScene_DebugVisualizer();
//        initScene_ViewTextureRenderer();
//        initScene_Catmul_Rom();
    }
    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);
       // onRender_CanvasTexttoMaterial(ellapsedRealtime, deltaTime);
//        onRender_Skybox(ellapsedRealtime, deltaTime);
      //  onRender_DebugVisualizer(ellapsedRealtime, deltaTime);
//        onRender_ViewTextureRenderer(ellapsedRealtime, deltaTime);

    }


    private Stack<Vector3> createWhirl(int numSides, float scaleFactor,
                                       float centerX, float centerY, float rotAngle) {
        Stack<Vector3> points = new Stack<Vector3>();
        Vector3[] sidePoints = new Vector3[numSides];
        float rotAngleSin = (float) Math.sin(rotAngle);
        float rotAngleCos = (float) Math.cos(rotAngle);
        float a = (float) Math.PI * (1f - 2f / (float) numSides);
        float c = (float) Math.sin(a)
                / (rotAngleSin + (float) Math.sin(a + rotAngle));

        for (int k = 0; k < numSides; k++) {
            float t = (2f * (float) k + 1f) * (float) Math.PI
                    / (float) numSides;
            sidePoints[k] = new Vector3(Math.sin(t), Math.cos(t), 0);
        }

        for (int n = 0; n < 64; n++) {
            for (int l = 0; l < numSides; l++) {
                Vector3 p = sidePoints[l];
                points.add(new Vector3((p.x * scaleFactor) + centerX,
                        (p.y * scaleFactor) + centerY, 8 - (n * .25f)));
            }
            for (int m = 0; m < numSides; m++) {
                Vector3 p = sidePoints[m];
                double z = p.x;
                p.x = (p.x * rotAngleCos - p.y * rotAngleSin) * c;
                p.y = (z * rotAngleSin + p.y * rotAngleCos) * c;
            }
        }

        return points;
    }
//Orthographic
    private void initScene_Orthographic() {
        OrthographicCamera orthoCam = new OrthographicCamera();
        orthoCam.setLookAt(0, 0, 0);
        orthoCam.enableLookAt();
        orthoCam.setY(1.5);
        int[][] grid;

        getCurrentScene().switchCamera(orthoCam);

        DirectionalLight spotLight = new DirectionalLight(1f, -.1f, -.5f);
        spotLight.setPower(2);
        getCurrentScene().addLight(spotLight);

        grid = new int[10][];
        for (int i = 0; i < 10; i++)
            grid[i] = new int[10];

        Material material = new Material();
        try {
            material.addTexture(new Texture("checkerboard", R.drawable.frame));
            material.setColorInfluence(0);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        Object3D group = new Object3D();
        group.setRotX(-45);
        group.setRotY(-45);
        group.setY(-.8f);

        Object3D innerGroup = new Object3D();
        group.addChild(innerGroup);

        Plane plane = new Plane(Vector3.Axis.Y);
        plane.setMaterial(material);
        plane.setDoubleSided(true);
        plane.setColor(0xff0000ff);
        innerGroup.addChild(plane);

        Material cubeMaterial = new Material();
        cubeMaterial.enableLighting(true);
        cubeMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

        Random random = new Random();

        for (int i = 0; i < 40; i++) {
            Cube cube = new Cube(.1f);
            cube.setMaterial(cubeMaterial);
            cube.setY(7);
            cube.setColor(0x666666 + random.nextInt(0x999999));
            innerGroup.addChild(cube);

            // find grid available grid cell
            boolean foundCell = false;
            int row = 0, column = 0;
            while (!foundCell) {
                int cell = (int) Math.floor(Math.random() * 100);
                row = (int) Math.floor(cell / 10.f);
                column = cell % 10;
                if (grid[row][column] == 0) {
                    grid[row][column] = 1;
                    foundCell = true;
                }
            }

            Vector3 toPosition = new Vector3(-.45f + (column * .1f), .05f,
                    -.45f + (row * .1f));
            Vector3 fromPosition = new Vector3(toPosition.x, 7,
                    toPosition.z);

            TranslateAnimation3D anim = new TranslateAnimation3D(
                    fromPosition, toPosition);
            anim.setDurationMilliseconds(4000 + (int) (4000 * Math.random()));
            anim.setInterpolator(new BounceInterpolator());
            anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
            anim.setTransformable3D(cube);
            anim.setDelayMilliseconds((int) (10000 * Math.random()));
            getCurrentScene().registerAnimation(anim);
            anim.play();
        }

        RotateOnAxisAnimation anim = new RotateOnAxisAnimation(Vector3.Axis.Y, 359);
        anim.setDurationMilliseconds(20000);
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        anim.setTransformable3D(innerGroup);
        getCurrentScene().registerAnimation(anim);
        anim.play();

        getCurrentScene().addChild(group);
    }

//Sphere
    private void initScene_Sphere() {

        Object3D mSphere;
    //    mLayout =  inflater.inflate(getLayoutID(), container, false);
      //  mRajawaliSurface = (IRajawaliSurface)mLayout.findViewById(R.id.rajwali_surface);

        ALight light = new DirectionalLight(-1, 0, -1);
        light.setPower(2);

        getCurrentScene().addLight(light);

        getCurrentCamera().setPosition(0, 0, 7);
        getCurrentCamera().setLookAt(0, 0, 0);

        try {
            mSphere= new Sphere(1, 24, 24);
            Material material = new Material();
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            material.addTexture(new Texture("earthColors", R.drawable.earthtruecolor_nasa_big));
            material.setColorInfluence(0);
            mSphere.setMaterial(material);
            getCurrentScene().addChild(mSphere);

            Vector3 axis = new Vector3(3, 1, 6);
            axis.normalize();
            Animation3D anim = new RotateOnAxisAnimation(axis, 0, 360);
            anim.setDurationMilliseconds(8000);
            anim.setRepeatMode(Animation.RepeatMode.INFINITE);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.setTransformable3D(mSphere);
            getCurrentScene().registerAnimation(anim);
            anim.play();

        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

      //  ((View) mRajawaliSurface).animate().rotation(360.0f).setDuration(20000).setInterpolator(new BounceInterpolator());


    }

//Cube
    private void initScene_Cube() {
        ALight light = new DirectionalLight(-1, 0, -1);
        light.setPower(2);

        getCurrentScene().addLight(light);

        getCurrentCamera().setPosition(0, 0, 7);
        getCurrentCamera().setLookAt(0, 0, 0);

        try {
            Cube cube = new Cube(2.5f);
            Material material = new Material();
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            material.addTexture(new Texture("rajawaliTex", R.drawable.frame));
            material.setColorInfluence(0);
            cube.setMaterial(material);
            getCurrentScene().addChild(cube);

            Vector3 axis = new Vector3(0, 6, 0);
            axis.normalize();
            Animation3D anim = new RotateOnAxisAnimation(axis, 0, 360);
            anim.setDurationMilliseconds(8000);
            anim.setRepeatMode(Animation.RepeatMode.INFINITE);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.setTransformable3D(cube);
            getCurrentScene().registerAnimation(anim);
            anim.play();

        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
    }
//    ArcballCamera
    private void initScene_ArcballCamera() {
        try {
            DirectionalLight light = new DirectionalLight();
            light.setLookAt(1, -1, 1);
            light.enableLookAt();
            light.setPower(1.5f);
            getCurrentScene().addLight(light);

            light = new DirectionalLight();
            light.setLookAt(-1, 1, -1);
            light.enableLookAt();
            light.setPower(1.5f);
            getCurrentScene().addLight(light);

            DebugVisualizer debugViz = new DebugVisualizer(this);
            debugViz.addChild(new GridFloor());
            getCurrentScene().addChild(debugViz);

            final LoaderAWD parser = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_suzanne);
            parser.parse();

            final Object3D monkey = parser.getParsedObject();

            Material material = new Material();
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            material.setColor(0x990000);

            monkey.setMaterial(material);
            getCurrentScene().addChild(monkey);

            material = new Material();
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            material.setColor(0x999900);

            Object3D monkey2 = monkey.clone();
            monkey2.setMaterial(material);
            monkey2.setPosition(-3, 3, 3);
            getCurrentScene().addChild(monkey2);

            ArcballCamera arcball = new ArcballCamera(mContext, ((Activity)mContext).findViewById(R.id.launcher));
            arcball.setPosition(4, 4, 4);
            getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), arcball);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
//CanvasTexttoMaterial
    private void initScene_CanvasTexttoMaterial() {
        DirectionalLight light = new DirectionalLight(.1f, .1f, -1);
        light.setPower(2);
        getCurrentScene().addLight(light);

        Material timeSphereMaterial = new Material();
        timeSphereMaterial.enableLighting(true);
        timeSphereMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
        mTimeBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
        mTimeTexture = new AlphaMapTexture("timeTexture", mTimeBitmap);
        try {
            timeSphereMaterial.addTexture(mTimeTexture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        timeSphereMaterial.setColorInfluence(1);

        Sphere parentSphere = null;

        for (int i = 0; i < 1; i++) {
            Sphere timeSphere = new Sphere(.6f, 12, 12);
            timeSphere.setMaterial(timeSphereMaterial);
            timeSphere.setDoubleSided(true);
            timeSphere.setColor((int)(Math.random() * 0xffffff));

            if (parentSphere == null) {
                timeSphere.setPosition(0, 0, -3);
                timeSphere.setRenderChildrenAsBatch(true);
                getCurrentScene().addChild(timeSphere);
                parentSphere = timeSphere;
            } else {
                timeSphere.setX(-3 + (float) (Math.random() * 6));
                timeSphere.setY(-3 + (float) (Math.random() * 6));
                timeSphere.setZ(-3 + (float) (Math.random() * 6));
                parentSphere.addChild(timeSphere);
            }

            int direction = Math.random() < .5 ? 1 : -1;

            RotateOnAxisAnimation anim = new RotateOnAxisAnimation(Vector3.Axis.Y, 0,
                    360 * direction);
            anim.setRepeatMode(Animation.RepeatMode.INFINITE);
            anim.setDurationMilliseconds(i == 0 ? 12000
                    : 4000 + (int) (Math.random() * 4000));
            anim.setTransformable3D(timeSphere);
            getCurrentScene().registerAnimation(anim);
            anim.play();
        }
    }
    private void updateTimeBitmap() {
        new Thread(new Runnable() {
            public void run() {
                if (mTimeCanvas == null) {

                    mTimeCanvas = new Canvas(mTimeBitmap);
                    mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    mTextPaint.setColor(Color.WHITE);
                    mTextPaint.setTextSize(50);
                    mDateFormat = new SimpleDateFormat("HH:mm:ss",
                            Locale.ENGLISH);
                }
                //
                // -- Clear the canvas, transparent
                //
                mTimeCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                //
                // -- Draw the time on the canvas
                //
                mTimeCanvas.drawText(mDateFormat.format(new Date()), 75,
                        128, mTextPaint);
                //
                // -- Indicates that the texture should be updated on the OpenGL thread.
                //
                mShouldUpdateTexture = true;
            }
        }).start();
    }
    private void onRender_CanvasTexttoMaterial(long ellapsedRealtime, double deltaTime) {
        //
        // -- not a really accurate way of doing things but you get the point :)
        //
        if (mFrameCount++ >= mFrameRate) {
            mFrameCount = 0;
            updateTimeBitmap();
        }
        //
        // -- update the texture because it is ready
        //
        if (mShouldUpdateTexture) {
            mTimeTexture.setBitmap(mTimeBitmap);
            mTextureManager.replaceTexture(mTimeTexture);
            mShouldUpdateTexture = false;
        }

    }
//Skybox
    private void initScene_Skybox() {
    getCurrentCamera().setFarPlane(1000);
    /**
     * Skybox images by Emil Persson, aka Humus. http://www.humus.name humus@comhem.se
     */
    try {
        getCurrentScene().setSkybox(R.drawable.posx, R.drawable.negx,
                R.drawable.posy, R.drawable.negy, R.drawable.posz, R.drawable.negz);
    } catch (ATexture.TextureException e) {
        e.printStackTrace();
    }
}
    private void onRender_Skybox(long ellapsedRealtime, double deltaTime) {

        getCurrentCamera().rotate(Vector3.Axis.X, -0.2);
    }
//Terrain
    private SquareTerrain mTerrain;
    private double mLastY = 0;
    public void initScene_Terrain() {
        getCurrentScene().setBackgroundColor(0x999999);

        //
        // -- Use a chase camera that follows and invisible ('empty') object
        //    and add fog for a nice effect.
        //

        ChaseCamera chaseCamera = new ChaseCamera(new Vector3(0, 4, -8));
        chaseCamera.setFarPlane(1000);
        getCurrentScene().replaceAndSwitchCamera(chaseCamera, 0);

        getCurrentScene().setFog(new FogMaterialPlugin.FogParams(FogMaterialPlugin.FogType.LINEAR, 0x999999, 50, 100));

        //
        // -- Load a bitmap that represents the terrain. Its color values will
        //    be used to generate heights.
        //

        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.terrain);

        try {
            SquareTerrain.Parameters terrainParams = SquareTerrain.createParameters(bmp);
            // -- set terrain scale
            terrainParams.setScale(4f, 54f, 4f);
            // -- the number of plane subdivisions
            terrainParams.setDivisions(128);
            // -- the number of times the textures should be repeated
            terrainParams.setTextureMult(4);
            //
            // -- Terrain colors can be set by manually specifying base, middle and
            //    top colors.
            //
            // --  terrainParams.setBasecolor(Color.argb(255, 0, 0, 0));
            //     terrainParams.setMiddleColor(Color.argb(255, 200, 200, 200));
            //     terrainParams.setUpColor(Color.argb(255, 0, 30, 0));
            //
            // -- However, for this example we'll use a bitmap
            //
            terrainParams.setColorMapBitmap(bmp);
            //
            // -- create the terrain
            //
            mTerrain = TerrainGenerator.createSquareTerrainFromBitmap(terrainParams, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //
        // -- The bitmap won't be used anymore, so get rid of it.
        //
        bmp.recycle();

        DirectionalLight light = new DirectionalLight(0.2f, -1f, 0f);
        light.setPower(1f);
        getCurrentScene().addLight(light);

        //
        // -- A normal map material will give the terrain a bit more detail.
        //
        Material material = new Material();
        material.enableLighting(true);
        material.useVertexColors(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        try {
            Texture groundTexture = new Texture("ground", R.drawable.ground);
            groundTexture.setInfluence(.5f);
            material.addTexture(groundTexture);
            material.addTexture(new NormalMapTexture("groundNormalMap", R.drawable.groundnor));
            material.setColorInfluence(0);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        //
        // -- Blend the texture with the vertex colors
        //
        material.setColorInfluence(.5f);

        mTerrain.setMaterial(material);

        getCurrentScene().addChild(mTerrain);

        //
        // -- The empty object that will move along a curve and that
        //    will be follow by the camera
        //
        Object3D empty = new Object3D();
        empty.setVisible(false);

        //
        // -- Tell the camera to chase the empty.
        //
        chaseCamera.setLinkedObject(empty);

        //
        // -- Create a camera path based on the terrain height
        //
        CatmullRomCurve3D cameraPath = createCameraPath();

        SplineTranslateAnimation3D anim = new SplineTranslateAnimation3D(cameraPath);
        anim.setTransformable3D(empty);
        anim.setDurationMilliseconds(60000);
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        anim.setOrientToPath(true);
        getCurrentScene().registerAnimation(anim);
        anim.play();
    }
    private CatmullRomCurve3D createCameraPath() {
        CatmullRomCurve3D path = new CatmullRomCurve3D();

        float radius = 200;
        float degreeStep = 15;
        float distanceFromGround = 20;

        for (int i = 0; i < 360; i += degreeStep) {
            double radians = MathUtil.degreesToRadians(i);
            double x = Math.cos(radians) * Math.sin(radians)
                    * radius;
            double z = Math.sin(radians) * radius;
            double y = mTerrain.getAltitude(x, z) + distanceFromGround;

            if (i > 0)
                y = (y + mLastY) * .5f;
            mLastY = y;

            path.addPoint(new Vector3(x, y, z));
        }
        path.isClosedCurve(true);
        return path;
    }

//DebugVisualizer
    private DirectionalLight mDirectionalLight;
    private Camera mOtherCamera;
    private Object3D mSphere;
    public void initScene_DebugVisualizer() {
        mDirectionalLight = new DirectionalLight();
        mDirectionalLight.setLookAt(1, -1, -1);
        mDirectionalLight.enableLookAt();
        mDirectionalLight.setPosition(-4, 10, -4);
        mDirectionalLight.setPower(2);
        getCurrentScene().addLight(mDirectionalLight);
        getCurrentScene().setBackgroundColor(0x393939);

        animateCamera();

        mOtherCamera = new Camera();
        mOtherCamera.setPosition(4, 2, -10);
        mOtherCamera.setFarPlane(10);
        mOtherCamera.enableLookAt();

        mSphere = createAnimatedSphere();

        DebugVisualizer debugViz = new DebugVisualizer(this);
        debugViz.addChild(new GridFloor(20, 0x555555, 1, 20));
        debugViz.addChild(new DebugLight(mDirectionalLight, 0x999900, 1));
        debugViz.addChild(new DebugCamera(mOtherCamera, 0x000000, 1));
        getCurrentScene().addChild(debugViz);
    }
    private void animateCamera() {
        getCurrentCamera().enableLookAt();
        getCurrentCamera().setLookAt(0, 0, 0);

        EllipticalOrbitAnimation3D a = new EllipticalOrbitAnimation3D(new Vector3(), new Vector3(20,
                10, 20), Vector3.getAxisVector(Vector3.Axis.Y), 0, 360,
                EllipticalOrbitAnimation3D.OrbitDirection.CLOCKWISE);
        a.setDurationMilliseconds(20000);
        a.setRepeatMode(Animation.RepeatMode.INFINITE);
        a.setTransformable3D(getCurrentCamera());
        getCurrentScene().registerAnimation(a);
        a.play();
    }
    private Object3D createAnimatedSphere() {
        Object3D sphere = new Sphere(0.5f, 16, 12);
        Material sphereMaterial = new Material();
        sphereMaterial.enableLighting(true);
        sphereMaterial.setColor(Color.YELLOW);
        sphereMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
        sphere.setMaterial(sphereMaterial);
        getCurrentScene().addChild(sphere);

        RotateAroundAnimation3D a = new RotateAroundAnimation3D(
                new Vector3(1, 0, 1),
                Vector3.Axis.Y,
                4
        );
        a.setDurationMilliseconds(6000);
        a.setRepeatMode(Animation.RepeatMode.INFINITE);
        a.setTransformable3D(sphere);
        getCurrentScene().registerAnimation(a);
        a.play();

        return sphere;
    }
    public void onRender_DebugVisualizer(final long elapsedTime, final double deltaTime) {
        mOtherCamera.setLookAt(mSphere.getPosition());
        mDirectionalLight.setLookAt(mSphere.getPosition());

    }
//Catmul_Rom
    private void initScene_Catmul_Rom() {
        ALight light = new DirectionalLight(0, 0, -1);
        light.setPower(1);

        getCurrentScene().addLight(light);

        getCurrentCamera().setPosition(0, 0, 10);
        getCurrentCamera().setLookAt(0, 0, 0);
        getCurrentCamera().enableLookAt();

        Material material = new Material();

        // -- create a catmull-rom path. The first and the last point are control points.
        CatmullRomCurve3D path = new CatmullRomCurve3D();
        float r = 12;
        float rh = r * .5f;

        for (int i = 0; i < 16; i++) {
            path.addPoint(new Vector3(-rh + (Math.random() * r), -rh
                    + (Math.random() * r), -rh + (Math.random() * r)));
        }

        try {
            LoaderOBJ parser = new LoaderOBJ(mContext.getResources(),
                    mTextureManager, R.raw.arrow);
            parser.parse();
            Object3D arrow = parser.getParsedObject();
            arrow.setMaterial(material);
            arrow.setScale(.2f);
            arrow.setColor(0xffffff00);
            getCurrentScene().addChild(arrow);

            final SplineTranslateAnimation3D anim = new SplineTranslateAnimation3D(path);
            anim.setDurationMilliseconds(12000);
            anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
            // -- orient to path
            anim.setOrientToPath(true);
            anim.setTransformable3D(arrow);
            getCurrentScene().registerAnimation(anim);
            anim.play();
        } catch (ParsingException e) {
            e.printStackTrace();
        }

        int numPoints = path.getNumPoints();

        for (int i = 0; i < numPoints; i++) {
            Sphere s = new Sphere(.2f, 6, 6);
            s.setMaterial(material);
            s.setPosition(path.getPoint(i));

            if (i == 0)
                s.setColor(0xffff0000);
            else if (i == numPoints - 1)
                s.setColor(0xff0066ff);
            else
                s.setColor(0xff999999);

            getCurrentScene().addChild(s);
        }

        // -- visualize the line
        Stack<Vector3> linePoints = new Stack<Vector3>();
        for (int i = 0; i < 100; i++) {
            Vector3 point = new Vector3();
            path.calculatePoint(point, i / 100f);
            linePoints.add(point);
        }
        Line3D line = new Line3D(linePoints, 1, 0xffffffff);
        line.setMaterial(material);
        getCurrentScene().addChild(line);

        EllipticalOrbitAnimation3D camAnim = new EllipticalOrbitAnimation3D(
                new Vector3(), new Vector3(26, 0, 0), 0, 360,
                EllipticalOrbitAnimation3D.OrbitDirection.CLOCKWISE);
        camAnim.setDurationMilliseconds(10000);
        camAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
        camAnim.setTransformable3D(getCurrentCamera());
        getCurrentScene().registerAnimation(camAnim);
        camAnim.play();
    }

    @Override
    public void setSurface(Surface surface) {
        mSurface = surface;
        mStreamingTexture.getSurfaceTexture().setDefaultBufferSize(1024, 1024);
    }

}
