
import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'enums/data_source_type.dart';
import 'enums/hardware_acceleration.dart';
import 'utils/options/vlc_player_options.dart';
import 'vlc_player_value.dart';

class VlcPlayerController extends ValueNotifier<VlcPlayerValue> {

  VlcPlayerController.network(
      String dataSource, {
        HwAcc hwAcc = HwAcc.AUTO,
        this.autoPlay = true,
        VlcPlayerOptions? options,
      }) : super(VlcPlayerValue(duration: Duration.zero)) {
    _ready = _setup(
      dataSource: dataSource,
      hwAcc: hwAcc,
      autoPlay: autoPlay,
      dataSourceType: DataSourceType.network,
      options: options,
    );
  }

  static const MethodChannel _channel = MethodChannel("vlc_player/vlc_player_controller");
  static int _idCounter = 0;
  static bool _isListening = false;
  static Map<int, VlcPlayerController> _playerControllers = {};
  late int _id;
  int get id => _id;

  final bool autoPlay;

  late Future<void> _ready;
  Future<void> get ready => _ready;

  Future<void> _setup({
    required String dataSource,
    required HwAcc hwAcc,
    required bool autoPlay,
    required DataSourceType dataSourceType,
    VlcPlayerOptions? options,
    String? package,
  }) async {
    _id = ++_idCounter;
    if (!_isListening) {
      _channel.setMethodCallHandler(_onCall);
      _isListening = true;
    }
    await _channel.invokeMethod("init", {
      "id": _id,
      "uri": dataSource,
      "type": dataSourceType.index,
      "package": package,
      "hwAcc": hwAcc.index,
      "autoPlay": autoPlay,
      "options": options?.get() ?? [],
    });
  }


  @override
  Future<void> dispose() async {
    _playerControllers.remove(_id);
    super.dispose();

    await _channel.invokeMethod("dispose", {
      "id": _id,
    });
  }

  Future<void> play() async {
    await _channel.invokeMethod("play", {
      "id": _id,
    });
  }

  Future<void> pause() async {
    await _channel.invokeMethod("pause", {
      "id": _id,
    });
  }

  Future<void> stop() async {
    await _channel.invokeMethod("stop", {
      "id": _id,
    });
  }

  Future<void> seekTo(Duration position) async {
    await _channel.invokeMethod("seekTo", {
      "id": _id,
      "position": position.inMilliseconds,
    });
  }

  static Future _onCall(MethodCall call) async {
    switch (call.method) {
      case "update": {
        VlcPlayerController? controller = _playerControllers[call.arguments['id']];
        Size? size;
        if (call.arguments["width"] != null && call.arguments["height"] != null) {
          size = Size(call.arguments["width"].toDouble(), call.arguments["height"].toDouble());
        }
        Duration? toDuration(obj) {
          if (obj is int) {
            return Duration(milliseconds: obj);
          }
        }
        if (controller != null) {
          controller.value = controller.value.copyWith(
            duration: toDuration(call.arguments["duration"]),
            size: size,
            position: toDuration(call.arguments["position"]),
            isPlaying: call.arguments["isPlaying"],
            isRecording: call.arguments["isRecording"],
            bufferPercent: call.arguments["buffer"],
            playbackSpeed: call.arguments["speed"],
            audioTracksCount: call.arguments["audioTracksCount"],
            activeAudioTrack: call.arguments["activeAudioTrack"],
            spuTracksCount: call.arguments["spuTracksCount"],
            activeSpuTrack: call.arguments["activeSpuTrack"],
            recordPath: call.arguments["recordPath"],
            errorDescription: call.arguments["error"],
          );
        }
        break;
      }
    }
  }
}