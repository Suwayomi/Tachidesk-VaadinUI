package online.hatsunemiku.tachideskvaadinui.component.scroller;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import online.hatsunemiku.tachideskvaadinui.component.items.BlurryItem;
import online.hatsunemiku.tachideskvaadinui.component.items.LangItem;
import online.hatsunemiku.tachideskvaadinui.component.items.SourceItem;
import online.hatsunemiku.tachideskvaadinui.data.Settings;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Source;
import online.hatsunemiku.tachideskvaadinui.services.SourceService;
import online.hatsunemiku.tachideskvaadinui.utils.SerializationUtils;
import online.hatsunemiku.tachideskvaadinui.view.ServerStartView;
import org.vaadin.firitin.components.orderedlayout.VScroller;

@CssImport("./css/components/source-scroller.css")
public class SourceScroller extends VScroller {

  private final SourceService service;
  private final List<List<Source>> filteredSources;
  private final List<String> languages;
  private final Div content;
  private int currentIndex = 0;
  private int languageIndex = 0;
  private static final int LIST_SIZE = 15;
  private boolean isDone = false;

  public SourceScroller(SourceService service) {
    super();
    setClassName("source-scroller");

    this.service = service;

    List<Source> sourceList;
    try {
      sourceList = service.getSources();
    } catch (Exception e) {
      getUI().ifPresent(ui -> ui.access(() -> ui.navigate(ServerStartView.class)));
      this.filteredSources = new ArrayList<>();
      this.content = new Div();
      this.languages = new ArrayList<>();
      return;
    }
    List<Source> sources = new ArrayList<>(sourceList);

    languages = new ArrayList<>(getLanguages(sources));
    languages.sort((o1, o2) -> {
      if (o1.equals("localsourcelang")) {
        return -1;
      }
      if (o2.equals("localsourcelang")) {
        return 1;
      }
      return o1.compareTo(o2);
    });

    this.content = new Div();
    this.filteredSources = new ArrayList<>();

    sort(sources);

    for (String language : languages) {
      List<Source> filtered = filterLang(language, sources);

      if (filtered.isEmpty()) {
        continue;
      }

      filteredSources.add(filtered);
    }

    Settings settings = SerializationUtils.deseralizeSettings();

    addNextContent(settings);
    setContent(content);

    addScrollToEndListener(e -> addNextContent(settings));
  }


  private void sort(List<Source> sources) {
    sources.sort(Comparator.comparing(Source::getName));
  }

  private void addNextContent(Settings settings) {
    List<BlurryItem> items = new ArrayList<>();
    getNextContent(settings, items);
    for (BlurryItem source : items) {
      content.add(source);
    }
  }

  private void getNextContent(Settings settings, List<BlurryItem> items) {

    if (isDone) {
      return;
    }

    if (languageIndex >= filteredSources.size()) {
      return;
    }

    if (items.size() >= LIST_SIZE) {
      return;
    }

    List<Source> sources = filteredSources.get(languageIndex);

    if (currentIndex >= sources.size()) {
      switchToNextLang(items, settings);
      if (isDone) {
        return;
      }
    }

    int endIndex = currentIndex + LIST_SIZE;

    if (endIndex > sources.size()) {
      endIndex = sources.size();
    }

    List<Source> subList = new ArrayList<>(sources.subList(currentIndex, endIndex));

    for (Source source : subList) {
      SourceItem item = new SourceItem(source, settings);
      items.add(item);
    }

    if (subList.size() < LIST_SIZE) {
      switchToNextLang(items, settings);
    } else {
      currentIndex = endIndex;
    }
  }

  private void switchToNextLang(List<BlurryItem> subList, Settings settings) {
    currentIndex = 0;
    languageIndex++;

    if (languageIndex >= languages.size()) {
      isDone = true;
      return;
    }

    if (subList.size() >= LIST_SIZE) {
      return;
    }

    String lang = languages.get(languageIndex);
    LangItem langItem = new LangItem(lang);
    subList.add(langItem);

    getNextContent(settings, subList);
  }

  private List<String> getLanguages(List<Source> sources) {
    return sources.parallelStream()
        .map(Source::getLang)
        .distinct()
        .sorted()
        .toList();
  }

  private List<Source> filterLang(String lang, List<Source> sources) {
    return sources.parallelStream()
        .filter(source -> source.getLang().equals(lang))
        .toList();
  }

  private List<Source> filterSources(String search, List<Source> sources) {
    if (search == null || search.isBlank()) {
      return sources;
    }

    return sources.parallelStream()
        .filter(source -> source.getName().toLowerCase().contains(search.toLowerCase()))
        .toList();
  }
}