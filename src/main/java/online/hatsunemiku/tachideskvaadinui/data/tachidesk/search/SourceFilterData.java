package online.hatsunemiku.tachideskvaadinui.data.tachidesk.search;

import java.util.List;

public record SourceFilterData(String searchTerm, List<SourceFilterChange> filter) {
  public SourceFilterData(String searchTerm) {
    this(searchTerm, List.of());
  }
}
