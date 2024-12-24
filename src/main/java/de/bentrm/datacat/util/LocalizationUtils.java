package de.bentrm.datacat.util;

import de.bentrm.datacat.catalog.domain.XtdMultiLanguageText;
import de.bentrm.datacat.catalog.domain.XtdText;
import de.bentrm.datacat.catalog.service.MultiLanguageTextRecordService;
import de.bentrm.datacat.catalog.service.TextRecordService;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Nullable;

import org.springframework.stereotype.Component;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public final class LocalizationUtils {

    public static final List<Locale.LanguageRange> DEFAULT_LANGUAGE_RANGE = Locale.LanguageRange
            .parse("de,en-US;q=0.7,en;q=0.3");


    private static MultiLanguageTextRecordService multiLanguageTextRecordService;
    private static TextRecordService textRecordService;

    public LocalizationUtils(MultiLanguageTextRecordService multiLanguageTextRecordService, TextRecordService textRecordService) {
        LocalizationUtils.multiLanguageTextRecordService = multiLanguageTextRecordService;
        LocalizationUtils.textRecordService = textRecordService;
    }

    public static List<Locale.LanguageRange> getPriorityList(Locale... locales) {
        List<Locale.LanguageRange> ranges = new ArrayList<>();

        double priority = 1.0;
        for (Locale locale : locales) {
            ranges.add(new Locale.LanguageRange(locale.toLanguageTag(), priority));
            priority = Math.max(0.0, priority - 0.1);
        }
        return ranges;
    }

    @Nullable
    public static XtdText getTranslation(@NotNull String id) {
        return getTranslation(DEFAULT_LANGUAGE_RANGE, id);
    }

    @Nullable
    public static XtdText getTranslation(@NotNull List<Locale.LanguageRange> priorityList, @NotNull String id) {

        final XtdMultiLanguageText mText = multiLanguageTextRecordService.findByIdWithDirectRelations(id).orElse(null);

        final Map<Locale, XtdText> translationMap = mText.getTexts().stream()
                .map(t -> textRecordService.findByIdWithDirectRelations(t.getId()).orElse(null))
                .filter(Objects::nonNull).collect(Collectors.toMap(XtdText::getLocale, Function.identity()));

        final Set<Locale> locales = translationMap.keySet();

        if (translationMap.isEmpty()) {
            return null;
        }

        final List<Locale> filteredCandidates = Locale.filter(priorityList, locales);
        if (!filteredCandidates.isEmpty()) {
            return translationMap.get(filteredCandidates.get(0));
        }

        Locale lookupCandidate = Locale.lookup(priorityList, locales);
        if (lookupCandidate != null) {
            return translationMap.get(lookupCandidate);
        }

        return translationMap.getOrDefault(Locale.ENGLISH, null);
    }
}
