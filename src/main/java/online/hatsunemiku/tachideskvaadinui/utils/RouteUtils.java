/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import lombok.experimental.UtilityClass;
import online.hatsunemiku.tachideskvaadinui.view.ReadingView;

@UtilityClass
public class RouteUtils {

  /**
   * Method to route to the reading view.
   *
   * @param ui The UI object to navigate with.
   * @param mangaId The ID of the manga.
   * @param chapterIndex The index of the chapter.
   */
  public void routeToReadingView(UI ui, long mangaId, long chapterIndex) {

    String mangaIdString = String.valueOf(mangaId);
    String chapterIndexString = String.valueOf(chapterIndex);

    RouteParam mangaIdParam = new RouteParam("mangaId", mangaIdString);
    RouteParam chapterIndexParam = new RouteParam("chapterIndex", chapterIndexString);

    RouteParameters params = new RouteParameters(mangaIdParam, chapterIndexParam);

    ui.navigate(ReadingView.class, params);
  }
}
