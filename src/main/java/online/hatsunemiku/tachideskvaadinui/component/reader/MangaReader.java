/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.reader;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.shared.Registration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.ReaderSettingsChangeEvent;
import online.hatsunemiku.tachideskvaadinui.data.settings.reader.ReaderSettings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.data.tracking.Tracker;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingCommunicationService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import online.hatsunemiku.tachideskvaadinui.view.RootView;
import org.jetbrains.annotations.NotNull;
import org.vaadin.addons.online.hatsunemiku.diamond.swiper.Swiper;
import org.vaadin.addons.online.hatsunemiku.diamond.swiper.SwiperConfig;
import org.vaadin.addons.online.hatsunemiku.diamond.swiper.constants.LanguageDirection;

@CssImport("./css/components/reader/manga-reader.css")
@Slf4j
public class MangaReader extends Div {

  private final SettingsService settingsService;
  private final ExecutorService trackerExecutor;

  public MangaReader(
      Chapter chapter,
      SettingsService settingsService,
      TrackingDataService dataService,
      MangaService mangaService,
      TrackingCommunicationService trackingCommunicationService,
      boolean hasNext) {
    addClassName("manga-reader");

    this.settingsService = settingsService;
    this.trackerExecutor = Executors.newSingleThreadExecutor();

    Reader reader = new Reader(chapter, dataService, trackingCommunicationService, mangaService);
    Sidebar sidebar = new Sidebar(mangaService, chapter, reader.swiper, hasNext);
    Controls controls = new Controls(reader, hasNext, chapter);
    add(sidebar, reader, controls);
  }

  public Registration addReaderChapterChangeEventListener(
      ComponentEventListener<ReaderChapterChangeEvent> listener) {
    return addListener(ReaderChapterChangeEvent.class, listener);
  }

  // skipcq: JAVA-W1019
  private class Sidebar extends Div {

    public Sidebar(MangaService mangaService, Chapter chapter, Swiper swiper, boolean hasNext) {
      addClassName("sidebar");

      Button home = getHomeButton();

      List<Chapter> chapters = mangaService.getChapterList(chapter.getMangaId());
      Div chapterSelect = new Div();
      chapterSelect.setClassName("chapter-select");
      chapterSelect.getStyle().set("--vaadin-combo-box-overlay-width", "20vw");

      Button leftBtn = getChapterLeftBtn(swiper, chapter, hasNext);

      ComboBox<Chapter> chapterComboBox = getChapterComboBox(chapter, chapters);

      Button rightBtn = getChapterRightBtn(swiper, chapter, hasNext);

      chapterSelect.add(leftBtn, chapterComboBox, rightBtn);

      Button settingsBtn = new Button(VaadinIcon.COG.create());
      settingsBtn.setId("settings-btn");
      settingsBtn.addClickListener(
          e -> {
            var dialog =
                new ReaderSettingsDialog(settingsService.getSettings(), chapter.getMangaId());
            dialog.open();
          });

      add(home, chapterSelect, settingsBtn);
    }

    @NotNull
    private Button getChapterRightBtn(Swiper swiper, Chapter chapter, boolean hasNext) {
      Button rightBtn = new Button(VaadinIcon.ANGLE_RIGHT.create());
      rightBtn.setId("rightBtn");
      rightBtn.addClickListener(
          e -> {
            int chapterIndex = chapter.getIndex();

            if (swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {

              if (chapterIndex <= 1) {
                return;
              }

              chapterIndex--;
            } else {

              if (!hasNext) {
                return;
              }

              chapterIndex++;
            }

            var changeEvent =
                new ReaderChapterChangeEvent(
                    MangaReader.this, false, chapter.getMangaId(), chapterIndex);
            MangaReader.this.fireEvent(changeEvent);
          });
      return rightBtn;
    }

    @NotNull
    private ComboBox<Chapter> getChapterComboBox(Chapter chapter, List<Chapter> chapters) {
      ComboBox<Chapter> chapterComboBox = new ComboBox<>();
      chapterComboBox.setRenderer(createRenderer());
      chapterComboBox.setItems(chapters);
      chapterComboBox.setValue(chapter);
      chapterComboBox.setAllowCustomValue(false);
      chapterComboBox.addValueChangeListener(
          e -> {
            if (!e.isFromClient()) {
              return;
            }

            if (Objects.equals(e.getOldValue(), e.getValue())) {
              return;
            }

            Chapter c = e.getValue();

            if (c == null) {
              return;
            }

            var mangaId = c.getMangaId();
            var chapterIndex = c.getIndex();

            var event =
                new ReaderChapterChangeEvent(MangaReader.this, false, mangaId, chapterIndex);

            MangaReader.this.fireEvent(event);
          });
      return chapterComboBox;
    }

    @NotNull
    private Button getChapterLeftBtn(Swiper swiper, Chapter chapter, boolean hasNext) {
      Button leftBtn = new Button(VaadinIcon.ANGLE_LEFT.create());
      leftBtn.setId("leftBtn");
      leftBtn.addClickListener(
          e -> {
            int chapterIndex = chapter.getIndex();

            int mangaId = chapter.getMangaId();
            if (swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {

              if (!hasNext) {
                return;
              }

              chapterIndex++;
            } else {

              if (chapterIndex <= 1) {
                return;
              }
              chapterIndex--;
            }

            var changeEvent =
                new ReaderChapterChangeEvent(MangaReader.this, false, mangaId, chapterIndex);

            MangaReader.this.fireEvent(changeEvent);
          });
      return leftBtn;
    }

    private Renderer<Chapter> createRenderer() {
      String template = """
          <div>${item.name}</div>
          """;

      return LitRenderer.<Chapter>of(template).withProperty("name", Chapter::getName);
    }

    @NotNull
    private static Button getHomeButton() {
      Button home = new Button(VaadinIcon.HOME.create());
      home.setId("homeBtn");
      home.addClickListener(e -> UI.getCurrent().navigate(RootView.class));
      return home;
    }
  }

  private class Reader extends Div {

    private final Chapter chapter;
    private final Swiper swiper;

    public Reader(
        Chapter chapter,
        TrackingDataService dataService,
        TrackingCommunicationService trackingCommunicationService,
        MangaService mangaService) {
      addClassName("reader");
      this.chapter = chapter;

      var config = SwiperConfig.builder().zoom(true).centeredSlides(true).build();

      swiper = new Swiper(config);

      UI ui = UI.getCurrent();
      ComponentUtil.addListener(
          ui,
          ReaderSettingsChangeEvent.class,
          e -> {
            var direction = e.getNewSettings().getDirection();

            switch (direction) {
              case RTL -> swiper.changeLanguageDirection(LanguageDirection.RIGHT_TO_LEFT);
              case LTR -> swiper.changeLanguageDirection(LanguageDirection.LEFT_TO_RIGHT);
              default -> throw new IllegalStateException("Unexpected value: " + direction);
            }
          });

      ReaderSettings settings =
          settingsService.getSettings().getReaderSettings(chapter.getMangaId());

      switch (settings.getDirection()) {
        case RTL -> swiper.changeLanguageDirection(LanguageDirection.RIGHT_TO_LEFT);
        case LTR -> swiper.changeLanguageDirection(LanguageDirection.LEFT_TO_RIGHT);
        default -> throw new IllegalStateException("Unexpected value: " + settings.getDirection());
      }

      /*This is a JavaScript function as it feels more sluggish when it has
       * to send data back to the server. Therefore, the server is responsible
       * for the mouse wheel's zoom function.
       */
      swiper
          .getElement()
          .executeJs(
              """
                  addEventListener('wheel', function (e) {

                    var zoom = $0.swiper.zoom.scale;
                    if (e.deltaY < 0) {
                      zoom += 0.5;
                    } else {
                      zoom -= 0.5;
                    }

                    if (zoom < 1) {
                      zoom = 1;
                    }

                    if (zoom > 3) {
                      zoom = 3;
                    }

                    $0.swiper.zoom.in(zoom);
                  });
                  """,
              swiper.getElement());

      loadChapter();

      Tracker tracker = dataService.getTracker(chapter.getMangaId());

      if (tracker.hasAniListId()) {
        swiper.addActiveIndexChangeEventListener(
            e -> {
              if (e.getActiveIndex() == chapter.getPageCount() - 1) {
                log.info("Last page of chapter {}", chapter.getIndex());
                trackerExecutor.submit(
                    () ->
                        trackingCommunicationService.setChapterProgress(
                            chapter.getMangaId(), chapter.getIndex(), true));
                e.unregisterListener();
              }
            });
      }

      swiper.addReachEndEventListener(
          e -> {
            int mangaId = chapter.getMangaId();
            int chapterIndex = chapter.getIndex();
            if (mangaService.setChapterRead(mangaId, chapterIndex)) {
              log.info("Set chapter {} to read", chapter.getName());
            } else {
              log.warn("Couldn't set chapter {} to read", chapter.getName());
            }
          });

      add(swiper);
    }

    private void loadChapter() {
      Settings settings = settingsService.getSettings();
      String baseUrl = settings.getUrl();
      int mangaId = chapter.getMangaId();
      int chapterIndex = chapter.getIndex();
      String format = "%s/api/v1/manga/%d/chapter/%d/page/%d";

      for (int i = 0; i < chapter.getPageCount(); i++) {
        String url = String.format(format, baseUrl, mangaId, chapterIndex, i);

        Image image = new Image(url, "Page %d".formatted(i + 1));

        if (i > 1) {
          image.getElement().setAttribute("loading", "lazy");
        }

        image.addClassName("manga-page");

        swiper.addZoomable(true, image);
      }
    }
  }

  private class Controls extends Div {

    private final int pageCount;
    private final int mangaId;
    private final int chapterIndex;
    private final boolean hasNext;

    public Controls(Reader reader, boolean hasNext, Chapter chapter) {
      addClassName("controls");

      this.pageCount = chapter.getPageCount();
      this.mangaId = chapter.getMangaId();
      this.chapterIndex = chapter.getIndex();
      this.hasNext = hasNext;

      Button left = getPrevButton(reader);

      Div pageTrack = new Div();

      TextField input = new TextField("", "1", "");
      input.setAllowedCharPattern("\\d");
      input.addValueChangeListener(
          e -> {
            if (e.getValue().isEmpty()) {
              log.debug("Value is empty");
              input.setValue(e.getOldValue());
              return;
            }

            if (!e.getValue().matches("\\d+")) {
              log.debug("Value is not a number");
              input.setValue(e.getOldValue());
              return;
            }

            int value = Integer.parseInt(e.getValue());

            if (value == reader.swiper.getActiveIndex()) {
              log.debug("Value is the same as active index");
              input.setValue(e.getOldValue());
              return;
            }

            if (value > pageCount || value < 1) {
              log.debug("Value is out of bounds");
              input.setValue(e.getOldValue());
              return;
            }

            reader.swiper.slideTo(value - 1);
            log.debug("Value changed to {}", value);
          });

      Text totalChapters = new Text("/ " + pageCount);

      reader.swiper.addActiveIndexChangeEventListener(
          e -> {
            int activeIndex = e.getActiveIndex() + 1;
            input.setValue(String.valueOf(activeIndex));
          });

      pageTrack.add(input, totalChapters);

      Button right = getNextButton(reader);

      add(left, pageTrack, right);
    }

    @NotNull
    private Button getNextButton(Reader reader) {
      Icon arrowRight = VaadinIcon.ARROW_RIGHT.create();
      Button right = new Button(arrowRight);
      right.addClickListener(
          e -> {
            Swiper swiper = reader.swiper;
            if (swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {
              prevPage(swiper);
            } else {
              nextPage(swiper);
            }
          });

      if (reader.swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {
        right.addClickShortcut(Key.ARROW_RIGHT);
      } else {
        right.addClickShortcut(Key.ARROW_LEFT);
      }

      right.setIconAfterText(true);
      return right;
    }

    @NotNull
    private Button getPrevButton(Reader reader) {
      Icon arrowLeft = VaadinIcon.ARROW_LEFT.create();
      Button left = new Button(arrowLeft);
      left.addClickListener(
          e -> {
            Swiper swiper = reader.swiper;
            if (swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {
              nextPage(swiper);
            } else {
              prevPage(swiper);
            }
          });

      if (reader.swiper.getLanguageDirection() == LanguageDirection.RIGHT_TO_LEFT) {
        left.addClickShortcut(Key.ARROW_LEFT);
      } else {
        left.addClickShortcut(Key.ARROW_RIGHT);
      }
      return left;
    }

    private void nextPage(Swiper swiper) {

      if (swiper.getActiveIndex() != pageCount - 1) {
        swiper.slideNext();
        return;
      }

      if (!hasNext) {
        return;
      }

      int chapterIndex = this.chapterIndex + 1;

      var event = new ReaderChapterChangeEvent(MangaReader.this, false, mangaId, chapterIndex);
      MangaReader.this.fireEvent(event);
    }

    private void prevPage(Swiper swiper) {

      if (swiper.getActiveIndex() != 0) {
        swiper.slidePrev();
        return;
      }

      if (this.chapterIndex <= 1) {
        return;
      }

      int chapterIndex = this.chapterIndex - 1;

      var event = new ReaderChapterChangeEvent(MangaReader.this, false, mangaId, chapterIndex);
      MangaReader.this.fireEvent(event);
    }
  }
}
