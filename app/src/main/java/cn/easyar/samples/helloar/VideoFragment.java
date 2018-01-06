package cn.easyar.samples.helloar;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

/**
 * Created by Administrator on 2017/12/14.
 */

public class VideoFragment extends Fragment {
    @Nullable
    private VideoView videoView;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.video_fragment,null);
        videoView= (VideoView) view.findViewById(R.id.videoview);
        String uri = "android.resource://" + getContext().getPackageName() + "/" + R.raw.video;
        videoView.setVideoPath(uri);
        videoView.setBackgroundColor(0);
        videoView.setZOrderOnTop(true);
        videoView.setVideoURI(Uri.parse(uri));
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                getActivity().onBackPressed();

            }
        });

        return view;
    }
}
