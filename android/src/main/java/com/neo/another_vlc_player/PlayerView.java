package com.neo.another_vlc_player;

import android.content.Context;
import android.graphics.SurfaceTexture;
import javax.microedition.khronos.egl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import org.videolan.libvlc.MediaPlayer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class PlayerView extends GLSurfaceView {

    VideoRender mRenderer;
    private SurfaceTexture mSurface;
    VlcPlayerController mController;
    private VlcPlayerController.OnFrameListener onFrameListener = new VlcPlayerController.OnFrameListener() {
        @Override
        public void onFrame() {
            requestRender();
        }
    };

    public PlayerView(Context context, VlcPlayerController controller) {
        super(context);

        setEGLContextClientVersion(2);
        mController = controller;
        mSurface = controller.getSurfaceTexture();
        mRenderer = new VideoRender(context);
        setRenderer(mRenderer);

    }

    public void enable() {
        mController.addOnFrameListener(onFrameListener);
    }

    public void disable() {
        mController.removeOnFrameListener(onFrameListener);
    }

    @Override
    public void onResume() {
        queueEvent(new Runnable(){
            public void run() {
            }});

        super.onResume();
    }

    private class VideoRender
            implements GLSurfaceView.Renderer {
        private String TAG = "VideoRender";

        private static final int FLOAT_SIZE_BYTES = 4;
        private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
        private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
        private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
        private final float[] mTriangleVerticesData = {
                // X, Y, Z, U, V
                -1.0f, -1.0f, 0, 0.f, 0.f,
                1.0f, -1.0f, 0, 1.f, 0.f,
                -1.0f,  1.0f, 0, 0.f, 1.f,
                1.0f,  1.0f, 0, 1.f, 1.f,
        };

        private FloatBuffer mTriangleVertices;

        private final String mVertexShader =
                "uniform mat4 uMVPMatrix;\n" +
                        "uniform mat4 uSTMatrix;\n" +
                        "attribute vec4 aPosition;\n" +
                        "attribute vec4 aTextureCoord;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "void main() {\n" +
                        "  gl_Position = uMVPMatrix * aPosition;\n" +
                        "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                        "}\n";

        private final String mFragmentShader =
                "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 vTextureCoord;\n" +
                        "uniform samplerExternalOES sTexture;\n" +
                        "void main() {\n" +
                        "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                        "}\n";

        private float[] mMVPMatrix = new float[16];
        private float[] mSTMatrix = new float[16];

        private int mProgram;
        private int mTextureID;
        private int muMVPMatrixHandle;
        private int muSTMatrixHandle;
        private int maPositionHandle;
        private int maTextureHandle;

        private int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

        public VideoRender(Context context) {
            mTriangleVertices = ByteBuffer.allocateDirect(
                    mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mTriangleVertices.put(mTriangleVerticesData).position(0);

            Matrix.setIdentityM(mSTMatrix, 0);
        }

        public void onDrawFrame(GL10 glUnused) {
            synchronized(mSurface) {
                mSurface.attachToGLContext(mTextureID);
                mSurface.updateTexImage();
                mSurface.getTransformMatrix(mSTMatrix);

                GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
                GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

                GLES20.glUseProgram(mProgram);
                checkGlError("glUseProgram");

                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);

                mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
                GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                        TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
                checkGlError("glVertexAttribPointer maPosition");
                GLES20.glEnableVertexAttribArray(maPositionHandle);
                checkGlError("glEnableVertexAttribArray maPositionHandle");

                mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
                GLES20.glVertexAttribPointer(maTextureHandle, 3, GLES20.GL_FLOAT, false,
                        TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
                checkGlError("glVertexAttribPointer maTextureHandle");
                GLES20.glEnableVertexAttribArray(maTextureHandle);
                checkGlError("glEnableVertexAttribArray maTextureHandle");

                Matrix.setIdentityM(mMVPMatrix, 0);
//                Matrix.setIdentityM(mSTMatrix, 0);
                GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
                GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
                checkGlError("glDrawArrays");
                GLES20.glFinish();

                mSurface.detachFromGLContext();
            }



        }

        int mWidth = 0, mHeight = 0;

        public void onSurfaceChanged(GL10 glUnused, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            mWidth = width;
            mHeight = height;
//            mController.getMediaPlayer().getVLCVout().setWindowSize(width, height);
        }

        public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
            mProgram = createProgram(mVertexShader, mFragmentShader);
            if (mProgram == 0) {
                return;
            }
            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
            checkGlError("glGetAttribLocation aPosition");
            if (maPositionHandle == -1) {
                throw new RuntimeException("Could not get attrib location for aPosition");
            }
            maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
            checkGlError("glGetAttribLocation aTextureCoord");
            if (maTextureHandle == -1) {
                throw new RuntimeException("Could not get attrib location for aTextureCoord");
            }

            muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            checkGlError("glGetUniformLocation uMVPMatrix");
            if (muMVPMatrixHandle == -1) {
                throw new RuntimeException("Could not get attrib location for uMVPMatrix");
            }

            muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
            checkGlError("glGetUniformLocation uSTMatrix");
            if (muSTMatrixHandle == -1) {
                throw new RuntimeException("Could not get attrib location for uSTMatrix");
            }


            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);

            mTextureID = textures[0];
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
            checkGlError("glBindTexture mTextureID");

            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);

            /*
             * Create the SurfaceTexture that will feed this textureID,
             * and pass it to the MediaPlayer
             */

//            mMediaPlayer.getVLCVout().setVideoSurface(mSurface);
//            Surface surface = new Surface(mSurface);
//            mMediaPlayer.setSurface(surface);
//            surface.release();
//
//            try {
//                mMediaPlayer.prepare();
//            } catch (IOException t) {
//                Log.e(TAG, "media player prepare failed");
//            }


//            if (!mMediaPlayer.getVLCVout().areViewsAttached()) {
//                mMediaPlayer.getVLCVout().attachViews();
//                mMediaPlayer.setVideoTrackEnabled(true);
//            }

//            mMediaPlayer.start();
        }

        private int loadShader(int shaderType, String source) {
            int shader = GLES20.glCreateShader(shaderType);
            if (shader != 0) {
                GLES20.glShaderSource(shader, source);
                GLES20.glCompileShader(shader);
                int[] compiled = new int[1];
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
                if (compiled[0] == 0) {
                    Log.e(TAG, "Could not compile shader " + shaderType + ":");
                    Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                    GLES20.glDeleteShader(shader);
                    shader = 0;
                }
            }
            return shader;
        }

        private int createProgram(String vertexSource, String fragmentSource) {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
            if (vertexShader == 0) {
                return 0;
            }
            int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
            if (pixelShader == 0) {
                return 0;
            }

            int program = GLES20.glCreateProgram();
            if (program != 0) {
                GLES20.glAttachShader(program, vertexShader);
                checkGlError("glAttachShader");
                GLES20.glAttachShader(program, pixelShader);
                checkGlError("glAttachShader");
                GLES20.glLinkProgram(program);
                int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
                if (linkStatus[0] != GLES20.GL_TRUE) {
                    Log.e(TAG, "Could not link program: ");
                    Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                    GLES20.glDeleteProgram(program);
                    program = 0;
                }
            }
            return program;
        }

        private void checkGlError(String op) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                Log.e(TAG, op + ": glError " + error);
                throw new RuntimeException(op + ": glError " + error);
            }
        }

    }
}
