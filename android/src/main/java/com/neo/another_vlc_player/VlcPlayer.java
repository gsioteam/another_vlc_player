package com.neo.another_vlc_player;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import org.videolan.libvlc.MediaPlayer;

import java.util.Map;

import io.flutter.plugin.platform.PlatformView;
import io.flutter.view.TextureRegistry;

public class VlcPlayer implements PlatformView {

    VlcPlayerController controller;

//    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
//            controller.getMediaPlayer().getVLCVout().setWindowSize(width, height);
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
//            controller.getMediaPlayer().getVLCVout().setWindowSize(width, height);
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
//            return true;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
//
//        }
//    };

//    TextureView textureView;
    PlayerView playerView;
    RelativeLayout container;

//    TextureRegistry.SurfaceTextureEntry textureEntry;

    VlcPlayer(Context context, VlcPlayerController controller, Map argv) {
        playerView = new PlayerView(context, controller);
        container = new RelativeLayout(context);
        container.addView(playerView);
        container.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int width = v.getWidth();
                int height = v.getHeight();
                if (width >= height) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            width, width
                    );
                    int o = (height - width) / 2;
                    params.setMargins(0, o, 0, o);
                    playerView.setLayoutParams(params);
                } else {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            height, height
                    );
                    int o = (width - height) / 2;
                    params.setMargins(o, 0, o, 0);
                    playerView.setLayoutParams(params);
                }
            }
        });
//        textureView = new TextureView(context);
//        textureView.setSurfaceTexture(controller.getSurfaceTexture());

        this.controller = controller;

    }

    @Override
    public View getView() {
//        return textureView;
        return container;
    }

    @Override
    public void dispose() {
        playerView.dispose();
//        textureEntry.release();
    }
}
