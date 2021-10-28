package com.neo.another_vlc_player;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import java.util.Map;

import io.flutter.plugin.platform.PlatformView;
import io.flutter.view.TextureRegistry;

public class VlcPlayer implements PlatformView {

    VlcPlayerController controller;

    class PlayerContainer extends RelativeLayout {

        public PlayerContainer(Context context) {
            super(context);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        );

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = widthMeasureSpec;
            int height = heightMeasureSpec;
            if (width >= height) {
                params.width = width;
                int o = (height - width) / 2;
                params.setMargins(0, o, 0, o);
                playerView.setLayoutParams(params);
            } else {
                params = new RelativeLayout.LayoutParams(
                        height, height
                );
                int o = (width - height) / 2;
                params.setMargins(o, 0, o, 0);
                playerView.setLayoutParams(params);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    PlayerView playerView;
    PlayerContainer container;

    VlcPlayer(Context context, VlcPlayerController controller, Map argv) {
        playerView = new PlayerView(context, controller);
        container = new PlayerContainer(context);
        container.addView(playerView);
        playerView.enable();

        this.controller = controller;

    }

    @Override
    public View getView() {
        return container;
    }

    @Override
    public void dispose() {
        playerView.disable();
    }
}
