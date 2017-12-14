//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloar;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import cn.easyar.engine.EasyAR;

public class GLView extends GLSurfaceView
{
    private HelloAR helloAR;
    private MainActivity mainActivity;
    public GLView(Context context ,MainActivity mainActivity)
    {
        super(context);
        this.mainActivity = mainActivity;
        setEGLContextFactory(new ContextFactory());
        setEGLConfigChooser(new ConfigChooser());

        helloAR = new HelloAR(mainActivity);

        this.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                try {
                    synchronized (helloAR) {
                        Log.e("123","18" );
                        helloAR.initGL();
                    }
                } catch (Throwable ex) {
                    Log.e("HelloAR", ex.getMessage());
                    Log.e("HelloAR", Log.getStackTraceString(ex));
                }
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int w, int h) {
                try {
                    synchronized (helloAR) {

                        helloAR.resizeGL(w, h);
                    }
                } catch (Throwable ex) {
                    Log.e("HelloAR", ex.getMessage());
                    Log.e("HelloAR", Log.getStackTraceString(ex));
                }
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                try {
                    synchronized (helloAR) {
                        helloAR.render();
                    }
                } catch (Throwable ex) {
                    Log.e("123", "19" );
                }
            }
        });
        this.setZOrderMediaOverlay(true);
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        try {
            Log.e("123","20" );
            synchronized (helloAR) {
                if (helloAR.initialize()) {
                    helloAR.start();
                }
            }
        } catch (Throwable ex) {
            Log.e("123", "21" );
            Log.e("HelloAR", ex.getMessage());
            Log.e("HelloAR", Log.getStackTraceString(ex));
        }
    }

    @Override
    protected void onDetachedFromWindow()
    {
        try {
            synchronized (helloAR) {
                Log.e("123","22" );
                helloAR.stop();
            }
        } catch (Throwable ex) {
            Log.e("123", "23" );
            Log.e("HelloAR", ex.getMessage());
            Log.e("HelloAR", Log.getStackTraceString(ex));
        }
        try {
            synchronized (helloAR) {
                Log.e("123","24" );
                helloAR.dispose();
            }
        } catch (Throwable ex) {
            Log.e("123", "25" );
            Log.e("HelloAR", ex.getMessage());
            Log.e("HelloAR", Log.getStackTraceString(ex));
        }
        super.onDetachedFromWindow();
    }

    @Override
    public void onResume()
    {
        Log.e("123", "26" );
        super.onResume();
        EasyAR.onResume();
    }

    @Override
    public void onPause()
    {
        Log.e("123", "27" );
        EasyAR.onPause();
        super.onPause();
    }

    private static class ContextFactory implements GLSurfaceView.EGLContextFactory
    {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig)
        {
            EGLContext context;
            int[] attrib = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
            context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib );
            Log.e("123", "28" );
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context)
        {
            Log.e("123", "29" );
            egl.eglDestroyContext(display, context);
        }
    }

    private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser
    {
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display)
        {
            final int EGL_OPENGL_ES2_BIT = 0x0004;
            final int[] attrib = { EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE };

            int[] num_config = new int[1];
            egl.eglChooseConfig(display, attrib, null, 0, num_config);
            Log.e("123", "30" );
            int numConfigs = num_config[0];
            if (numConfigs <= 0)

                throw new IllegalArgumentException("fail to choose EGL configs");

            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, attrib, configs, numConfigs,
                    num_config);

            for (EGLConfig config : configs)
            {
                Log.e("123", "31" );
                int[] val = new int[1];
                int r = 0, g = 0, b = 0, a = 0, d = 0;
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_DEPTH_SIZE, val))
                    d = val[0];
                if (d < 16)
                    continue;

                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_RED_SIZE, val))
                    r = val[0];
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_GREEN_SIZE, val))
                    g = val[0];
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_BLUE_SIZE, val))
                    b = val[0];
                if (egl.eglGetConfigAttrib(display, config, EGL10.EGL_ALPHA_SIZE, val))
                    a = val[0];
                if (r == 8 && g == 8 && b == 8 && a == 0)
                    return config;
            }

            return configs[0];
        }
    }
}
