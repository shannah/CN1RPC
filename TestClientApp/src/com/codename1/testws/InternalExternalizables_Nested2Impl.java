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

public class InternalExternalizables_Nested2Impl extends InternalExternalizables.Nested2 implements Externalizable {
  @Override
  public String getObjectId() {
    return "com.codename1.testws.InternalExternalizables.Nested2";
  }

  @Override
  public int getVersion() {
    return 1;
  }

  @Override
  public void externalize(DataOutputStream out) throws IOException {
    Util.writeUTF(this.stringVal2, out);
  }

  @Override
  public void internalize(int version, DataInputStream in) throws IOException {
    if (version == 1) {
      this.stringVal2 = Util.readUTF(in);
    }
  }
}
