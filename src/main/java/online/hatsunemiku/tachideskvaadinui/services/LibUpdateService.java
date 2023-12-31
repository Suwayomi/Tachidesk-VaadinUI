/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services;

import feign.RetryableException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import online.hatsunemiku.tachideskvaadinui.services.client.LibUpdateClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LibUpdateService {

  private final SettingsService settingsService;
  private final LibUpdateClient client;

  public LibUpdateService(SettingsService settingsService, LibUpdateClient client) {
    this.settingsService = settingsService;
    this.client = client;
  }

  @CacheEvict(
      value = {"manga"},
      allEntries = true)
  public boolean fetchUpdate() {
    var settings = settingsService.getSettings();

    URI baseUrl = URI.create(settings.getUrl());

    ResponseEntity<Void> response;
    try {
      response = client.fetchUpdate(baseUrl);
    } catch (RetryableException e) {
      return false;
    }

    return response.getStatusCode().is2xxSuccessful();
  }

  @Scheduled(initialDelay = 1, fixedRate = 10, timeUnit = TimeUnit.MINUTES)
  protected void scheduledUpdate() {
    boolean success = fetchUpdate();

    if (success) {
      log.info("Library update started");
    } else {
      log.debug("Could not start library update");
    }
  }
}
