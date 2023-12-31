/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package online.hatsunemiku.tachideskvaadinui.services.client;

import java.net.URI;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.search.SearchQueryParameters;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.search.SearchResponse;
import online.hatsunemiku.tachideskvaadinui.data.tachidesk.search.SourceFilterData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "search-client", url = "https://localhost:8080/")
public interface SearchClient {

  @PostMapping("/api/v1/source/{sourceId}/quick-search")
  SearchResponse quickSearch(
      URI baseUrl,
      @PathVariable long sourceId,
      @SpringQueryMap SearchQueryParameters searchQueryParameters,
      @RequestBody SourceFilterData filterData);

  @GetMapping("/api/v1/source/{sourceId}/search")
  SearchResponse search(
      URI baseUrl,
      @PathVariable long sourceId,
      @SpringQueryMap SearchQueryParameters searchQueryParameters);
}
