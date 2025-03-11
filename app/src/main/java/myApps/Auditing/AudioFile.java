package myApps.Auditing;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;

import java.io.File;
import android.media.MediaMetadataRetriever;

class AudioFileExisting {
    private AudioFileExisting(){}
    static final byte BANNED = 0;
    static final byte DOWNLOADED = 1;
    static final byte UNDOWNLOADED = 2;
    static final byte DELETED = 3;
    static final byte LOST = 4;
}
class AudioFileBitrate {
  private AudioFileBitrate(){}
  // 43 86 129, 32 64 96 128, 48 96 144
  // youtube  ,             ,
  static final int LOW = 43;
  static final int MEDIAM = 86;
  static final int HIGH = 129;
}

public class AudioFile implements Externalizable{
  private static final long serialVersionUID = 1L;
  private Byte version = 1;
  String filePath = null;
  int fileIndex = -1;
  AudioSource source = null;
  int existStatus = -1;
  int bitrate = 0; //byte per second
  int duration = 0;
  //static ArrayList<AudioFile> audioFiles = new ArrayList<AudioFile>();

  private AudioFile(String filePath) {
    if (!(new File(filePath)).exists())
      existStatus = AudioFileExisting.LOST;
    this.filePath = filePath;
    fileIndex = AuditingManager.audioFiles.size();
    AuditingManager.audioFiles.add(this);
    existStatus = AudioFileExisting.DOWNLOADED;
    loadAudioMetadata();
  }
//  private AudioFile(AudioSource src) {
//        source = src;
//        existStatus = AudioFileExisting.UNDOWNLOADED;        
//  }
  static AudioFile get(String filePath){
    for (AudioFile af: AuditingManager.audioFiles){
      if (af.filePath != null && af.filePath.equals(filePath)) return af;
    }
    return new AudioFile(filePath);
  }
//    static AudioFile get(AudioSource src) {
//      for (AudioFile af: audioFiles){
//        if (af.source != null && af.source.equals(src)) return af;
//      }
//      AudioFile af = new AudioFile(src);
//      audioFiles.add(af);
//      return af;
//    }
    int getDuration(){
      return duration;
    }
    String getName(){
      if (filePath == null)
        return null;
      String[] sp = filePath.split("/");
      return sp[sp.length-1];
    }
    private void loadAudioMetadata() {
      MediaMetadataRetriever retriever = new MediaMetadataRetriever();
      try {
            retriever.setDataSource(filePath);
//            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
//            String album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
//            String mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            bitrate = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
//            CmdActivity.println("Title: " + title);
//            CmdActivity.println("Artist: " + artist);
//            CmdActivity.println("Album: " + album);
//            CmdActivity.println("Duration (ms): " + duration);
//            CmdActivity.println("MIME Type: " + mimeType);
//            CmdActivity.println("Bitrate: " + bitrate + " bps");
      } catch (Exception e){
          CmdActivity.println(e.toString()+", can not load file`s metadata");
        }finally {
          retriever.release();
        }
    }
  boolean setBitrate(int kb){
    //throw new IncompletedException();
    if (kb*1024 < bitrate) {
      //"mv "+filePath+" _"+filePath;
      //"ffmpeg -i _"+filePath+" -b:a "+kb+"k "+filePath;
      //"rm _"+filePath;
      return true;
    }
    return false;
  }
  void cutIn(){
    //cut will be in final audio
    //throw new IncompletedException();
  }
  void cutOut(){
    //cut will be out of final audio
    //throw new IncompletedException();
  }

  public AudioFile(){}
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeByte(version);
    out.writeUTF(filePath);
    out.writeInt(fileIndex);
    out.writeObject(source);
    out.writeInt(existStatus);
    out.writeInt(bitrate);
    out.writeInt(duration);
  }
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    version = in.readByte();
    if (version == 1){
      filePath = in.readUTF();
      fileIndex = in.readInt();
      source = (AudioSource) in.readObject();
      existStatus = in.readInt();
      bitrate = in.readInt();
      duration = in.readInt();
    }
  }
}
