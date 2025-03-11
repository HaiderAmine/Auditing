package myApps.Auditing;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;

import java.util.Arrays;

public class Audio implements Externalizable{
  private static final long serialVersionUID = 1L;
  private byte version = 1;
  int id;
  String title;
  String directory = "";
  AudioCut cuts[] = null;

  Audio (String title, String dir){
    this.title=title;
    this.directory=dir;
    id = AuditingManager.getNewId();
  }
  Audio (String title){
    this(title, "");
  }
  public String toString(){
    return title + " "+Formats.watchFormat(getDuration()/1000,'s');
  }
  int getDuration(){
      int time=0;
      for (AudioCut cut: cuts)
        time += cut.getDuration();
      return time;
  }
  String getDir() {
    if (directory == null || "".equals(directory))
      return ".";
    return directory;
  }
  Audio subAudio(int s, int e) { //[s, e]
    if (s < 0) s += getDuration();
    if (e < 0) e += getDuration();
    if (s < e && e < getDuration()){
      Audio audio = new Audio(this.title);
      int dist = 0;
      int start, end;
      for (int i=0; i<cuts.length; i++){
        if (i==0) dist = cuts[i].start;
        else dist += cuts[i].start-cuts[i-1].end;
        start=cuts[i].start; end=cuts[i].end;
        if (dist+e < start || end < dist+s) continue;
        if (dist+s > start && dist+s <= end)
          start = dist+s;
        if (dist+e >= start && dist+e < end)
          end = dist+e;
        if (start != end)
          audio.appendCut(cuts[i].getFilePath(), start, end);
      }
      return audio;
    }else return null;
  }
  void setTitle(String t) { title =t;  }
  String getTitle() {return title;}

  void appendCut (AudioCut cut) {
    if (cuts == null)
      cuts = new AudioCut[]{cut};
    else {
      cuts = Arrays.copyOf(cuts, cuts.length+1);
      cuts[cuts.length-1] = cut; 
    }
  }
  void appendCut(String filePath, int s, int e) {
    appendCut(new AudioCut(filePath, s, e));
  }
  
  public Audio(){}
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeByte(version);
    out.writeInt(id);
    out.writeUTF(title);
    out.writeUTF(directory);
    out.writeInt(cuts.length);
    for (AudioCut cut: cuts)
      out.writeObject(cut);
  }
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    version = in.readByte();
    if (version == 1){
      id = in.readInt();
      title = in.readUTF();
      directory = in.readUTF();
      cuts = new AudioCut[in.readInt()];
      for(int i=0; i<cuts.length; i++)
        cuts[i] = (AudioCut) in.readObject();
    }
  }
}
