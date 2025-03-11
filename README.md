# Auditing Project
(Audio Editing)
- [ ] reset AudiringManager. dir after add or delete audio (add-file also add audio)
- [ ] use html , css and javaScript to build android app with java on backend, (using webView, Apache cordova, flutter webView plugin, React native webView)

#### version 1:  (audio manager)
- [x] audio player
- [x] reuse parts from a audio as diffrent audio objects
- [ ] play history
- [x] textual audio making
- [x] logical directory system
- [ ] play list
- [ ] looping subaudio (start|end points),
- [ ] record audio
- [x] text to voice
#### version 2: (FFMPEG)
- [ ] auto (audio cut in, cut out) after unrefrenced part of file
- [x] decreasing audio bitrate (less space uses)
- [ ] change audio volume
```
ffmpeg -i input.mp3 -filter:a "volume=2.0" output.mp3
```
- [ ] change audio speed
```
ffmpeg -i input.mp3 -filter:a "atempo=2.0,atempo=2.0" output.mp3
```
- [ ] process operation if user play audio and the app in forward-ground
#### version 3: (APIs)
- [x] search from youtube
- [x] download from youtube 
- [ ] upload to telegram channel, re-download if need.
#### version 4:
- [ ] change sound volume for each cut
- [ ] فلتر الصدى
- [ ] ffmpeg audio filters
#### version 5: (DEEZER SPLEETER)
- [ ] music remover filter
#### vesion 6: (speech to text)
- [ ] get text from audio
- [ ] translate using text
#### tools:
 - chaquopy (java plugin)
 - FFmpegKit (java plugin)
 - youtube api
 - pytubefix (using python)
 - telegram api (telethon using python)
 - deezer spliter (using pythom)

---

```plain
HH: ≥ 0
<Time> = 1000 /*(ms)*/ | 0:11 | 00:59 | 0:58:1 | 00:03:01 | 0:2:4.0000 | END/*==-1*/ | -89000/*==END-(ms)*/ | -0:11

AUDIO_TITLE
 FILE_NAME_1
 ...

AUDIO_TITLE
 FILE_NAME_1
  <Time> <Time> /*cut in*/
  ...
 ...
AUDIO_TITLE
 !FILE_NAME_1 /*cut out*/
  <Time> <time>
  ...
 FILE_NAME_2
 ...

PLAYLIST_TITLE:
 AUDIO_TITLE_1
 ...

AUDIO_TITLE in .
 FILE_NAME_1
 ...

AUDIO_TITLE in PATH/OF/PLAYLIST
 FILE_NAME_1
 ...

```
---
```java
class AudioPlayer {
  public static int getCurrentTime()
  public static int getDuration()
  public static boolean setTime(int ms)
  public static void playAudio(Audio audio)
  public static void pause()
  public static void play()
}
class AuditingManager() {
  static void load()
  static void addFile(String filePath, boolean copy) throws IOException
  static void newAudio(String textualData) throws TextualDataException
  static void editAudio(Audio audio, String textualData) throws TextualDataException
  static String audioData(Audio audio)
  static Audio[] findAudio(String title)
  static Audio findAudioById(int id)

class FileManager {
  static boolean write(String absulotePath, String data, boolean isAppend)
  static boolean write(String absolutePath, String data)
  static boolean append(String absolutePath, , String data)
  static String readFile(String absulotePath)
  static boolean writeBytes(String absulotePath, Object obj)
  static boolean appendBytes(String absolutePath, Object obj)
  static ArrayList<Object> readBytes(String absolutePath)
  static boolean replaceFile(String newFile, String oldFile)
}
```

---
##### problems and its fix:
- permmision denied

---
Since you want to run **Spleeter separately from Termux** in your Android app, the best approach is to **convert Spleeter’s model to TensorFlow Lite (TFLite)** and integrate it into your Java-based Android app.

### **Steps to Use Spleeter in Android with TensorFlow Lite**

#### **1. Convert Spleeter Model to TFLite**

- Download the pre-trained **Spleeter model** (`2stems`, `4stems`, or `5stems`).
- Convert it to **TFLite** using `tf.lite.TFLiteConverter`.
- Optimize the model for mobile devices (quantization).

#### **2. Load the TFLite Model in Java**

- Use **TensorFlow Lite Interpreter** in Android.
- Process input audio as **spectrograms**.
- Run inference to get separated audio sources.

#### **3. Post-Processing**

- Convert the output spectrograms back to **audio (WAV/MP3)**.
- Merge or save separated stems.

Would you like help with **converting the model** to TFLite, or do you need a **code example for using TFLite in Android**?
 
