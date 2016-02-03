package com.android.soma.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.android.soma.PagedView;
import com.android.soma.R;
import com.android.soma.ui.materials.CustomMaterialPlugin;
import com.android.soma.ui.planes.PlanesGalore;
import com.android.soma.ui.planes.PlanesGaloreMaterialPlugin;

import org.rajawali3d.IRajawaliDisplay;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.AlphaMapTexture;
import org.rajawali3d.materials.textures.NormalMapTexture;
import org.rajawali3d.materials.textures.SphereMapTexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.NPrism;
import org.rajawali3d.primitives.Plane;
import org.rajawali3d.primitives.RectangularPrism;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.IRajawaliSurfaceRenderer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ViewToTextureFragment extends Fragment implements IRajawaliDisplay, View.OnClickListener,View.OnTouchListener {

    FragmentToDraw mFragmentToDraw;
    Handler mHandler;
    protected FrameLayout mLayout;
    protected IRajawaliSurface mRajawaliSurface;
    protected IRajawaliSurfaceRenderer mRenderer;

    private Point mScreenSize;

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mLayout != null)
            mLayout.removeView((View) mRajawaliSurface);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ((ViewTextureRenderer) mRenderer).setTouch(event.getX()
                    / mScreenSize.x, 1.0f - (event.getY() / mScreenSize.y));
        }
        return ((View) mRajawaliSurface).onTouchEvent(event);
    }

    @Override
    public int getLayoutID() {
        return 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mLayout = (FrameLayout) inflater.inflate(R.layout.rajawali_textureview_fragment, container, false);

        mLayout.findViewById(R.id.relative_layout_loader_container).bringToFront();

        // Find the TextureView
        mRajawaliSurface = (IRajawaliSurface) mLayout.findViewById(R.id.rajwali_surface);
        // Create the renderer
        mRenderer = createRenderer();
        onBeforeApplyRenderer();
        applyRenderer();

        mHandler = new Handler(Looper.getMainLooper());

        final FrameLayout fragmentFrame = new FrameLayout(getActivity());
        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(1024, 1024);
        fragmentFrame.setLayoutParams(params);
        fragmentFrame.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_bright));
        fragmentFrame.setId(R.id.view_to_texture_frame);
        fragmentFrame.setVisibility(View.INVISIBLE);
        mLayout.addView(fragmentFrame);

        mFragmentToDraw = new FragmentToDraw();
        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.view_to_texture_frame, mFragmentToDraw, "custom").commit();
        return mLayout;
    }

    @Override
    public ViewTextureRenderer createRenderer() {
        return new ViewTextureRenderer(getActivity());
    }

    protected void onBeforeApplyRenderer() {

    }

    protected void applyRenderer() {
        mRajawaliSurface.setSurfaceRenderer(mRenderer);
    }
    private final class ViewTextureRenderer extends RajawaliRenderer implements StreamingTexture.ISurfaceListener {
        private int mFrameCount;
        private Surface mSurface;
        private StreamingTexture mStreamingTexture;
        private volatile boolean mShouldUpdateTexture;

        private final float[] mMatrix = new float[16];

        private Object3D mObject3D;
        private Object3D mObject3D_2;
//CanvasTextRenderer
        private AlphaMapTexture mTimeTexture;
        private Bitmap mTimeBitmap;
        private Canvas mTimeCanvas;
        private Paint mTextPaint;
        private SimpleDateFormat mDateFormat;
        private volatile boolean mShouldUpdateCanvasTexture;
        private int mFrameCountCanvas;
//FrameTexture
        private AlphaMapTexture mPictureTexture;
        private Animation3D mLightAnim;

//        Optimized2000PlanesRenderer
        private long mStartTime;
        private Material mMaterial;
        private PlanesGaloreMaterialPlugin mMaterialPlugin;
// TwoDimensional
        private float mTime;
        private Material mCustomMaterial;
// LoadObjModel
        private PointLight mLight;
        private Object3D mObjectGroup;
        private Animation3D mCameraAnim, mLightAnim_LoadObj;

        public ViewTextureRenderer(Context context) {
            super(context);
        }

        @Override
        public void onOffsetsChanged(float v, float v2, float v3, float v4, int i, int i2) {

        }
        @Override
        public void onTouchEvent(MotionEvent event) {

        }
        @Override
        public void initScene() {
            initScene_ViewToTexture();
//            initScene_2000Planes();
            //initScene_TwoDimensional();
            initScene_LoadObjModel();
            initScene_AWD();
             }

        public void initScene_ViewToTexture() {
            ALight light = new DirectionalLight(-1, 0, -1);
           // DirectionalLight Directlight = new DirectionalLight(.1f, .1f, -1);
            light.setPower(8);

            getCurrentScene().addLight(light);
            getCurrentCamera().setPosition(0, 0, 7);
            getCurrentCamera().setLookAt(0, 0, 0);

//            mObject3D = new Cube(3.0f);
//            mObject3D = new Plane(3.0f,3.0f,2,2);
//            mObject3D= new Cube(3.0f,true,false,false,false,false);
   //         mObject3D = new Cube(3.0f,true);
//            mObject3D = new NPrism(6,3.0d,3.0f);
//            mObject3D = new NPrism(6,2.0d,2.0f,0,3);
//            mObject3D = new NPrism(6,2.0d,2.0f,3);
//            mObject3D = new RectangularPrism(3.0f);
 //             mObject3D = new RectangularPrism(3.0f,3,5.0f,true);
            mObject3D = new Sphere(1, 24, 24);
//            mObject3D = new ScreenQuad();
//            mObject3D = new Plane();
            //mObject3D.setColor(0);

            mObject3D.setPosition(0, 2, -1);
            mObject3D.setDoubleSided(true);
            mObject3D.setColor((int) (Math.random() * 0xffffff));
        //    mObject3D.setColor(0);
            mObject3D.setRenderChildrenAsBatch(true);

                Material material = new Material();

                material.enableLighting(true);
                material.setDiffuseMethod(new DiffuseMethod.Lambert());
                material.setSpecularMethod(new SpecularMethod.Phong());
                material.setColorInfluence(0.5f);


//View to Texture
            mStreamingTexture = new StreamingTexture("viewTexture", this);
            mStreamingTexture.setInfluence(.5f);

            //PictureTexture
//            mPictureTexture=new AlphaMapTexture("rajawaliTex", R.drawable.frame);
            mPictureTexture=new AlphaMapTexture("rajawaliTex", R.drawable.earth_diffuse);
            mPictureTexture.setInfluence(.5f);

//TimeBitmapTexture
            mTimeBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
            mTimeBitmap = PagedView.mCanvasBitmap;
            mTimeTexture = new AlphaMapTexture("timeTexture", mTimeBitmap);
            mTimeTexture.setInfluence(.5f);



            try {
                {
                //    material.addTexture(mStreamingTexture);
                    material.addTexture(mPictureTexture);
//                    material.addTexture(mTimeTexture);

                }
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }
            mObject3D.setMaterial(material);
            getCurrentScene().addChild(mObject3D);

            Vector3 axis = new Vector3(1, 1, 1);
            axis.normalize();

            RotateOnAxisAnimation anim = new RotateOnAxisAnimation(axis, 0,360);
            anim.setRepeatMode(Animation.RepeatMode.INFINITE);
            anim.setDurationMilliseconds(12000);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.setTransformable3D(mObject3D);
            getCurrentScene().registerAnimation(anim);
            anim.play();
//mObject3D_2

            mObject3D_2 = new Cube(3.0f);
//            mObject3D_2 =new Sphere(1,20,20);
            mObject3D_2.setPosition(0, 2, -1);
            mObject3D_2.setDoubleSided(true);
            mObject3D_2.setColor((int) (Math.random() * 0xffffff));
            mObject3D_2.setRenderChildrenAsBatch(true);

            Material material2 = new Material();

            material2.enableLighting(true);
            material2.setDiffuseMethod(new DiffuseMethod.Lambert());
            material2.setColorInfluence(.5f);

            mObject3D_2.setMaterial(material2);
            getCurrentScene().addChild(mObject3D_2);
            try {
                {
                    //    material2.addTexture(mStreamingTexture);
//                    material2.addTexture(mPictureTexture);
                    material2.addTexture(mTimeTexture);
                }
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }
            Vector3 axis2 = new Vector3(1, 1, 1);
            axis2.normalize();
            RotateOnAxisAnimation anim2 = new RotateOnAxisAnimation(axis2, 0,360);
            anim2.setRepeatMode(Animation.RepeatMode.INFINITE);
            anim2.setDurationMilliseconds(12000);
            anim2.setInterpolator(new AccelerateDecelerateInterpolator());
            anim2.setTransformable3D(mObject3D_2);
            getCurrentScene().registerAnimation(anim2);
            anim2.play();

//mLightAnim
            mLightAnim = new TranslateAnimation3D(new Vector3(-1, 1, 1),
                    new Vector3(1, -1, 1));
            mLightAnim.setDurationMilliseconds(4000);
            mLightAnim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
            mLightAnim.setTransformable3D(light);
            mLightAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            getCurrentScene().registerAnimation(mLightAnim);
            mLightAnim.play();
        }

        final Runnable mUpdateTexture = new Runnable() {
            public void run() {
                // -- Draw the view on the canvas
                final Canvas canvas = mSurface.lockCanvas(null);
                mStreamingTexture.getSurfaceTexture().getTransformMatrix(mMatrix);
                mFragmentToDraw.getView().draw(canvas);
                mSurface.unlockCanvasAndPost(canvas);
                // -- Indicates that the texture should be updated on the OpenGL thread.
                mShouldUpdateTexture = true;
            }
        };

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime)
        {
            super.onRender(ellapsedRealtime, deltaTime);
            onRender_ViewToTexture(ellapsedRealtime, deltaTime);
            onRender_CanvasToTexture(ellapsedRealtime, deltaTime);
//            onRender_2000Planes(ellapsedRealtime, deltaTime);
         //   onRender_TwoDimensional(ellapsedRealtime, deltaTime);
        }
        private void onRender_ViewToTexture(long ellapsedRealtime, double deltaTime) {
            // -- not a really accurate way of doing things but you get the point :)
            if (mSurface != null && mFrameCount++ >= (mFrameRate * 0.25)) {
                mFrameCount = 0;
                mHandler.post(mUpdateTexture);
            }
            // -- update the texture because it is ready
            if (mShouldUpdateTexture) {
                mStreamingTexture.update();
                mShouldUpdateTexture = false;
            }
        }

        @Override
        public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
            super.onRenderSurfaceCreated(config, gl, width, height);
            mStartTime = System.currentTimeMillis();
        }

        @Override
        public void setSurface(Surface surface) {
            mSurface = surface;
            mStreamingTexture.getSurfaceTexture().setDefaultBufferSize(1024, 1024);
        }

        public void updateTimeBitmap() {
            new Thread(new Runnable() {
                public void run() {
                    if (mTimeCanvas == null) {

                        mTimeCanvas = new Canvas(mTimeBitmap);
                        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        mTextPaint.setColor(Color.WHITE);
                        mTextPaint.setTextSize(35);
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
                    mShouldUpdateCanvasTexture = true;
                }
            }).start();
        }

        private void onRender_CanvasToTexture(long ellapsedRealtime, double deltaTime) {
            //
            // -- not a really accurate way of doing things but you get the point :)
            //
            if (mFrameCountCanvas++ >= mFrameRate) {
                mFrameCountCanvas = 0;
                updateTimeBitmap();
            }
            //
            // -- update the texture because it is ready
            //
            if (mShouldUpdateCanvasTexture) {
                mTimeTexture.setBitmap(mTimeBitmap);
                mTextureManager.replaceTexture(mTimeTexture);
                mShouldUpdateCanvasTexture = false;
            }
        }


        private void initScene_2000Planes() {
            DirectionalLight light = new DirectionalLight(0, 0, 1);

            getCurrentScene().addLight(light);
            getCurrentCamera().setPosition(0, 0, -16);

            final PlanesGalore planes = new PlanesGalore();
            mMaterial = planes.getMaterial();
            mMaterial.setColorInfluence(0);
            try {
                mMaterial.addTexture(new Texture("flickrPics", R.drawable.flickrpics));
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }

            mMaterialPlugin = planes.getMaterialPlugin();

            planes.setDoubleSided(true);
            planes.setZ(4);
            getCurrentScene().addChild(planes);

            Object3D empty = new Object3D();
            getCurrentScene().addChild(empty);

            CatmullRomCurve3D path = new CatmullRomCurve3D();
            path.addPoint(new Vector3(-4, 0, -20));
            path.addPoint(new Vector3(2, 1, -10));
            path.addPoint(new Vector3(-2, 0, 10));
            path.addPoint(new Vector3(0, -4, 20));
            path.addPoint(new Vector3(5, 10, 30));
            path.addPoint(new Vector3(-2, 5, 40));
            path.addPoint(new Vector3(3, -1, 60));
            path.addPoint(new Vector3(5, -1, 70));

            final SplineTranslateAnimation3D anim = new SplineTranslateAnimation3D(path);
            anim.setDurationMilliseconds(20000);
            anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
            anim.setTransformable3D(getCurrentCamera());
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            getCurrentScene().registerAnimation(anim);
            anim.play();

            getCurrentCamera().setLookAt(new Vector3(0, 0, 30));
        }

        private void onRender_2000Planes(long ellapsedRealtime, double deltaTime) {
            mMaterial.setTime((System.currentTimeMillis() - mStartTime) / 1000f);
            mMaterialPlugin.setCameraPosition(getCurrentCamera().getPosition());
        }

        private void initScene_TwoDimensional() {
            mCustomMaterial = new Material();
            mCustomMaterial.enableTime(true);
            mCustomMaterial.addPlugin(new CustomMaterialPlugin());

            ScreenQuad screenQuad = new ScreenQuad();
            screenQuad.setMaterial(mCustomMaterial);
            getCurrentScene().addChild(screenQuad);
        }

        private void onRender_TwoDimensional(long ellapsedRealtime, double deltaTime) {
            mTime += .007f;
            mCustomMaterial.setTime(mTime);
        }

        private void initScene_LoadObjModel() {


            mLight = new PointLight();
            mLight.setPosition(0, -3, 4);
            mLight.setPower(3);

            getCurrentScene().addLight(mLight);
            getCurrentCamera().setZ(16);

            LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(),
                    mTextureManager, R.raw.multiobjects_obj);
            try {
                objParser.parse();
                mObjectGroup = objParser.getParsedObject();
                mObjectGroup.setPosition(0,-5,-8);
                getCurrentScene().addChild(mObjectGroup);

                mCameraAnim = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
                mCameraAnim.setDurationMilliseconds(8000);
                mCameraAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
                mCameraAnim.setTransformable3D(mObjectGroup);
            } catch (ParsingException e) {
                e.printStackTrace();
            }

            mLightAnim_LoadObj = new EllipticalOrbitAnimation3D(new Vector3(),
                    new Vector3(0, 10, 0), Vector3.getAxisVector(Vector3.Axis.Z), 0,
                    360, EllipticalOrbitAnimation3D.OrbitDirection.CLOCKWISE);

            mLightAnim_LoadObj.setDurationMilliseconds(3000);
            mLightAnim_LoadObj.setRepeatMode(Animation.RepeatMode.INFINITE);
            mLightAnim_LoadObj.setTransformable3D(mLight);

            getCurrentScene().registerAnimation(mCameraAnim);
            getCurrentScene().registerAnimation(mLightAnim_LoadObj);

            mCameraAnim.play();
            mLightAnim_LoadObj.play();
        }

        private void initScene_AWD() {

            try {
                final LoaderAWD parser = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_arrows);
                parser.parse();

                final Object3D obj = parser.getParsedObject();

                obj.setScale(0.25f);
                obj.setPosition(0,6,-2);
                getCurrentScene().addChild(obj);

                final Animation3D anim = new RotateOnAxisAnimation(Vector3.Axis.Y, -360);
                anim.setDurationDelta(4d);
                anim.setRepeatMode(Animation.RepeatMode.INFINITE);
                anim.setTransformable3D(obj);
                anim.play();
                getCurrentScene().registerAnimation(anim);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void setTouch(float x, float y) {
//            mFilter.addTouch(x, y, frameCount * .05f);
        }
    }

    public static final class FragmentToDraw extends Fragment {

        WebView mWebView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.view_to_texture, container, false);
            mWebView = (WebView) view.findViewById(R.id.webview);
            mWebView.setWebViewClient(new WebViewClient());
            // Load the Rajawali Repo commit activity graph
           // mWebView.loadUrl("https://github.com/Rajawali/Rajawali/graphs/commit-activity");
            mWebView.loadUrl("http://www.clintonmedbery.com/wp-content/uploads/2015/04/earthtruecolor_nasa_big.jpg");
            //mWebView.loadUrl("https://www.baidu.com/");
            //mWebView.loadUrl("https://plus.google.com/communities/116529974266844528013");
            mWebView.setInitialScale(100);
            mWebView.setScrollY(0);
            mWebView.animate().rotationYBy(360.0f).setDuration(60000);
            view.setVisibility(View.INVISIBLE);
            return view;
        }
    }


}
