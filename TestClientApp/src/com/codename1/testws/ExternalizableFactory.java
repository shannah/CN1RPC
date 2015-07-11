// DO NOT MODIFY THIS FILE.  IT HAS BEEN AUTOMATICALLY GENERATED
// CHANGES MAY BE OVERWRITTEN WITHOUT NOTICE
package com.codename1.testws;

import com.codename1.io.Util;
import java.lang.Class;

public class ExternalizableFactory {
  public void init() {
    Util.register("com.codename1.testws.TestExternalizable", TestExternalizableImpl.class);
    Util.register("com.codename1.testws.InternalExternalizables.Nested1", InternalExternalizables_Nested1Impl.class);
    Util.register("com.codename1.testws.ChildExternalizable", ChildExternalizableImpl.class);
    Util.register("com.codename1.testws.TestVersionedClass", TestVersionedClassImpl.class);
    Util.register("com.codename1.testws.ParentExternalizable", ParentExternalizableImpl.class);
    Util.register("com.codename1.testws.InternalExternalizables.Nested2", InternalExternalizables_Nested2Impl.class);
  }

  public <T> T create(Class<T> cls) {
    if (TestExternalizable.class.equals(cls)) {
      return (T)new TestExternalizableImpl();
    }
    if (InternalExternalizables.Nested1.class.equals(cls)) {
      return (T)new InternalExternalizables_Nested1Impl();
    }
    if (ChildExternalizable.class.equals(cls)) {
      return (T)new ChildExternalizableImpl();
    }
    if (TestVersionedClass.class.equals(cls)) {
      return (T)new TestVersionedClassImpl();
    }
    if (ParentExternalizable.class.equals(cls)) {
      return (T)new ParentExternalizableImpl();
    }
    if (InternalExternalizables.Nested2.class.equals(cls)) {
      return (T)new InternalExternalizables_Nested2Impl();
    }
    throw new RuntimeException("No matching implementation found for class.");
  }

  public <T> T create(Class<T> cls, int version) {
    T out = create(cls);
    ((Versioned)out).setVersion(version);
    return out;
  }

  interface Versioned {
    void setVersion(int version);

    int getVersion();
  }
}
