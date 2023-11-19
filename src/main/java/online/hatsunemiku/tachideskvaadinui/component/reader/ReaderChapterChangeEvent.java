package online.hatsunemiku.tachideskvaadinui.component.reader;

import com.vaadin.flow.component.ComponentEvent;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter;

import java.util.List;

@Getter
public class ReaderChapterChangeEvent extends ComponentEvent<MangaReader> {
  private final int mangaId;
  private final int chapterId;
  private final List<Chapter> chapters;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   * @param fromClient <code>true</code> if the event originated from the client side, <code>false
   *     </code> otherwise
   * @param mangaId The ID of the {@link online.hatsunemiku.tachideskvaadinui.data.tachidesk.Manga
   *     Manga} to which the next chapter belongs
   * @param chapterId The ID of the next {@link
   *     online.hatsunemiku.tachideskvaadinui.data.tachidesk.Chapter Chapter}
   */
  public ReaderChapterChangeEvent(
      MangaReader source, boolean fromClient, int mangaId, int chapterId, List<Chapter> chapters) {
    super(source, fromClient);
    this.mangaId = mangaId;
    this.chapterId = chapterId;
    this.chapters = chapters;
  }
}
