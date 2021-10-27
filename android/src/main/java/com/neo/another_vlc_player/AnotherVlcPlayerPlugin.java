package com.neo.another_vlc_player;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;
import io.flutter.view.TextureRegistry;

/** AnotherVlcPlayerPlugin */
public class AnotherVlcPlayerPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  
  private Map<Object, VlcPlayerController> controllers = new HashMap<>();
  private Context context;
  private TextureRegistry textureRegistry;

  public class VlcPlayerFactory extends PlatformViewFactory {

    public VlcPlayerFactory() {
      super(StandardMessageCodec.INSTANCE);
    }

    @Override
    public PlatformView create(Context context, int viewId, Object args) {
      Map data = (Map)args;
      VlcPlayerController controller = controllers.get(data.get("id"));
      return new VlcPlayer(context, controller, data);
    }

  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "vlc_player/vlc_player_controller");
    channel.setMethodCallHandler(this);

    context = flutterPluginBinding.getApplicationContext();
    textureRegistry = flutterPluginBinding.getTextureRegistry();

    flutterPluginBinding
            .getPlatformViewRegistry()
            .registerViewFactory("vlc_player/vlc_player", new VlcPlayerFactory());
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "init": {
        VlcPlayerController controller = new VlcPlayerController(
                context,
                (Map) call.arguments,
                textureRegistry,
                channel
        );
        controllers.put(controller.id, controller);
        result.success(true);
        break;
      }
      case "dispose": {
        Object id = call.argument("id");
        VlcPlayerController controller = controllers.remove(id);
        if (controller != null) {
          controller.dispose();
        }
        result.success(true);
        break;
      }
      case "play": {
        Object id = call.argument("id");
        VlcPlayerController controller = controllers.get(id);
        if (controller != null) {
          controller.play();
        }
        result.success(true);
        break;
      }
      case "pause": {
        Object id = call.argument("id");
        VlcPlayerController controller = controllers.get(id);
        if (controller != null) {
          controller.pause();
        }
        result.success(true);
        break;
      }
      case "stop": {
        Object id = call.argument("id");
        VlcPlayerController controller = controllers.get(id);
        if (controller != null) {
          controller.stop();
        }
        result.success(true);
        break;
      }
      case "seekTo": {
        Object id = call.argument("id");
        VlcPlayerController controller = controllers.get(id);
        if (controller != null) {
          int time = call.argument("position");
          controller.seekTo(time);
        }
        result.success(true);
        break;
      }
      default:
        result.notImplemented();
        break;
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

}
