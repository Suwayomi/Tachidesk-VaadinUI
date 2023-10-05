/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import lombok.Getter;
import online.hatsunemiku.tachideskvaadinui.data.settings.Settings;
import online.hatsunemiku.tachideskvaadinui.data.settings.event.UrlChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Getter
@Service
public class WebClientService {
  private WebClient webClient;
  public WebClientService(SettingsService settingsService) {
    Settings settings = settingsService.getSettings();

    this.webClient = WebClient.create(settings.getUrl());
  }

  @EventListener(UrlChangeEvent.class)
  protected void onUrlChange(UrlChangeEvent event) {
    this.webClient = WebClient.create(event.getUrl());
  }


}