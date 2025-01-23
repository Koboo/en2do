package eu.koboo.en2do.utility;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NameUtils {

    public String toCamelCaseLower(String string) {
        StringBuilder caseBuilder = new StringBuilder();
        boolean makeNextUpperCase = false;
        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (i == 0) {
                caseBuilder.append(Character.toLowerCase(character));
                continue;
            }
            if (character == ' ' || character == '-' || character == '_') {
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

    public String toCamelCaseUpper(String string) {
        StringBuilder caseBuilder = new StringBuilder();
        boolean makeNextUpperCase = false;
        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (i == 0) {
                caseBuilder.append(Character.toUpperCase(character));
                continue;
            }
            if (character == ' ' || character == '-' || character == '_') {
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
        string = string.replace(" ", "_");
        string = string.replace("-", "_");
        StringBuilder caseBuilder = new StringBuilder();
        boolean previousWasUnderscore = false;
        for (int i = 0; i < string.length(); i++) {
            char character = string.charAt(i);
            if (i != 0 && (character == '_')) {
                caseBuilder.append("_");
                previousWasUnderscore = true;
                continue;
            }
            if(i != 0 && Character.isUpperCase(character) && !previousWasUnderscore) {
                caseBuilder.append("_");
            }
            previousWasUnderscore = false;
            caseBuilder.append(Character.toLowerCase(character));
        }
        return caseBuilder.toString();
    }
}
