/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.component.card.event;

import com.vaadin.flow.component.ComponentEvent;
import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.component.tab.CategoryTab;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.Category;

@Getter
public class MangaCategoryUpdateEvent extends ComponentEvent<CategoryTab> {

  private final long mangaId;
  private final Category newCategory;

  /**
   * Creates a new event using the given source and indicator whether the event originated from the
   * client side or the server side.
   *
   * @param source the source component
   * @param fromClient <code>true</code> if the event originated from the client side, <code>false
   *     </code> otherwise
   */
  public MangaCategoryUpdateEvent(
      CategoryTab source, boolean fromClient, long mangaId, Category newCategory) {
    super(source, fromClient);
    this.newCategory = newCategory;
    this.mangaId = mangaId;
  }
}
