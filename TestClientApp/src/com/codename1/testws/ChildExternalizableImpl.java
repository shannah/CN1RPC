// DO NOT MODIFY THIS FILE.  IT HAS BEEN AUTOMATICALLY GENERATED
// CHANGES MAY BE OVERWRITTEN WITHOUT NOTICE
package com.codename1.testws;

import com.codename1.io.Externalizable;
import com.codename1.io.Util;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Override;
import java.lang.String;

public class ChildExternalizableImpl extends ChildExternalizable implements Externalizable {
  @Override
  public String getObjectId() {
    return "com.codename1.testws.ChildExternalizable";
  }

  @Override
  public int getVersion() {
    return 1;
  }

  @Override
  public void externalize(DataOutputStream out) throws IOException {
    out.writeInt(this.childInt);
    out.writeInt(this.someParentInt);
    Util.writeUTF(this.someParentString, out);
    Util.writeObject(this.someParentExternalizable, out);
    out.writeInt(this.someParentIntArray==null?0:this.someParentIntArray.length);
    if (this.someParentIntArray != null) {
      for (int i=0; i<this.someParentIntArray.length; i++) {
        out.writeInt(this.someParentIntArray[i]);
      }
    }
  }

  @Override
  public void internalize(int version, DataInputStream in) throws IOException {
    if (version == 1) {
      this.childInt = in.readInt();
      this.someParentInt = in.readInt();
      this.someParentString = Util.readUTF(in);
      this.someParentExternalizable = (com.codename1.testws.TestExternalizable)Util.readObject(in);
      int len = in.readInt();
      this.someParentIntArray = new int[len];
      if (len>0) {
        for (int i=0; i<len; i++) {
          this.someParentIntArray[i] = in.readInt();
        }
      }
    }
  }
}
