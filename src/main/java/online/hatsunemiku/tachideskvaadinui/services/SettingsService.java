package online.hatsunemiku.tachideskvaadinui.services;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.startup.TachideskMaintainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

  private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);

  @Getter private final Settings settings;

  @Getter(AccessLevel.NONE)
  private final ObjectMapper mapper;

  @Getter(AccessLevel.NONE)
  private final TachideskMaintainer maintainer;

  public SettingsService(ObjectMapper mapper, TachideskMaintainer maintainer) {
    this.mapper = mapper;
    this.maintainer = maintainer;
    settings = deserialize();
  }

  private Settings deserialize() {
    final Settings settings;

    var projectDir = maintainer.getProjectDir();

    Path projectDirPath = projectDir.getAbsoluteFile().toPath();
    Path settingsFile = projectDirPath.resolve("settings.json");

    if (!Files.exists(settingsFile)) {
      settings = getDefaults();
      serialize();
      return settings;
    }

    Settings tempSettings;
    try (var in = Files.newInputStream(settingsFile)) {
      tempSettings = mapper.readValue(in, Settings.class);
    } catch (EOFException e) {
      settings = getDefaults();
      serialize();
      return settings;
    } catch (IOException e) {
      logger.error("Could not read settings file", e);
      throw new RuntimeException(e);
    }

    settings = tempSettings;
    return settings;
  }

  private void serialize() {
    ObjectMapper mapper = new ObjectMapper();

    var projectDir = maintainer.getProjectDir();

    Path projectDirPath = projectDir.getAbsoluteFile().toPath();
    Path settingsFile = projectDirPath.resolve("settings.json");

    try (var out = Files.newOutputStream(settingsFile, CREATE, WRITE)) {
      mapper.writeValue(out, settings);
    } catch (IOException e) {
      logger.error("Could not write settings file", e);
      throw new RuntimeException(e);
    }
  }

  @EventListener(ContextClosedEvent.class)
  public void onShutdownEvent() {
    logger.info("Saving settings");
    serialize();
  }

  private Settings getDefaults() {
    return new Settings("http://localhost:4567");
  }
}
