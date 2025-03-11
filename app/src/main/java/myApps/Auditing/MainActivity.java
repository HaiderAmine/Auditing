package myApps.Auditing;

import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;


import java.io.File;
//import java.nio.file.Paths;
//import java.nio.file.Path;
import java.nio.file.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;


import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.ObjectInput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.net.URL;

import android.media.MediaPlayer;
import android.media.AudioManager;


import android.speech.tts.TextToSpeech;
import java.util.Locale;

import android.app.PictureInPictureParams;
import android.content.res.Configuration;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class MainActivity extends Activity {
  private static final int PERMISSION_REQUEST_CODE = 101;
    // storage management req code;
  public static MainActivity mainActivity;
  public static Intent cmdActivity;
  public static Intent directoryActivity;
  public static Handler events;
  public static TextView fileNameView;
  public static TextView durationView;
  public static Button disView;
  
  public static final String BASE_PATH = "/storage/emulated/0/";
  static boolean isCmdsLoaded = false;
  static TextToSpeech ttsEN;
  static TextToSpeech ttsAR;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    try{
      super.onCreate(savedInstanceState);
      setContentView(R.layout.layout_main);
      mainActivity = this;
      cmdActivity = new Intent(this, CmdActivity.class);
      directoryActivity = new Intent(this, DirectoryActivity.class);
      events = new Handler(Looper.getMainLooper());
  
      fileNameView = findViewById(R.id.filename_text);
      durationView = findViewById(R.id.text_duration);
      disView = findViewById(R.id.distance_button);
         
      if ( !(new File(AuditingManager.pathAuditing)).exists() )
        (new File(AuditingManager.pathAuditing)).mkdir();

      findViewById(R.id.button_select_audio).setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v){
          Directory<Audio> dir = new Directory<>("");
          if (AuditingManager.dir != null)
            dir = AuditingManager.dir;
          DirectoryActivity.openDirectoryActivity(mainActivity, dir, new Runnable(){
            @Override
            public void run(){
              Audio audio = (Audio) (DirectoryActivity.currentDir.getItem(DirectoryActivity.selectedItemIndex));
              fileNameView.setText(audio.title);
              AudioPlayer.playAudio(audio);
            }
          });
        }
      });
      findViewById(R.id.button_open_cmd).setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v){
          mainActivity.startActivity(cmdActivity);
        }
      });
      findViewById(R.id.button_open_youtube).setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View v){
//            mainActivity.runOnUiThread(() -> {
          Directory<Youtube.Video> dir = new Directory<>("");
          DirectoryActivity.openDirectoryActivity(mainActivity, dir, new Runnable(){
            @Override
            public void run(){
              Youtube.Video video = (Youtube.Video) (DirectoryActivity.currentDir.getItem(DirectoryActivity.selectedItemIndex));
              CmdActivity.println("you choose video: "+video.toString());
            }
          }, new Runnable(){
            @Override
            public void run(){
              Toast.makeText(mainActivity, "loading from youtube", Toast.LENGTH_SHORT).show();
              new Thread ( ()-> {
                try{
                  //dir.clear()
                  Youtube.YoutubeObject[] items = new Youtube.Search(DirectoryActivity.searchBox.getText().toString()).getItems();
                  for (Youtube.YoutubeObject item :items){
                    if ("video".equals(item.kind)){
                      dir.addItem((Youtube.Video) item);
                    }
                    else if ("playlist".equals(item.kind)){
                      dir.addDir(((Youtube.Playlist) item).id);
                    }
                  }
                  events.post(() -> {
                    Toast.makeText(mainActivity, "loaded", Toast.LENGTH_SHORT).show();
                    DirectoryActivity.reload();
                  });
                } catch (Exception e) {mainActivity.runOnUiThread(() -> {CmdActivity.println(e.toString());});}
              }).start();
            }
          });
        }
      });
      findViewById(R.id.distance_button).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v){
          Button self = (Button) v;
          if (self.getText().toString().equals("5")){
            self.setText("30"); }
          else if (self.getText().toString().equals("30"))
            {self.setText("300");}
          else {self.setText("5");}
        }
      });
      findViewById(R.id.forward_button).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v){
          if (AudioPlayer.isFree)
            return;
          int pos = AudioPlayer.getCurrentTime();
          int limit = AudioPlayer.getDuration();
          int dis = Integer.parseInt(MainActivity.disView.getText().toString());
          pos = pos + 1000*dis;
          if (pos > AudioPlayer.getDuration())
            pos = AudioPlayer.getDuration();
          AudioPlayer.setTime(pos);
        }
      });
      findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v){
          if (AudioPlayer.isFree)
            return;
          int pos = AudioPlayer.getCurrentTime();
          int dis = Integer.parseInt(MainActivity.disView.getText().toString());
          pos = pos - 1000*dis;
          if (pos < 0)
            pos = 0;
          AudioPlayer.setTime(pos);
         }
      });
      findViewById(R.id.pause_button).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v){
          Button self = (Button) v;
          if (self.getText().toString().equals("pause")){
            AudioPlayer.pause();
            self.setText("play");
          }
          else {
            AudioPlayer.play();
            self.setText("pause");
            //events.post(MainActivity.news);
          }
        }
      });
      findViewById(R.id.button_point).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v){
          if (!AudioPlayer.isFree){
            CmdActivity.println("\""+fileNameView.getText() +"\" id: "+AudioPlayer.audio.id+" point: " + AudioPlayer.getCurrentTime());
          }
        }
      });

//      if (checkSelfPermission(android.Manifest.permission.MANAGE_EXTERNAL_STORAGE)
//           != PackageManager.PERMISSION_GRANTED) {
//        requestPermissions(new String[]{android.Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 
//          PERMISSION_REQUEST_CODE);
//      }
      if (!Python.isStarted()) {
        Python.start(new AndroidPlatform(mainActivity));
      }
      if (!isCmdsLoaded) {
        loadCmds();
        AuditingManager.load();
      }
    } catch (Exception e) { CmdActivity.println("Exception On MainActivitu:  "+e.toString());}
  }
  static void loadCmds(){
    isCmdsLoaded = true;
    try {
      CmdActivity.addCmd("show-audios", new Executable(){
        @Override
        public void run(String args[], String targs[]){
          for (Audio audio: AuditingManager.audios){
            CmdActivity.println("'"+audio.title+"' id:"+ audio.id);
          }
        }
      });
      CmdActivity.addCmd("show-files", new Executable(){
        @Override
        public void run(String args[], String targs[]){
          ArrayList<AudioFile> audioFiles = AuditingManager.audioFiles;
          String lines = audioFiles.size()+" file(s)";
          for (int i=0; i<audioFiles.size(); i++){
             lines += "\n"+audioFiles.get(i).filePath+String.format(" %d",audioFiles.get(i).bitrate);
          }
          CmdActivity.print(lines);
        }
      });
      CmdActivity.addCmd("pause", new Executable(){
        @Override
        public void run(String args[], String targs[]){
          AudioPlayer.pause();
          CmdActivity.keepOnINbox("play");
        }
      });
      CmdActivity.addCmd("add-file (move|copy) path:<str>", new Executable(){
        @Override
        public void run(String args[], String targs[]){
          try {
            boolean copy = true;
            if (args[0].equals("move"))
              copy = false;
            AuditingManager.addFile(CmdActivity.PWD+args[1], copy);
            CmdActivity.println("added");
          } catch (IOException e) { CmdActivity.println(e.toString()); }
        }
      });
      CmdActivity.addCmd("add-audio", new Executable(){
        @Override
        public void run(String args[], String targs[]){
          CmdActivity.openTextualDataActivity(new Runnable (){
            @Override
            public void run(){
              try {
                AuditingManager.newAudio(TextualDataActivity.data);
                CmdActivity.println("done");
              } catch (TextualDataException e) {
                CmdActivity.println(e.toString());
              }
            }
          });
        }
      });
      CmdActivity.addCmd("edit-audio (id:<num>|title:<str>)", new Executable(){
        @Override
        public void run(String args[], String targs[]){
          Audio audio;
          if (targs[0].equals("<str>"))
            audio = findAudio(args[0]);
          else
            audio = findAudio(Integer.parseInt(args[0]));
          if (audio == null) return;
          String data = AuditingManager.audioData(audio);
          CmdActivity.openTextualDataActivity(data, new Runnable (){
            @Override
            public void run(){
              try {
                AuditingManager.editAudio(audio, TextualDataActivity.data);
              } catch (TextualDataException e) {
                CmdActivity.println(e.toString());
              }
            }
          });
        }
      });
      CmdActivity.addCmd("play (id:<num>|title:<str>) [s:<num> [e:<num>]]",new Executable(){
        @Override
        public void run(String args[], String targs[]){
          int s = 0;
          int e = -1;
          if (args.length >= 2)
            s = Integer.parseInt(args[1]);
          if (args.length >= 3)
            e = Integer.parseInt(args[2]);
          if (args.length == 0){
            if (AudioPlayer.isPause){
              AudioPlayer.play();
              CmdActivity.keepOnINbox("pause");
            } else {
              CmdActivity.println("there is no audio loaded");
            }
            return;
          }
          Audio audio;
          if (targs[0].equals("<str>"))
            audio = findAudio(args[0]);
          else
            audio = findAudio(Integer.parseInt(args[0]));
          if (audio == null) return;
          if (s == 0 && e == -1)
            CmdActivity.println("you can not use s and e  yet");
            //AudioPlayer.playAudio(audio);
          else
            CmdActivity.println("you can not use s and e  yet");
            //AudioPlayer.playAudio(audio, s, e);
          CmdActivity.print("playing: <"+audio.title+">\n");
          CmdActivity.keepOnINbox("pause");
          events.post(new Runnable(){
            @Override
            public void run(){
              try{
                int [] pos = AudioPlayer.displayPos;
                if (!AudioPlayer.isFree){
                  if (pos[0] > 0) AudioPlayer.displayPos = CmdActivity.print(Formats.watchFormat(AudioPlayer.getCurrentTime()/1000,'s')+"->"+AudioPlayer.getDuration()+"\n", pos[0], pos[1]);
                  else AudioPlayer.displayPos = CmdActivity.print(Formats.watchFormat(AudioPlayer.getCurrentTime()/1000,'h')+"->"+AudioPlayer.getDuration()+"\n");
                  events.post(this);
                }else {
                  if (pos[0] > 0)
                    CmdActivity.println("END.",pos[0],pos[1]);
                  AudioPlayer.displayPos = new int[]{-1, -1};
                  if ("pause".equals(CmdActivity.IN_text))
                    CmdActivity.keepOnINbox("");
                }
              }catch(Exception e) {CmdActivity.print("Exception in Exe(aplay, ("+args+","+targs+") )["+e.toString()+"]");}
            }
          });
        }
      });
      CmdActivity.addCmd("delete-audio id:<num>", new Executable(){
        @Override
        public void run(String args[], String targs[]){
          AuditingManager.deleteAudio(Integer.parseInt(args[0]));
          CmdActivity.println("done");
        }
      });
      CmdActivity.addCmd("youtube-vd id:<str>", new Executable(){
        @Override
        public void run(String args[], String targs[]){
          Python py = Python.getInstance();
          try{
            String result = py.getModule("youtube").callAttr("downloadVideo", args[0]).toString();
            CmdActivity.println(result);
          } catch (Exception e) {
            CmdActivity.println("Exception on chaqopy: "+e.toString());
          }
        }
      });
      CmdActivity.addCmd("youtube-search title:<str>", new Executable(){
        @Override
        public void run(String args[], String targs[]){
          new Thread(() -> {
            try{
              Youtube.YoutubeObject[] objItems = new Youtube.Search(args[0]).getItems();
              //ArrayList<String> items = new ArrayList<>();
              ArrayList<String> itemsId = new ArrayList<>();
              String text = "";
              for (Youtube.YoutubeObject obj: objItems){
                if ("video".equals(obj.kind)){
                  itemsId.add(((Youtube.Video) obj).id);
                  text += "video: "+((Youtube.Video) obj).id +"\n";
                }
                  else if ("playlist".equals(obj.kind)){
                    itemsId.add(((Youtube.Playlist) obj).id);
                    text += "playlist: "+((Youtube.Playlist) obj).id + "\n";
                  }
                }
                final String _text = text;
                mainActivity.runOnUiThread(() -> {
                  // Code to update UI
                  CmdActivity.print(_text);
                });
              } catch (Exception e) {
               CmdActivity.println("Exception on Youtube API: "+e.toString());
              }
          }).start();
        }
      });
      CmdActivity.addCmd("speech text:<str>", new Executable(){
        @Override
        public void run(String args[], String targs[]){
          if (ttsEN==null){
            CmdActivity.println("wait once for preparing speeker");
//            ttsAR = new TextToSpeech(mainActivity, new TextToSpeech.OnInitListener() {
//              @Override
//              public void onInit(int status) {
//                if (status == TextToSpeech.SUCCESS) {
//                  ttsAR.setLanguage(Locale.US);
//                  ttsAR.speak("", TextToSpeech.QUEUE_FLUSH, null, null);
//                }
//              }
//            });
            ttsEN = new TextToSpeech(mainActivity, new TextToSpeech.OnInitListener() {
              @Override
              public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                  ttsEN.setLanguage(Locale.US);
                  ttsEN.speak(args[0], TextToSpeech.QUEUE_FLUSH, null, null);
                }
              }
            });
            //CmdActivity.exe("speech \""+args[0]+"\"");
          }
          else
            ttsEN.speak(args[0], TextToSpeech.QUEUE_FLUSH, null, null);
        }
      });
      CmdActivity.addCmd("speech-save file:<str> text:<str>", new Executable(){
        @Override
        public void run(String args[], String targs[]){

        }
      });
    }catch (Exception e) {CmdActivity.println(e.toString());}
  }
  static Audio findAudio(String title){
      Audio[] audios = AuditingManager.findAudio(title);
      if(audios == null){
        CmdActivity.print("no audio has title: '"+title+"'\n");
        return null;
      }
      else if (audios.length != 1){
        CmdActivity.println("there are "+audios.length+" audios have that title");
        return null;
      } else {
        return  audios[0];
      }
  }
  static Audio findAudio(int id){
      Audio audio = AuditingManager.findAudioById(id);
      if (audio == null){
        CmdActivity.println("no audio has id:"+id);
        return null;
      } else return audio;
  }

  public void enterPipMode(View view) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
      enterPictureInPictureMode(pipBuilder.build());
    }
  }

  @Override
  public void onConfigurationChanged(/*@NonNull*/ Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
       if (isInPictureInPictureMode()) {
                // Handle PiP mode UI changes
       }
    }
  }
  @Override
  public void onPictureInPictureModeChanged(boolean isInPiPMode, Configuration newConfig) {
    super.onPictureInPictureModeChanged(isInPiPMode, newConfig);
    if (isInPiPMode) {
        // Hide UI elements for PiP mode
//      findViewById(R.id.linear_layout_1).setVisibility(View.GONE);
//      findViewById(R.id.linear_layout_2).setVisibility(View.GONE);
//      findViewById(R.id.linear_layout_1).setVisibility(View.GONE);
    } else {
        // Show UI elements when returning to normal mode
    }
  }
/* work but not need it.
  @Override
  protected void onUserLeaveHint() {
    super.onUserLeaveHint();
    enterPipMode(findViewById(R.id.btnPip));
  }
*/
}

class IncompletedException extends RuntimeException {}

class AudioPlayer{
  public static MediaPlayer mp = null;
  public static Audio audio;
  public static int[] displayPos = new int[]{-1, -1};
  public static int cutIndex = 0;
  static boolean isFree = true;
  static boolean isCutEnd = false;
  public static boolean isPause = false;
  
  private static int startPoint = 0;
  private static int endPoint = -1;
  private static boolean isLooping = false;
  private static int loopLeft = 0;
  private static int sLoop = 0;
  private static int eLoop = -1;
  private static Runnable progress = new Runnable(){
    @Override
    public void run() {
      try{
        if (!AudioPlayer.mp.isPlaying() && !isPause){
          cutIndex++;
          if (cutIndex < audio.cuts.length) {
               MainActivity.events.post(this);
               playCut(audio.cuts[cutIndex]);
          } else isFree = true;
        }
        else if (!isPause){
          MainActivity.durationView.setText(Formats.watchFormat(getCurrentTime()/1000,'s')+" -> "+Formats.watchFormat(audio.getDuration()/1000,'s'));
          if (isLooping && getCurrentTime() >= eLoop) {
            loopLeft --;
            setTime(sLoop);
            if (loopLeft == 0) isLooping = false;
          }
          else if (AudioPlayer.mp.getCurrentPosition() >= AudioPlayer.endPoint){
             AudioPlayer.mp.pause();
             cutIndex++;
             if (cutIndex < audio.cuts.length) {
               MainActivity.events.post(this);
               playCut(audio.cuts[cutIndex]);
             }else isFree = true;
          }
          else{MainActivity.events.post(this);}
        }
        else{MainActivity.events.post(this);}
      }catch(Exception e){CmdActivity.println(e.toString());}
    }
        };
  public static int getCurrentTime(){
    if (!isFree){
      int time = 0;
      for (int i=0; i<cutIndex; i++){
        time += audio.cuts[i].getDuration();
      }
      time += mp.getCurrentPosition()-audio.cuts[cutIndex].start;
      return time;
    }else return 0;
  }
  public static int getDuration(){
    if (!isFree) return audio.getDuration();
    else return 0;
  }
  public static boolean setTime(int ms){ // >= 0
    if (isFree || ms < 0 || ms >= getDuration()) 
      return false;
    for (int i=0; i<audio.cuts.length; i++){
      if (ms > audio.cuts[i].getDuration())
        ms -= (audio.cuts[i].getDuration());
      else {
        cutIndex = i;
        playCut(audio.cuts[i], ms);
        break;
      }
    }
    return true;
  }
  // cut: seperate part; using it on loops
  // play part can exit out of part bounds
  private static void playCut(AudioCut cut) {
    playCut(cut, 0);
  }
  private static void playCut(AudioCut cut, int s){
    try {
      if (isFree)
        throw new Exception("you select Cut to play but media player is steel free");
      if (mp == null) {
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_ALARM);
      }
      mp.reset();
      mp.setDataSource(cut.getFilePath());
      mp.prepare();
      mp.seekTo(cut.start+s);


      isPause = false;
      if (isCutEnd) {
        isCutEnd = false;
        MainActivity.events.post(new Runnable() {
          @Override
          public void run() {
            try{
              AudioCut thisCut = AudioPlayer.audio.cuts[AudioPlayer.cutIndex];
              int time = mp.getCurrentPosition();

              if (thisCut.start > time || time >= thisCut.end) {
                mp.pause();
                AudioPlayer.isCutEnd = true;
              }
              else {
                MainActivity.events.post(this);
                MainActivity.durationView.setText(Formats.watchFormat(getCurrentTime()/1000,'s')+" -> "+Formats.watchFormat(audio.getDuration()/1000,'s'));
              }
            } catch(Exception e) {
              CmdActivity.println(e.toString());
            }
          }
        });
      }

      mp.start();
    } catch (Exception e) {
      CmdActivity.println(e.toString());
    }
  }
  public static void playAudio(Audio audio){
    AudioPlayer.audio = audio;
    cutIndex = -1;
    isLooping = false;
    isCutEnd = true;
    if (isFree) {
      isFree = false;
      MainActivity.events.post(new Runnable() {
        @Override
        public void run() {
          try{
            if (AudioPlayer.isCutEnd){
              AudioPlayer.cutIndex++;
            }
            if (AudioPlayer.cutIndex < AudioPlayer.audio.cuts.length){
              if (AudioPlayer.isCutEnd){
                playCut(AudioPlayer.audio.cuts[cutIndex]);
              }
                //after play cut:
              MainActivity.events.post(this);
            }
            else {
              AudioPlayer.isFree = true;
            }
          } catch(Exception e) {
            CmdActivity.println(e.toString());
          }
        }
      });
    }
  }
//  public static void loopAudio(int s, int e){
//    if (!isFree){
//      sLoop = s; eLoop = e;
//      if (sLoop < 0) sLoop += getDuration();
//      if (eLoop < 0) eLoop += getDuration();
//      if (sLoop < eLoop && eLoop < getDuration()){
//        isLooping = true;
//      }
//    }
//  }
//  public static void loopAudio(Audio ad, int s, int e, int time){
//    sLoop = s; eLoop = e;
//    if (sLoop < 0) sLoop += getDuration();
//    if (eLoop < 0) eLoop += getDuration();
//    if (sLoop < eLoop && eLoop < ad.getDuration()){
//      if (time != 0){
//        isLooping = true;
//        loopLeft = time;
//      }
//      audio = ad.subAudio(s, e);
//      cutIndex = 0;
//      if (cutIndex<audio.cuts.length) playCut(audio.cuts[0]);
//    }
//  }
  public static void pause() {
    if (mp != null && !isFree) {
      mp.pause();
      isPause = true;
    }
  }
  public static void play() {
    if (mp != null && !isFree) {
      mp.start();
      isPause = false;
    }
   else if( audio != null) {
     playAudio(audio);
   }
  }
}
class TextualDataException extends Exception{
  TextualDataException(String message) {
    super(message);
  }
}
//class Directory{
//  String name;
//  ArrayList<String> files = new ArrayList<String>();
//  ArrayList<Directory> directories = new ArrayList<Directory>();
//
//  Directory(String name){
//    this.name = name;
//  }
//  Directory getDirectory(String dirName){
//    for (Directory dir: directories){
//      if (dir.name.equals(dirName))
//        return dir;
//    }
//    return null;
//  }
//}
class AuditingManager {
  public static final String pathAuditing = MainActivity.BASE_PATH+"auditing/";
  static ArrayList<AudioFile> audioFiles = new ArrayList<AudioFile>();
  static ArrayList<Audio> audios = new ArrayList<Audio>();
  static Directory<Audio> dir = new Directory<>("");
  private static boolean isLoaded = false;
  private static int unusedId = 0;

  static void load(){
    //throw new IncompletedException();
    ArrayList<Object> AudioFileObjs = FileManager.readBytes(pathAuditing+"AudioFile.bin");
    if (AudioFileObjs != null) {
      audioFiles.clear();
      for (Object obj: AudioFileObjs){
        audioFiles.add((AudioFile)obj);
      }
    }
    ArrayList<Object> AudioObjs = FileManager.readBytes(pathAuditing+"Audio.bin");
    if (AudioObjs != null) {
      audios.clear();
      dir = new Directory<Audio>("");
      Audio audio;
      Directory<Audio> d;
      for (Object obj: AudioObjs){
        audio = (Audio)obj;
        audios.add(audio);
        if (audio.id >= unusedId) unusedId = audio.id+1;
        d = dir;
        if (!audio.getDir().equals(".")){
          for (String dirName: audio.directory.split("/")){
            if (null == d.getChildDir(dirName))
               d.addDir(dirName);
            d = d.getChildDir(dirName);
            if (d==null)
              CmdActivity.println("now is null");
            // error here ^ (null)
          }
        }
        d.addItem(audio);
      }
    }
    isLoaded = true;
  }
  private static void saveAudioFiles() {
    if (FileManager.writeBytes(pathAuditing+"AudioFile.bin", null)){
      for (AudioFile obj: audioFiles){
        FileManager.appendBytes(pathAuditing+"AudioFile.bin", obj);
      }
    }
  }
  private static void saveAudios(){
    if (FileManager.writeBytes(pathAuditing+"Audio.bin", null)){
      for (Audio obj: audios){
        FileManager.appendBytes(pathAuditing+"Audio.bin", obj);
      }
    }
  }
  private static void save() { // after multi-changes
    saveAudioFiles();
    saveAudios();
  }
  static int getNewId() {
    if (!isLoaded) load();
    return unusedId++;
  }
  static void addFile(String filePath, boolean copy) throws IOException{
    /*
     mv or copy to auditing file and create new Audio obj
    */
    Path src = Paths.get(filePath);
    if (!Files.exists(src))
      throw new IOException("file '"+filePath+"' not exist");
    else if (Files.isDirectory(src))
      throw new IOException("'"+filePath+"' is directory");
    Path dest = Paths.get(pathAuditing+src.getFileName().toString());
    if (Files.exists(dest)) {
      String name_ext[] = dest.getFileName().toString().split("\\.");
      int n = 1;
      if (name_ext.length == 1){
        while (Files.exists(Paths.get(pathAuditing+name_ext[0]+" ("+n+")")))
          n++;
        dest = Paths.get(pathAuditing+name_ext[0]+" ("+n+")");
      }
      else {
        while (Files.exists(Paths.get(pathAuditing+name_ext[0]+" ("+n+")."+name_ext[1])))
          n++;
        dest = Paths.get(pathAuditing+name_ext[0]+" ("+n+")."+name_ext[1]);
      }
    }
    if (copy)
      Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
    else
      Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
    Audio audio = new Audio(dest.getFileName().toString());
    audio.appendCut(new AudioCut(dest.toAbsolutePath().toString()));
    audios.add(audio);
    FileManager.appendBytes(pathAuditing+"AudioFile.bin",AudioFile.get(dest.toAbsolutePath().toString()));
    FileManager.appendBytes(pathAuditing+"Audio.bin",audio);
  }
  
  static Audio createAudio(String textualData) throws TextualDataException{
    /*
      add to audios and return it
    */
    String[] lines = textualData.split("\n");
    if (lines.length < 2) throw new TextualDataException("not enough data");
    String init[] = CmdActivity.splitInput(lines[0]);
    String title = init[0];
    String dir = "";
    if (init.length==3 && !init[2].equals(".")){
      dir = init[2];
      CmdActivity.println("new dir: "+dir);
    }
    AudioFile audioFile = null;
    boolean isAudioFileUsed = true; // for: if he do not specified <Time>(s)
    ArrayList<AudioCut> cuts = new ArrayList<AudioCut>();
    String line;
    for (int i=1; i<lines.length; i++){
      line = lines[i];
      if (line.equals("")) continue;
      if (line.charAt(0) != ' ') throw new TextualDataException("add ' ' on L:"+i+" C:0");
      if (line.charAt(1) != ' ') {
        if (!isAudioFileUsed)
            cuts.add(new AudioCut(audioFile, 0, -1));
        audioFile = AudioFile.get(pathAuditing+line.substring(1, line.length()));
        isAudioFileUsed = false;
      }
      else if (line.charAt(2) != ' ') {
        if (audioFile == null) throw new TextualDataException("found cut time with out file on l:"+i);
        isAudioFileUsed = true;
        String[] t = line.substring(2, line.length()).split(" ");
        if (t.length != 2) throw new TextualDataException("syntext error, write as '  <Time> <Time>'");
        int s, e;
        s = Integer.parseInt(t[0]);
        if (t[1].equals("END")) e = audioFile.getDuration()-1;
        else e = Integer.parseInt(t[1]);
        cuts.add(new AudioCut(audioFile, s, e));
      }
      else throw new TextualDataException("incorrect line:"+i);
    }
    if (!isAudioFileUsed)
        cuts.add(new AudioCut(audioFile, 0, -1));
    Audio audio = new Audio(title, dir);
    for (AudioCut cut : cuts)
        audio.appendCut(cut);
    return audio;
  }
  static void newAudio(String textualData) throws TextualDataException {
    Audio audio = createAudio(textualData);
    audios.add(audio);
    FileManager.appendBytes(pathAuditing+"Audio.bin",audio);
  }
  static void editAudio(Audio audio, String textualData) throws TextualDataException{
    Audio _audio = createAudio(textualData);
    audio.directory = _audio.getDir();
    audio.cuts = _audio.cuts;
    saveAudios();
  }
  static String audioData(Audio audio){
    String data = "\""+audio.title+"\""+" in "+audio.getDir();
    for (AudioCut cut: audio.cuts){
      data+= "\n "+cut.getAudioFile().getName();
      data+= "\n  "+cut.start+" "+cut.end;
    }
    return data;
  }
  static Audio[] findAudio(String title){
    ArrayList<Audio> auds = new ArrayList<Audio>();
    for (Audio audio: audios){
      if (audio.title.equals(title))
           auds.add(audio);
    }
    if (auds.size()>0){
        Audio[] filtered = auds.toArray(new Audio[auds.size()]);
        return filtered;
    } else return null;
  }
  static Audio findAudioById(int id){
    for(Audio audio: audios){
      if(audio.id == id) return audio;
    }
    return null;
  }
  static void deleteAudio(int id){
    int i = 0;
    while (i<audios.size() && audios.get(i).id != id)
       i++;
    if (i >= audios.size()) return;
    audios.remove(i);
    FileManager.writeBytes(pathAuditing+"_Audio.bin", null);
    //ArrayList<AudioCut> neighborAudios = new ArrayList<>();
    for (Audio a: audios) {
      FileManager.appendBytes(pathAuditing+"_Audio.bin", a);
    }
    FileManager.replaceFile(pathAuditing+"_Audio.bin", pathAuditing+"Audio.bin");
  }
  private static void deleteAudioFile(int fileIndex){
    audioFiles.set(fileIndex, null);
    FileManager.writeBytes(pathAuditing+"_AudioFile.bin", null);
    for (AudioFile af: audioFiles)
      FileManager.appendBytes(pathAuditing+"_AudioFile.bin", af);
    FileManager.replaceFile("_AudioFile.bin", "AudioFile.bin");
  }
  private void shiftAudioFiles(){
//    int i = 0;
//    while (i<audioFiles.size() && !audioFiles.get(i) != null)
//        i++;
//    if (i>= audioFiles.size()) return;
//    
  }
}

class AppendableObjectOutputStream extends ObjectOutputStream {
    public AppendableObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        // Do nothing to avoid writing header again
    }
}
class FileManager{
  private FileManager() {}
  static boolean write(String absulotePath, String data, boolean isAppend){
    try {
      FileOutputStream fos = new FileOutputStream(new File(absulotePath), isAppend);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
      writer.write(data); writer.flush();
      return true;
    } catch (IOException e) { return false; }    
  }
  static boolean write(String absolutePath, String data) {
    return write(absolutePath, data, false);
  }
  static boolean append(String absolutePath, String data) {
    return write(absolutePath, data, true);
  }
  static String readFile(String absulotePath){
    String data = "";
    try {
      FileInputStream fis = new FileInputStream(new File(absulotePath));
      BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
      String line;
      if ((line = reader.readLine()) != null){
        data += line;
      }
      while ((line = reader.readLine()) != null){
        data += "\n" + line;
      }
      return data;
    } catch (IOException e) { return null; }
  }
  static boolean writeBytes(String absulotePath, Object obj) {
    try {
      FileOutputStream fos = new FileOutputStream(new File(absulotePath));
      ObjectOutputStream out = new ObjectOutputStream(fos);
      if (obj != null)
         out.writeObject(obj);
      return true;
    } catch (IOException e) { return false;}
  }
  static boolean appendBytes(String absolutePath, Object obj) {
    if (!(new File(absolutePath)).exists())
       return writeBytes(absolutePath, obj);
    try {
      FileOutputStream fos = new FileOutputStream(new File(absolutePath), true);
      AppendableObjectOutputStream out = new AppendableObjectOutputStream(fos);
      out.writeObject(obj);
      return true;
    } catch (IOException e) { return false;}
  }
  static ArrayList<Object> readBytes(String absulotePath) {
    ArrayList<Object> objs = new ArrayList<Object>();
    try {
      FileInputStream fis = new FileInputStream(new File(absulotePath));
      ObjectInputStream in = new ObjectInputStream(fis);
      while (true) {
        objs.add(in.readObject());
      }
    } catch (EOFException e) {
      return objs;
    } catch (IOException|ClassNotFoundException e) {CmdActivity.println(e.toString()); return null;}
  }
  static boolean replaceFile(String newFile, String oldFile) {
       Path source = Paths.get(newFile);
       Path target = Paths.get(oldFile);
       try {
         Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
       return true;
        } catch (Exception e) {
            CmdActivity.println(e.toString());
            return false;
        }
  }
}
class Formats {
  public static String watchFormat(double time, char unit){
        int hours, minutes, seconds;
        if (unit == 'h'){
            hours = (int) time;
            minutes = (int) (time*60 - hours*60);
            seconds = (int) (time *3600 - hours *3600 - minutes *60);
            if ( (time*3600 -hours*3600 -minutes*60)-seconds > 0.99){
                seconds += 1;
                if (seconds == 60){
                    seconds = 0;
                    minutes += 1;
                }
            }
        }
        else if (unit == 'm'){
                hours = (int) (time / 60);
                minutes = (int) (time - hours*60);
                seconds = (int) (60*(time - hours *60 - minutes));
       }
       else{
                hours = (int) (time / 3600);
                minutes = (int) ((time - hours * 3600)/60);
                seconds = (int) (time - hours * 3600 - minutes * 60);
       } 
    return (String.format("%02d:%02d:%02d", hours, minutes, seconds));
  }
}
   
