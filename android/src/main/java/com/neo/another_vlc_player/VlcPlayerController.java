package com.neo.another_vlc_player;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Handler;
import android.view.Surface;
import android.view.View;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.Log;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.view.TextureRegistry;

public class VlcPlayerController {
    static int HW_ACC_AUTO = 0;
    static int HW_ACC_DISABLED = 1;
    static int HW_ACC_DECODING = 2;
    static int HW_ACC_FULL = 3;

    Object id;
    private LibVLC libVLC;
    private MediaPlayer mediaPlayer;
    private TextureRegistry.SurfaceTextureEntry surfaceTextureEntry;
    private SurfaceTexture surfaceTexture;
    private MethodChannel channel;

    MediaPlayer.EventListener mediaListener = new MediaPlayer.EventListener() {
        @Override
        public void onEvent(MediaPlayer.Event event) {
            int height = 0;
            int width = 0;
            Media.VideoTrack currentVideoTrack = mediaPlayer.getCurrentVideoTrack();
            if (currentVideoTrack != null) {
                height = currentVideoTrack.height;
                width = currentVideoTrack.width;
            }

            Map map = new HashMap();
            map.put("id", id);
            switch (event.type) {

                case MediaPlayer.Event.Opening:
                    map.put("event", "opening");
                    channel.invokeMethod("update", map);
                    break;

                case MediaPlayer.Event.Paused:
                    map.put("event", "paused");
                    channel.invokeMethod("update", map);
                    break;

                case MediaPlayer.Event.Stopped:
                    map.put("event", "stopped");
                    channel.invokeMethod("update", map);
                    break;

                case MediaPlayer.Event.Playing:
                    map.put("event", "playing");
                    map.put("height", height);
                    map.put("width", width);
                    map.put("speed", mediaPlayer.getRate());
                    map.put("duration", mediaPlayer.getLength());
                    map.put("audioTracksCount", mediaPlayer.getAudioTracksCount());
                    map.put("activeAudioTrack", mediaPlayer.getAudioTrack());
                    map.put("spuTracksCount", mediaPlayer.getSpuTracksCount());
                    map.put("activeSpuTrack", mediaPlayer.getSpuTrack());
                    channel.invokeMethod("update", map);
                    break;

                case MediaPlayer.Event.Vout:
//                    mediaPlayer.getVLCVout().setWindowSize(textureView.getWidth(), textureView.getHeight());
                    break;

                case MediaPlayer.Event.EndReached:
                    map.put("event", "ended");
                    map.put("position", mediaPlayer.getTime());
                    channel.invokeMethod("update", map);
                    break;

                case MediaPlayer.Event.Buffering:
                case MediaPlayer.Event.TimeChanged:
                    map.put("event", "timeChanged");
                    map.put("height", height);
                    map.put("width", width);
                    map.put("speed", mediaPlayer.getRate());
                    map.put("position", mediaPlayer.getTime());
                    map.put("duration", mediaPlayer.getLength());
                    map.put("buffer", event.getBuffering());
                    map.put("audioTracksCount", mediaPlayer.getAudioTracksCount());
                    map.put("activeAudioTrack", mediaPlayer.getAudioTrack());
                    map.put("spuTracksCount", mediaPlayer.getSpuTracksCount());
                    map.put("activeSpuTrack", mediaPlayer.getSpuTrack());
                    map.put("isPlaying", mediaPlayer.isPlaying());
                    channel.invokeMethod("update", map);
                    break;

                case MediaPlayer.Event.EncounteredError:
                    //mediaEventSink.error("500", "Player State got an error.", null);
                    map.put("event", "error");
                    map.put("error", "Player State got an error");
                    channel.invokeMethod("update", map);
                    break;

                case MediaPlayer.Event.RecordChanged:
                    map.put("event", "recording");
                    map.put("isRecording", event.getRecording());
                    map.put("recordPath", event.getRecordPath());
                    channel.invokeMethod("update", map);
                    break;

                case MediaPlayer.Event.LengthChanged:
                case MediaPlayer.Event.MediaChanged:
                case MediaPlayer.Event.ESAdded:
                case MediaPlayer.Event.ESDeleted:
                case MediaPlayer.Event.ESSelected:
                case MediaPlayer.Event.PausableChanged:
                case MediaPlayer.Event.SeekableChanged:
                case MediaPlayer.Event.PositionChanged:
                default:
                    break;
            }
        }
    };

    VlcPlayerController(Context context, Map params, TextureRegistry textureRegistry, final MethodChannel channel) {
        id = params.get("id");
        List<String> options = (List<String>)params.get("options");
        String url = (String)params.get("uri");

        libVLC = new LibVLC(context, options);
        mediaPlayer = new MediaPlayer(libVLC);

        surfaceTextureEntry = textureRegistry.createSurfaceTexture();
        surfaceTexture = surfaceTextureEntry.surfaceTexture();
        surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                for (OnFrameListener listener: onFrameListeners) {
                    listener.onFrame();
                }
            }
        });
        surfaceTexture.setDefaultBufferSize(1024, 1024);

        mediaPlayer.getVLCVout().setWindowSize(1024, 1024);
        mediaPlayer.getVLCVout().setVideoSurface(surfaceTexture);
        mediaPlayer.getVLCVout().attachViews();
        mediaPlayer.setVideoTrackEnabled(true);

        this.channel = channel;

        mediaPlayer.setEventListener(mediaListener);

        mediaPlayer.play(Uri.parse(url));
    }

    public void dispose() {
        libVLC.release();
        mediaPlayer.release();
    }

    public void play() {
        mediaPlayer.play();
    }

    public void pause() {
        mediaPlayer.pause();
    }

    public void stop() {
        mediaPlayer.stop();
    }

    public void seekTo(long time) {
        mediaPlayer.setTime(time);
    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public interface OnFrameListener {
        void onFrame();
    }
    List<OnFrameListener> onFrameListeners = new ArrayList<>();

    void addOnFrameListener(OnFrameListener listener) {
        onFrameListeners.add(listener);
    }

    void removeOnFrameListener(OnFrameListener listener) {
        onFrameListeners.remove(listener);
    }
}
