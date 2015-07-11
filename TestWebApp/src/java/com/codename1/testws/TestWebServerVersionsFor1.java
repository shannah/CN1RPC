package com.codename1.testws;

import java.lang.Class;

class TestWebServerVersionsFor1 {
  static int getVersionFor(Class cls) {
    if (ParentExternalizable.class.equals(cls)) {
      return 1;
    }
    else if (TestExternalizable.class.equals(cls)) {
      return 1;
    }
    else if (TestVersionedClass.class.equals(cls)) {
      return 3;
    }
    else if (InternalExternalizables.Nested2.class.equals(cls)) {
      return 1;
    }
    else if (InternalExternalizables.Nested1.class.equals(cls)) {
      return 1;
    }
    else if (ChildExternalizable.class.equals(cls)) {
      return 1;
    }
    throw new RuntimeException("Cannot find version for class "+cls);
  }
}
