
import 'dart:io';

import 'package:another_vlc_player/src/vlc_player_controller.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

class VlcPlayer extends StatefulWidget {
  final VlcPlayerController controller;
  final Set<Factory<OneSequenceGestureRecognizer>>? gestureRecognizers;

  VlcPlayer({
    Key? key,
    required this.controller,
    this.gestureRecognizers,
  }) : super(key: key);

  @override
  State<StatefulWidget> createState() => _VlcPlayerState();
}

class _VlcPlayerState extends State<VlcPlayer> {
  static const String viewType = "vlc_player/vlc_player";

  @override
  Widget build(BuildContext context) {
    Map params = {
      "id": widget.controller.id
    };
    if (Platform.isAndroid) {
      return PlatformViewLink(
        viewType: viewType,
        surfaceFactory:
            (BuildContext context, PlatformViewController controller) {
          return AndroidViewSurface(
            controller: controller as AndroidViewController,
            gestureRecognizers: widget.gestureRecognizers ?? const <Factory<OneSequenceGestureRecognizer>>{},
            hitTestBehavior: PlatformViewHitTestBehavior.opaque,
          );
        },
        onCreatePlatformView: (PlatformViewCreationParams platformParams) {
          return PlatformViewsService.initSurfaceAndroidView(
            id: platformParams.id,
            viewType: viewType,
            layoutDirection: TextDirection.ltr,
            creationParams: params,
            creationParamsCodec: StandardMessageCodec(),
          )
            ..addOnPlatformViewCreatedListener(platformParams.onPlatformViewCreated)
            ..create();
        },
      );
    } else if (Platform.isIOS) {
      return UiKitView(
        viewType: viewType,
        layoutDirection: TextDirection.ltr,
        creationParams: params,
        creationParamsCodec: const StandardMessageCodec(),
        gestureRecognizers: widget.gestureRecognizers,
      );
    } else {
      throw Exception("Not support platform");
    }
  }
}