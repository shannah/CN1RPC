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

public class TestVersionedClassImpl extends TestVersionedClass implements Externalizable, ExternalizableFactory.Versioned {
  int __version = 3;

  public void setVersion(int version) {
    __version=version;
  }

  @Override
  public String getObjectId() {
    return "com.codename1.testws.TestVersionedClass";
  }

  @Override
  public int getVersion() {
    return __version;
  }

  @Override
  public void externalize(DataOutputStream out) throws IOException {
    if (__version == 1) {
      out.writeInt(this.a);
      out.writeInt(this.b);
    }
    else if (__version == 2) {
      out.writeInt(this.a);
      out.writeInt(this.b);
      Util.writeUTF(this.name, out);
      out.writeFloat(0);
    }
    else if (__version == 3) {
      out.writeInt(this.a);
      out.writeInt(this.b);
      Util.writeUTF(this.name, out);
    }
    else {
      throw new RuntimeException("Unsupported write version for entity "+getObjectId()+" version "+__version+"");
    }
  }

  @Override
  public void internalize(int version, DataInputStream in) throws IOException {
    if (version == 1) {
      __version = version;
      this.a = in.readInt();
      this.b = in.readInt();
    }
    else if (version == 2) {
      __version = version;
      this.a = in.readInt();
      this.b = in.readInt();
      this.name = Util.readUTF(in);
      in.readFloat();
    }
    else if (version == 3) {
      __version = version;
      this.a = in.readInt();
      this.b = in.readInt();
      this.name = Util.readUTF(in);
    }
    else {
      throw new RuntimeException("Unsupported read version for entity "+getObjectId()+" version "+version+"");
    }
  }
}
