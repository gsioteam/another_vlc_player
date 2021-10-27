import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:another_vlc_player/another_vlc_player.dart';

void main() {
  const MethodChannel channel = MethodChannel('another_vlc_player');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await AnotherVlcPlayer.platformVersion, '42');
  });
}