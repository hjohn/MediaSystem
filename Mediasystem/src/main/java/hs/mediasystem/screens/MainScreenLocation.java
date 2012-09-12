package hs.mediasystem.screens;


public class MainScreenLocation implements Location {

  @Override
  public String getId() {
    return "MainScreen";
  }

  @Override
  public Class<?> getParameterType() {
    return null;
  }

  @Override
  public Type getType() {
    return Type.NORMAL;
  }

  @Override
  public Location getParent() {
    return null;
  }

  @Override
  public String getBreadCrumb() {
    return "Home";
  }
}
