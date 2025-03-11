package myApps.Auditing;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;

public class AudioCut implements Externalizable{
  private static final long serialVersionUID = 1L;
  private byte version = 1;
  int fileIndex = -1;
  int start; int end;

  AudioCut(AudioFile af, int s, int e){
    fileIndex = af.fileIndex;
    start = s; end = e; // first and last;
    if (start <0) start+= af.duration;
    if (end<0) end+= af.duration; //warning may af.duration = -1
  }
  AudioCut(String filePath, int s, int e){
    this(AudioFile.get(filePath), s, e);
  }
  AudioCut(String filePath){
    this(filePath, 0, -1);
  }
  int getDuration(){
    return end-start+1;
  }
  AudioFile getAudioFile(){
    return AuditingManager.audioFiles.get(fileIndex);
  }
  String getFilePath() {
    return getAudioFile().filePath;
  }

  public AudioCut(){}
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeByte(version);
    out.writeInt(fileIndex);
    out.writeInt(start);
    out.writeInt(end);
  }
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    version = in.readByte();
    if (version == 1){
      fileIndex = in.readInt();
      start = in.readInt();
      end = in.readInt();
    }
  }
}
