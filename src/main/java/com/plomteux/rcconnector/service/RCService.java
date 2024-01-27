package com.plomteux.rcconnector.service;

import com.plomteux.rcconnector.entity.CruiseDetailsEntity;
import com.plomteux.rcconnector.mapper.CruiseDetailsMapper;
import com.plomteux.rcconnector.model.Cruise;
import com.plomteux.rcconnector.model.CruiseSearchResponse;
import com.plomteux.rcconnector.repository.CruiseDetailsRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class RCService {
    @Value("${rcc.api.endpoint.graph}")
    private String endpointGraph;
    @Value("${rcc.country}")
    private String country;
    @Value("${rcc.currency}")
    private String currency;

    private final RestTemplate restTemplate;
    private final HttpHeaders jsonHttpHeaders;
    private final CruiseDetailsMapper cruiseDetailsMapper;
    private final CruiseDetailsRepository cruiseDetailsRepository;
    public void getAllCruisesDetails() {
        int count = 100;
        int total = 0;
        int skip = 0;
        List<Cruise> allCruises = new ArrayList<>();
        do {
            jsonHttpHeaders.set("country", country);
            jsonHttpHeaders.set("currency", currency);

            HttpEntity<String> entity = new HttpEntity<>(
                    "{\n" +
                            "  \"operationName\":\"cruiseSearch_Cruises\",\n" +
                            "  \"variables\":{\n" +
                            "    \"sort\":{\n" +
                            "      \"by\":\"PRICE\",\n" +
                            "      \"order\":\"ASC\"\n" +
                            "    },\n" +
                            "    \"pagination\":{\n" +
                            "      \"count\":" + count + ",\n" +
                            "      \"skip\":" + skip + "\n" +
                            "    }\n" +
                            "  },\n" +
                            "  \"query\":\"query cruiseSearch_Cruises(\\n  $filters: String\\n  $qualifiers: String\\n  $sort: CruiseSearchSort\\n  $pagination: CruiseSearchPagination\\n) {\\n  cruiseSearch(\\n    filters: $filters\\n    qualifiers: $qualifiers\\n    sort: $sort\\n    pagination: $pagination\\n  ) {\\n    results {\\n      cruises {\\n        id\\n        productViewLink\\n        masterSailing {\\n          itinerary {\\n            name\\n            code\\n          days {\\n            number\\n            type\\n            ports {\\n              port {\\n                code\\n                name\\n                region\\n              }\\n            }\\n          }\\n            departurePort {\\n              code\\n              name\\n              region\\n            }\\n            destination {\\n              code\\n              name\\n            }\\n            totalNights\\n          }\\n        }\\n        sailings {\\n          bookingLink\\n          id\\n          itinerary {\\n            code\\n          }\\n          sailDate\\n          startDate\\n          endDate\\n          taxesAndFees {\\n            value\\n          }\\n          taxesAndFeesIncluded\\n          stateroomClassPricing {\\n            price {\\n              value\\n            }\\n            stateroomClass {\\n              id\\n            }\\n          }\\n        }\\n      }\\n      total\\n    }\\n  }\\n}\\n\"\n" +
                            "}", jsonHttpHeaders
            );
            ResponseEntity<CruiseSearchResponse> cruiseSearchResponse;
            try {
                cruiseSearchResponse = restTemplate.exchange(
                        endpointGraph,
                        HttpMethod.POST ,
                        entity,
                        new ParameterizedTypeReference<>() {
                        }
                );
                if (total == 0) {
                    total = Objects.requireNonNull(cruiseSearchResponse.getBody()).getData().getCruiseSearch().getResults().getTotal();
                }
                allCruises.addAll(Objects.requireNonNull(cruiseSearchResponse.getBody()).getData().getCruiseSearch().getResults().getCruises());
            } catch (HttpClientErrorException e) {
                HttpStatusCode statusCode = e.getStatusCode();
                log.warn("HTTP client error occurred while retrieving cruise details: {} - {}", statusCode, e.getMessage());
                ResponseEntity.status(statusCode).build();
                return;
            } catch (HttpServerErrorException e) {
                HttpStatusCode statusCode = e.getStatusCode();
                log.error("HTTP server error occurred while retrieving cruise details: {} - {}", statusCode, e.getMessage(), e);
                ResponseEntity.status(statusCode).build();
                return;
            } catch (Exception e) {
                log.error("An error occurred while retrieving cruise details: {}", e.getMessage(), e);
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                return;
            }
            skip += count;
        } while (skip < total);
        saveCruiseDetailsListInDataBase(allCruises);
        ResponseEntity.noContent().build();
    }
    void saveCruiseDetailsListInDataBase(List<Cruise> allCruises) {
        List<CruiseDetailsEntity> cruises = allCruises.stream()
                .map(cruiseDetailsMapper::toCruiseDetailsEntity)
                .toList();
        cruiseDetailsRepository.saveAllAndFlush(cruises);
    }
}