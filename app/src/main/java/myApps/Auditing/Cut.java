package myApps.Auditing;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.IOException;

public class Cut implements Externalizable{
  private static final long serialVersionUID = 1L;
  private byte version = 1;
  int start, end;

  public Cut() {}
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeByte(version);
    out.writeInt(start);
    out.writeInt(end);
  }
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    version = in.readByte();
    if (version == 1){
      start = in.readInt();
      end = in.readInt();
    }
  }
}
