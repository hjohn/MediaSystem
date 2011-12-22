package hs.mediasystem.screens;

public class SubOption extends Option {
  private final Option[] options;
  
  public SubOption(String description, Option... options) {
    super(description);

    this.options = options;
  }

  public Option[] getOptions() {
    return options;
  } 
}