package de.bentrm.datacat.catalog.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import de.bentrm.datacat.catalog.domain.XtdDictionary;
import de.bentrm.datacat.catalog.domain.XtdMultiLanguageText;
import de.bentrm.datacat.catalog.domain.XtdObject;

import java.util.List;

public interface DictionaryRecordService extends SimpleRecordService<XtdDictionary> {

    @NotNull XtdMultiLanguageText getName(@NotNull XtdDictionary dictionary);

    @NotNull Page<XtdObject> getConcepts(@NotNull XtdDictionary dictionary, @NotNull Pageable pageable);

    @NotNull XtdDictionary attachConcepts(@NotBlank String dictionaryId,
                                          @NotEmpty List<@NotBlank String> conceptIds);
}
