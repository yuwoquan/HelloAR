//================================================================================================================================
//
//  Copyright (c) 2015-2017 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
//  EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
//  and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package cn.easyar.samples.helloar;

import android.opengl.GLES20;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import cn.easyar.CameraCalibration;
import cn.easyar.CameraDevice;
import cn.easyar.CameraDeviceFocusMode;
import cn.easyar.CameraDeviceType;
import cn.easyar.CameraFrameStreamer;
import cn.easyar.Frame;
import cn.easyar.FunctorOfVoidFromPointerOfTargetAndBool;
import cn.easyar.ImageTarget;
import cn.easyar.ImageTracker;
import cn.easyar.Renderer;
import cn.easyar.StorageType;
import cn.easyar.Target;
import cn.easyar.TargetInstance;
import cn.easyar.TargetStatus;
import cn.easyar.Vec2I;
import cn.easyar.Vec4I;

public class HelloAR
{
    private MainActivity mainActivity;
    private CameraDevice camera;
    private CameraFrameStreamer streamer;
    private ArrayList<ImageTracker> trackers;
    private ArrayList<VideoRenderer> video_renderers;
    private VideoRenderer current_video_renderer;
    private int tracked_target = 0;
    private int active_target = 0;
    private ARVideo video = null;
    private Renderer videobg_renderer;
    private String target_name;
//    private BoxRenderer box_renderer;
    private boolean viewport_changed = false;
    private Vec2I view_size = new Vec2I(0, 0);
    private int rotation = 0;
    private Vec4I viewport = new Vec4I(0, 0, 1280, 720);

    public HelloAR(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        trackers = new ArrayList<ImageTracker>();
    }

    private void loadFromImage(ImageTracker tracker, String path)
    {
        ImageTarget target = new ImageTarget();
        String jstr = "{\n"
            + "  \"images\" :\n"
            + "  [\n"
            + "    {\n"
            + "      \"image\" : \"" + path + "\",\n"
            + "      \"name\" : \"" + path.substring(0, path.indexOf(".")) + "\"\n"
            + "    }\n"
            + "  ]\n"
            + "}";
        target.setup(jstr, StorageType.Assets | StorageType.Json, "");
        tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.e("123", "17" );
                Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    private void loadFromJsonFile(ImageTracker tracker, String path, String targetname)
    {
        ImageTarget target = new ImageTarget();
        target.setup(path, StorageType.Assets, targetname);
        tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.e("123", "16" );
                Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    private void loadAllFromJsonFile(ImageTracker tracker, String path)
    {
        for (ImageTarget target : ImageTarget.setupAll(path, StorageType.Assets)) {
            tracker.loadTarget(target, new FunctorOfVoidFromPointerOfTargetAndBool() {
                @Override
                public void invoke(Target target, boolean status) {
                    Log.e("123", "15" );
                    Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
                }
            });
        }
    }

    public boolean initialize()
    {
        camera = new CameraDevice();
        camera.cameraCalibration();
        streamer = new CameraFrameStreamer();
        streamer.attachCamera(camera);

        boolean status = true;
        status &= camera.open(CameraDeviceType.Default);
        Log.e("123", "6666666" );
        camera.setSize(new Vec2I(1280, 720));

        if (!status) {
            Log.e("123", "14" );
            return status;
        }
        /*
        设置图片的名字，找到所需的识别图
         */
        ImageTracker tracker = new ImageTracker();
        tracker.attachStreamer(streamer);
        loadFromJsonFile(tracker, "targets.json", "argame");
        loadFromJsonFile(tracker, "targets.json", "idback");
        loadFromImage(tracker, "victor.jpg");
        loadFromImage(tracker, "water_one.jpg");
        loadFromImage(tracker, "water_two.jpg");
        loadFromImage(tracker, "helmet.jpg");
        loadFromImage(tracker, "helmetone.jpg");
        trackers.add(tracker);
        return status;
    }

    public void dispose()
    {
        for (ImageTracker tracker : trackers) {
            Log.e("", "丢失了" );
            tracker.dispose();
        }
        trackers.clear();
//        box_renderer = null;
        if (videobg_renderer != null) {
            Log.e("123", "13" );
            videobg_renderer.dispose();
            videobg_renderer = null;
        }
        if (streamer != null) {
            Log.e("123", "12" );
            streamer.dispose();
            streamer = null;
        }
        if (camera != null) {
            Log.e("123", "11" );
            camera.dispose();
            camera = null;
        }
    }

    public boolean start()
    {
        boolean status = true;
        status &= (camera != null) && camera.start();
        status &= (streamer != null) && streamer.start();
        camera.setFocusMode(CameraDeviceFocusMode.Continousauto);
        for (ImageTracker tracker : trackers) {
            Log.e("123", "10" );
            status &= tracker.start();
        }
        return status;
    }

    public boolean stop()
    {
        boolean status = true;
        for (ImageTracker tracker : trackers) {
            Log.e("123", "9" );
            status &= tracker.stop();
        }
        status &= (streamer != null) && streamer.stop();
        status &= (camera != null) && camera.stop();
        return status;
    }

    public void initGL()
    {
        if (active_target != 0) {
            video.onLost();
            video.dispose();
            video  = null;
            tracked_target = 0;
            active_target = 0;
        }
        if (videobg_renderer != null) {
            videobg_renderer.dispose();
        }
        videobg_renderer = new Renderer();
        video_renderers = new ArrayList<VideoRenderer>();
        for (int k = 0; k < 5; k += 1) {
            VideoRenderer video_renderer = new VideoRenderer();
            video_renderer.init();
            video_renderers.add(video_renderer);
        }
        current_video_renderer = null;

        if (videobg_renderer != null) {
            Log.e("123", "8" );
            videobg_renderer.dispose();
        }
        Log.e("123", "9" );
        videobg_renderer = new Renderer();
//        box_renderer = new BoxRenderer();
//        box_renderer.init();
    }

    public void resizeGL(int width, int height)
    {
        view_size = new Vec2I(width, height);
        viewport_changed = true;
        Log.e("123", "8" );
    }
    private void updateViewport()
    {
        CameraCalibration calib = camera != null ? camera.cameraCalibration() : null;
        int rotation = calib != null ? calib.rotation() : 0;
        if (rotation != this.rotation) {
            Log.e("123", "7" );
            this.rotation = rotation;
            viewport_changed = true;
        }
        if (viewport_changed) {
            Log.e("123", "6" );
            Vec2I size = new Vec2I(1, 1);
            if ((camera != null) && camera.isOpened()) {
                size = camera.size();
                Log.e("123", "5" );
            }
            if (rotation == 90 || rotation == 270) {
                Log.e("123", "4" );
                size = new Vec2I(size.data[1], size.data[0]);
            }
            float scaleRatio = Math.max((float) view_size.data[0] / (float) size.data[0], (float) view_size.data[1] / (float) size.data[1]);
            Vec2I viewport_size = new Vec2I(Math.round(size.data[0] * scaleRatio), Math.round(size.data[1] * scaleRatio));
            viewport = new Vec4I((view_size.data[0] - viewport_size.data[0]) / 2, (view_size.data[1] - viewport_size.data[1]) / 2, viewport_size.data[0], viewport_size.data[1]);

            if ((camera != null) && camera.isOpened())
                Log.e("123", "3" );
                viewport_changed = false;
        }
    }

    public void render()
    {

        GLES20.glClearColor(1.f, 1.f, 1.f, 1.f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        if (videobg_renderer != null) {
            Vec4I default_viewport = new Vec4I(0, 0, view_size.data[0], view_size.data[1]);
            GLES20.glViewport(default_viewport.data[0], default_viewport.data[1], default_viewport.data[2], default_viewport.data[3]);
            if (videobg_renderer.renderErrorMessage(default_viewport)) {
                Log.e("123", "2" );
                return;
            }
        }
        if (streamer == null) {
            return;
        }
        Frame frame = streamer.peek();
        try {
            updateViewport();
            GLES20.glViewport(viewport.data[0], viewport.data[1], viewport.data[2], viewport.data[3]);
            if (videobg_renderer != null) {
//                Log.e("123", "6666666" );
                videobg_renderer.render(frame, viewport);
            }

            ArrayList<TargetInstance> targetInstances = frame.targetInstances();
            if (targetInstances.size() > 0) {
                TargetInstance targetInstance = targetInstances.get(0);
                Target target = targetInstance.target();
                int status = targetInstance.status();
                if (status == TargetStatus.Tracked) {
                    int id = target.runtimeID();
                    if (active_target != 0 && active_target != id) {
                        video.onLost();
                        video.dispose();
                        video  = null;
                        tracked_target = 0;
                        active_target = 0;
                    }
                    /*
                    识别图播放视频
                     */
                    if (tracked_target == 0) {
                        if (video == null && video_renderers.size() > 0) {
                           target_name = target.name();
                            if (target_name.equals("idback") && video_renderers.get(0).texId() != 0) {
                                video = new ARVideo();
//                                video.openTransparentVideoFile("video.mp4", video_renderers.get(0).texId());
                                video.openVideoFile("video.mp4", video_renderers.get(0).texId());
                                current_video_renderer = video_renderers.get(0);
                                EventBus.getDefault().post(new MessageEvent("0"));
                            }
                            if (target_name.equals("helmet") && video_renderers.get(1).texId() != 0) {
                                video = new ARVideo();
                                video.openVideoFile("video.mp4", video_renderers.get(1).texId());
                                current_video_renderer = video_renderers.get(1);
                                EventBus.getDefault().post(new MessageEvent("0"));
                            }
                            if (target_name.equals("helmetone") && video_renderers.get(2).texId() != 0) {
                                video = new ARVideo();
                                video.openVideoFile("video.mp4", video_renderers.get(2).texId());
                                current_video_renderer = video_renderers.get(2);
                                EventBus.getDefault().post(new MessageEvent("0"));
                            }
                        }

                        if (video != null) {
                            video.onFound();
                            tracked_target = id;
                            active_target = id;
                        }
                        /*
                        识别后发送信息判断，加载所需要的布局
                         */
//                        if (target_name.equals("idback")){
//                            EventBus.getDefault().post(new MessageEvent("1"));
//                        }
//                        if (target_name.equals("idback")){
//                            EventBus.getDefault().post(new MessageEvent("1"));
//                        }
                        if (target_name.equals("victor")){
                            EventBus.getDefault().post(new MessageEvent("2"));
                        }
                        if (target_name.equals("water_two")){
                            EventBus.getDefault().post(new MessageEvent("2"));
                        }
                        if (target_name.equals("victor")){
                            EventBus.getDefault().post(new MessageEvent("2"));
                        }
                    }
                    ImageTarget imagetarget = target instanceof ImageTarget ? (ImageTarget)(target) : null;
                    if (imagetarget != null) {
                        if (current_video_renderer != null) {
                            video.update();
                            if (video.isRenderTextureAvailable()) {
                                current_video_renderer.render(camera.projectionGL(0.2f, 500.f), targetInstance.poseGL(), imagetarget.size());
                            }
                        }
                    }
                }
            } else {
                if (tracked_target != 0) {
                    video.onLost();
                    tracked_target = 0;
                }
            }



            for (TargetInstance targetInstance : frame.targetInstances()) {
                int status = targetInstance.status();
                if (status == TargetStatus.Tracked) {
                    Target target = targetInstance.target();
                    ImageTarget imagetarget = target instanceof ImageTarget ? (ImageTarget) (target) : null;
                    if (imagetarget == null) {
                        EventBus.getDefault().post(new MessageEvent("8788"));
                                        Log.e("123", "1" );
//                        continue;
                    }else {

                    }
//                    if (box_renderer != null) {

//                        box_renderer.render(camera.projectionGL(0.2f, 500.f), targetInstance.poseGL(), imagetarget.size());
//                    }
                }
            }
        }
        finally {
            frame.dispose();
        }
    }


}
