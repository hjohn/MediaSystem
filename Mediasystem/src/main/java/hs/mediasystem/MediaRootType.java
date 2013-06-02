package hs.mediasystem;

import hs.mediasystem.framework.MediaRoot;

import java.lang.annotation.RetentionPolicy;

@java.lang.annotation.Documented
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
@javax.inject.Qualifier
public @interface MediaRootType {
  Class<? extends MediaRoot> value();
}
