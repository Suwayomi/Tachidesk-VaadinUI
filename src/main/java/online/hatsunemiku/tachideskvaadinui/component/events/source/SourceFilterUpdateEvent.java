package online.hatsunemiku.tachideskvaadinui.component.events.source;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;

public class SourceFilterUpdateEvent extends ComponentEvent<TextField> {

  @Getter private final String filterText;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   */
  public SourceFilterUpdateEvent(TextField source, String filterText) {
    super(source, false);
    this.filterText = filterText;
  }
}