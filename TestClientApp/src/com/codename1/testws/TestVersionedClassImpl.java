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

public class TestVersionedClassImpl extends TestVersionedClass implements Externalizable {
  @Override
  public String getObjectId() {
    return "com.codename1.testws.TestVersionedClass";
  }

  @Override
  public int getVersion() {
    return 3;
  }

  @Override
  public void externalize(DataOutputStream out) throws IOException {
    out.writeInt(this.a);
    out.writeInt(this.b);
    Util.writeUTF(this.name, out);
  }

  @Override
  public void internalize(int version, DataInputStream in) throws IOException {
    if (version == 1) {
      this.a = in.readInt();
      this.b = in.readInt();
    }
    if (version == 2) {
      this.a = in.readInt();
      this.b = in.readInt();
      this.name = Util.readUTF(in);
      in.readFloat();
    }
    if (version == 3) {
      this.a = in.readInt();
      this.b = in.readInt();
      this.name = Util.readUTF(in);
    }
  }
}
