package hs.mediasystem.entity;

import java.lang.annotation.RetentionPolicy;

@java.lang.annotation.Documented
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
@javax.inject.Qualifier
public @interface EntityEnricher {
  Class<? extends EntitySource> sourceClass();
  Class<? extends Entity> entityClass();
  double priority();
}
