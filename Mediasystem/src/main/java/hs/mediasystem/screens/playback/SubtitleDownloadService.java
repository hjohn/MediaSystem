package hs.mediasystem.screens.playback;

import hs.subtitle.SubtitleDescriptor;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class SubtitleDownloadService extends Service<Path> {
  private static int counter;

  private SubtitleDescriptor subtitleDescriptor;

  public void setSubtitleDescriptor(SubtitleDescriptor subtitleDescriptor) {
    this.subtitleDescriptor = subtitleDescriptor;
  }

  @Override
  protected Task<Path> createTask() {
    final SubtitleDescriptor descriptor = subtitleDescriptor;

    return new Task<Path>() {
      @Override
      protected Path call() throws IOException {
        updateProgress(10, 100);
        updateTitle("Fetching subtitle");
        updateMessage("Downloading " + descriptor.getName() + "...");

        byte[] buffer = descriptor.getSubtitleRawData();
        updateProgress(60, 100);

        Path path = Paths.get("tempsubtitle" + ++counter + ".srt");

        try(DataOutputStream os = new DataOutputStream(new FileOutputStream(path.toFile()))) {
          os.write(buffer);
          updateProgress(90, 100);
        }

        updateProgress(100, 100);

        return path;
      }
    };
  }

}
