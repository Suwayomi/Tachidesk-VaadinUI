/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.page.History;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeLeaveEvent;
import com.vaadin.flow.router.BeforeLeaveObserver;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.component.reader.MangaReader;
import online.hatsunemiku.tachideskvaadinui.component.reader.ReaderChapterChangeEvent;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;
import online.hatsunemiku.tachideskvaadinui.services.MangaService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingCommunicationService;
import online.hatsunemiku.tachideskvaadinui.services.TrackingDataService;
import online.hatsunemiku.tachideskvaadinui.view.layout.StandardLayout;

@Route("reading/:mangaId(\\d+)/:chapterIndex(\\d+(?:\\.\\d+)?)")
@CssImport("./css/reading.css")
@Slf4j
public class ReadingView extends StandardLayout
    implements BeforeEnterObserver, BeforeLeaveObserver {

  private final MangaService mangaService;
  private final SettingsService settingsService;
  private final TrackingDataService dataService;
  private final TrackingCommunicationService communicationService;

  public ReadingView(
      MangaService mangaService,
      SettingsService settingsService,
      TrackingDataService dataService,
      TrackingCommunicationService communicationService) {
    super("Reading");

    this.mangaService = mangaService;
    this.settingsService = settingsService;
    this.dataService = dataService;
    this.communicationService = communicationService;

    fullScreen();
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    var idparam = event.getRouteParameters().get("mangaId");
    var chapterparam = event.getRouteParameters().get("chapterIndex");

    if (idparam.isEmpty()) {
      event.rerouteToError(NotFoundException.class, "Manga not found");
      return;
    }

    if (chapterparam.isEmpty()) {
      event.rerouteToError(NotFoundException.class, "Chapter not found");
      return;
    }

    String mangaIdStr = idparam.get();
    String chapter = chapterparam.get();

    long mangaId = Long.parseLong(mangaIdStr);

    int chapterIndex = Integer.parseInt(chapter);

    Chapter chapterObj = mangaService.getChapter(mangaId, chapterIndex);

    boolean hasNext;

    try {
      hasNext = mangaService.getChapter(mangaId, chapterIndex + 1) != null;
    } catch (Exception e) {
      hasNext = false;
    }

    if (chapterObj == null) {
      event.rerouteToError(NotFoundException.class, "Chapter not found");
      return;
    }

    var reader =
        new MangaReader(
            chapterObj, settingsService, dataService, mangaService, communicationService, hasNext);

    reader.addReaderChapterChangeEventListener(this::processReaderChapterChangeEvent);

    setContent(reader);
  }

  private void processReaderChapterChangeEvent(ReaderChapterChangeEvent event) {
    var nextChapterIndex = event.getChapterIndex();
    var nextMangaId = event.getMangaId();

    var nextChapter = mangaService.getChapter(nextMangaId, nextChapterIndex);

    boolean hasNextChapter;

    try {
      hasNextChapter = mangaService.getChapter(nextMangaId, nextChapterIndex + 1) != null;
    } catch (Exception ex) {
      hasNextChapter = false;
    }

    var nextReader =
        new MangaReader(
            nextChapter,
            settingsService,
            dataService,
            mangaService,
            communicationService,
            hasNextChapter);

    nextReader.addReaderChapterChangeEventListener(this::processReaderChapterChangeEvent);

    replaceReader(nextReader);
    log.debug("Set content to next chapter {} for manga {}", nextChapterIndex, nextMangaId);

    UI ui = getUI().orElseGet(UI::getCurrent);

    if (ui == null) {
      log.error("UI can't be updated");
      throw new NullPointerException("UI can't be updated");
    }

    History history = ui.getPage().getHistory();

    String template = "reading/%d/%d";

    String readerUrl = template.formatted(event.getMangaId(), event.getChapterIndex());

    Location location = new Location(readerUrl);

    history.pushState(null, location);
  }

  private void replaceReader(MangaReader reader) {
    UI ui = getUI().orElseGet(UI::getCurrent);

    if (ui == null) {
      log.error("UI can't be updated");
      throw new NullPointerException("UI can't be updated");
    }

    ui.access(() -> setContent(reader));
  }

  @Override
  public void beforeLeave(BeforeLeaveEvent event) {
    UI.getCurrent()
        .access(
            () -> UI.getCurrent().getPage().executeJs("document.body.style.overflow = 'auto';"));
  }
}
