package de.bentrm.datacat.graphql.fetcher;

import de.bentrm.datacat.catalog.service.CatalogExportService;
import de.bentrm.datacat.catalog.service.value.ExportCatalogRecordsValue;
import de.bentrm.datacat.catalog.service.value.ExportRelationshipsValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor 
public class CatalogExportController {

    private final CatalogExportService service;

   @QueryMapping
   public ExportCatalogRecordsValue findExportCatalogRecords() {
       return service.getExportCatalogRecords();
   }

   @QueryMapping
   public ExportRelationshipsValue findExportCatalogRecordsRelationships() {
       return service.getExportCatalogRecordsRelationships();
   }
}
