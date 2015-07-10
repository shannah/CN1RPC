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

public class TestExternalizableImpl extends TestExternalizable implements Externalizable {
  @Override
  public String getObjectId() {
    return "com.codename1.testws.TestExternalizable";
  }

  @Override
  public int getVersion() {
    return 1;
  }

  @Override
  public void externalize(DataOutputStream out) throws IOException {
    out.writeInt(this.size);
    Util.writeUTF(this.name, out);
    out.writeInt(this.ages==null?0:this.ages.length);
    if (this.ages != null) {
      for (int i=0; i<this.ages.length; i++) {
        out.writeInt(this.ages[i]);
      }
    }
  }

  @Override
  public void internalize(int version, DataInputStream in) throws IOException {
    if (version == 1) {
      this.size = in.readInt();
      this.name = Util.readUTF(in);
      int len = in.readInt();
      this.ages = new int[len];
      if (len>0) {
        for (int i=0; i<len; i++) {
          this.ages[i] = in.readInt();
        }
      }
    }
  }
}
