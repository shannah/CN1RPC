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

public class ParentExternalizableImpl extends ParentExternalizable implements Externalizable, ExternalizableFactory.Versioned {
  int __version = 1;

  public void setVersion(int version) {
    __version=version;
  }

  @Override
  public String getObjectId() {
    return "com.codename1.testws.ParentExternalizable";
  }

  @Override
  public int getVersion() {
    return __version;
  }

  @Override
  public void externalize(DataOutputStream out) throws IOException {
    if (__version == 1) {
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
    else {
      throw new RuntimeException("Unsupported write version for entity "+getObjectId()+" version "+__version+"");
    }
  }

  @Override
  public void internalize(int version, DataInputStream in) throws IOException {
    if (version == 1) {
      __version = version;
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
    else {
      throw new RuntimeException("Unsupported read version for entity "+getObjectId()+" version "+version+"");
    }
  }
}
