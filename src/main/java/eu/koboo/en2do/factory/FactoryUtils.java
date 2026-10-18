package eu.koboo.en2do.factory;

import eu.koboo.en2do.MongoSettings;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryNameInvalidException;
import eu.koboo.en2do.mongodb.exception.repository.RepositoryNameNotFoundException;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.NameConvention;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
class FactoryUtils {

    private static final Pattern COLLECTION_REGEX_NAME = Pattern.compile("^[A-Za-z0-9_]+$");

    static String parseRepositoryName(Class<?> repositoryClass,
                                      MongoSettings mongoSettings) {
        NameConvention nameConvention = mongoSettings.collectionNameConvention();
        String baseName = null;
        if (nameConvention != null) {
            baseName = nameConvention.generate(repositoryClass);
        }
        Collection collectionAnnotation = repositoryClass.getAnnotation(Collection.class);
        if (collectionAnnotation != null) {
            String collectionName = collectionAnnotation.value();
            if (!collectionName.isEmpty()) {
                baseName = collectionAnnotation.value();
            }
        }
        if (baseName == null) {
            baseName = NameConvention.SNAKE_CASE.generate(repositoryClass);
        }
        baseName = baseName.trim();
        if (baseName.isEmpty()) {
            throw new RepositoryNameNotFoundException(repositoryClass);
        }

        String fullName = baseName;
        String collectionPrefix = mongoSettings.collectionPrefix();
        if (collectionPrefix != null && !collectionPrefix.trim().equalsIgnoreCase("")) {
            fullName = collectionPrefix + fullName;
        }

        String collectionSuffix = mongoSettings.collectionSuffix();
        if (collectionSuffix != null && !collectionSuffix.trim().equalsIgnoreCase("")) {
            fullName = fullName + collectionSuffix;
        }

        if (!COLLECTION_REGEX_NAME.matcher(fullName).matches()) {
            throw new RepositoryNameInvalidException(repositoryClass,
                COLLECTION_REGEX_NAME.pattern(), fullName);
        }
        return fullName;
    }
}
