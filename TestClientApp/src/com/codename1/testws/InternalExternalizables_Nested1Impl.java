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

public class InternalExternalizables_Nested1Impl extends InternalExternalizables.Nested1 implements Externalizable, ExternalizableFactory.Versioned {
  int __version = 1;

  public void setVersion(int version) {
    __version=version;
  }

  @Override
  public String getObjectId() {
    return "com.codename1.testws.InternalExternalizables.Nested1";
  }

  @Override
  public int getVersion() {
    return __version;
  }

  @Override
  public void externalize(DataOutputStream out) throws IOException {
    if (__version == 1) {
      Util.writeUTF(this.stringVal, out);
    }
    else {
      throw new RuntimeException("Unsupported write version for entity "+getObjectId()+" version "+__version+"");
    }
  }

  @Override
  public void internalize(int version, DataInputStream in) throws IOException {
    if (version == 1) {
      __version = version;
      this.stringVal = Util.readUTF(in);
    }
    else {
      throw new RuntimeException("Unsupported read version for entity "+getObjectId()+" version "+version+"");
    }
  }
}
