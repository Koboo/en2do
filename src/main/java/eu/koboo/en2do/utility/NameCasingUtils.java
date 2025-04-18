package eu.koboo.en2do.utility;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Locale;

@UtilityClass
public class NameCasingUtils {

    private static final List<Character> POSSIBLE_SEPARATORS = List.of(' ', '-', '_');

    public String toCamelCase(String string) {
        return withoutSeparator(string, false);
    }

    public String toPascalCase(String string) {
        return withoutSeparator(string, true);
    }

    private String withoutSeparator(String string, boolean firstCharUpperCase) {
        StringBuilder caseBuilder = new StringBuilder();
        boolean makeNextUpperCase = false;
        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (i == 0) {
                Character firstCharacter;
                if (firstCharUpperCase) {
                    firstCharacter = Character.toUpperCase(character);
                } else {
                    firstCharacter = Character.toLowerCase(character);
                }
                caseBuilder.append(firstCharacter);
                continue;
            }
            if (POSSIBLE_SEPARATORS.contains(character)) {
                makeNextUpperCase = true;
                continue;
            }
            if (makeNextUpperCase) {
                makeNextUpperCase = false;
                caseBuilder.append(Character.toUpperCase(character));
                continue;
            }
            caseBuilder.append(character);
        }
        return caseBuilder.toString();
    }

    public String toSnakeCase(String string) {
        return toSeparator(string, '_');
    }

    public String toKebabCase(String string) {
        return toSeparator(string, '-');
    }

    private String toSeparator(String string, char newSeparator) {
        for (Character allowedSeparator : POSSIBLE_SEPARATORS) {
            string = string.replace(String.valueOf(allowedSeparator), String.valueOf(newSeparator));
        }
        StringBuilder caseBuilder = new StringBuilder();
        boolean previousWasSeparator = false;
        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (i != 0 && character == newSeparator) {
                caseBuilder.append(newSeparator);
                previousWasSeparator = true;
                continue;
            }
            if (i != 0 && Character.isUpperCase(character) && !previousWasSeparator) {
                caseBuilder.append(newSeparator);
            }
            previousWasSeparator = false;
            caseBuilder.append(Character.toLowerCase(character));
        }
        return caseBuilder.toString();
    }

    public String toMacroCase(String string) {
        return toSeparator(string, '_').toUpperCase(Locale.ROOT);
    }

    public String toFlatCase(String string) {
        for (Character allowedSeparator : POSSIBLE_SEPARATORS) {
            string = string.replace(String.valueOf(allowedSeparator), "");
        }
        return string.toLowerCase(Locale.ROOT);
    }
}
