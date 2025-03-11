package myApps.Auditing;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;

import java.net.URL;

public class AudioSource implements Externalizable{
  private static final long serialVersionUID = 1L;
  private byte version = 1;
  URL url;
  Cut[] cuts = new Cut[0];
  
  public AudioSource(){}
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeByte(version);
    out.writeUTF(url.toString());
    out.writeInt(cuts.length);
    for(Cut cut: cuts)
      out.writeObject(cut);
  }
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    version = in.readByte();
    if (version == 1){
      url = new URL(in.readUTF());
      cuts = new Cut[in.readInt()];
      
      for(int i=0; i<cuts.length; i++)
        cuts[i] = (Cut) in.readObject();
    }
  }
}
