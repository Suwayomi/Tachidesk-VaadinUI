package online.hatsunemiku.tachideskvaadinui.component.dialog.tracking;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import java.util.concurrent.atomic.AtomicReference;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.AniListMedia;
import online.hatsunemiku.tachideskvaadinui.data.tracking.anilist.common.MediaDate;
import online.hatsunemiku.tachideskvaadinui.services.AniListAPIService;
import online.hatsunemiku.tachideskvaadinui.services.SettingsService;
import org.jetbrains.annotations.NotNull;
import org.vaadin.miki.shared.labels.LabelPosition;
import org.vaadin.miki.superfields.text.LabelField;

public class TrackingMangaChoiceDialog extends Dialog {

  public TrackingMangaChoiceDialog(String mangaName, long mangaId, AniListAPIService aniListAPI,
      SettingsService settingsService) {
    TextField searchField = new TextField("Search Manga");
    searchField.setValue(mangaName);

    var apiResponse = aniListAPI.searchManga(mangaName);
    var mangaList = apiResponse.data().page().media();

    ListBox<AniListMedia> searchResults = new ListBox<>();
    searchResults.addClassName("manga-search-results");
    searchResults.setRenderer(getRenderer());
    searchResults.setItems(mangaList);
    AtomicReference<AniListMedia> selectedManga = new AtomicReference<>();
    searchResults.addValueChangeListener(e -> {
      AniListMedia selected = e.getValue();

      if (selected == null) {
        return;
      }

      int id = selected.id();

      if (!aniListAPI.isMangaInList(id)) {
        aniListAPI.addMangaToList(id);
      }

      selectedManga.set(selected);
    });

    add(searchField, searchResults);

    Button closeBtn = new Button("Close");
    closeBtn.addClickListener(e -> close());

    Button saveBtn = new Button("Save");
    saveBtn.addClickListener(e -> {
      var manga = selectedManga.get();

      if (manga == null) {
        Notification.show("Please select a manga to save");
        return;
      }
      int aniListId = manga.id();

      settingsService.getSettings()
          .getTracker(mangaId)
          .setAniListId(aniListId);

      close();
    });

    getFooter().add(closeBtn, saveBtn);
  }

  @NotNull
  private static ComponentRenderer<Component, AniListMedia> getRenderer() {
    return new ComponentRenderer<>(media -> {
      Div content = new Div();
      content.setClassName("manga-search-result");

      MediaDate mangaDate = media.date();

      Div upperHalf = new Div();
      upperHalf.setClassName("manga-search-result-upper-half");

      Image image = new Image(media.coverImage().large(), "Cover Image");

      Div data = new Div();

      LabelField<String> title = new LabelField<String>()
          .withLabelPosition(LabelPosition.BEFORE_MIDDLE)
          .withLabel("Title")
          .withValue(media.title().userPreferred());

      title.setClassName("manga-search-result-attribute");

      LabelField<String> type = new LabelField<String>()
          .withLabelPosition(LabelPosition.BEFORE_MIDDLE)
          .withLabel("Type")
          .withValue(media.format());

      type.addClassName("manga-search-result-attribute");

      String date = "%s-%s-%s".formatted(mangaDate.year(), mangaDate.month(), mangaDate.day());
      LabelField<String> started = new LabelField<String>()
          .withLabelPosition(LabelPosition.BEFORE_MIDDLE)
          .withLabel("Started")
          .withValue(date);

      started.addClassName("manga-search-result-attribute");

      LabelField<String> status = new LabelField<String>()
          .withLabelPosition(LabelPosition.BEFORE_MIDDLE)
          .withLabel("Status")
          .withValue(media.status());

      status.addClassName("manga-search-result-attribute");

      data.add(title, type, started, status);
      data.setClassName("manga-search-result-data");

      upperHalf.add(image, data);

      Text description = new Text(media.description());

      content.add(upperHalf, description);

      return content;
    });
  }


  /**
   * Adds a component event listener to track when a manga was chosen.
   *
   * @param listener the listener to be added
   * @return the registration object for the added listener (can be used to remove the listener)
   */
  public Registration addTrackerMangaChosenEventListener(ComponentEventListener<TrackerMangaChosenEvent> listener) {
    return addListener(TrackerMangaChosenEvent.class, listener);
  }
}