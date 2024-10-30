package eu.koboo.en2do.repository.methods.fields;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Represents multiple field updates, like set, rename or remove.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateBatch {

    /**
     * Creates a UpdateBatch instance from a List of
     *
     * @param fieldUpdateList The given list, which gets wrapped.
     * @return The new created UpdateBatch instance.
     * @see FieldUpdate
     */
    public static UpdateBatch of(Collection<FieldUpdate> fieldUpdateList) {
        return new UpdateBatch(new ArrayList<>()).addAll(fieldUpdateList);
    }

    /**
     * Creates a UpdateBatch instance from an Array of
     *
     * @param fieldUpdateArray The given array, which gets wrapped.
     * @return The new created UpdateBatch instance.
     * @see FieldUpdate
     */
    public static UpdateBatch of(FieldUpdate... fieldUpdateArray) {
        return new UpdateBatch(new ArrayList<>()).addAll(fieldUpdateArray);
    }

    /**
     * Creates a UpdateBatch instance from a single
     *
     * @param fieldUpdate The given instance, which gets wrapped.
     * @return The new created UpdateBatch instance.
     * @see FieldUpdate
     */
    public static UpdateBatch of(FieldUpdate fieldUpdate) {
        return new UpdateBatch(new ArrayList<>()).add(fieldUpdate);
    }

    List<FieldUpdate> updateList;

    /**
     * Adds a single FieldUpdate to the list.
     *
     * @param fieldUpdate The FieldUpdate, which should be added.
     * @return The instance of the UpdateBatch
     */
    public UpdateBatch add(FieldUpdate fieldUpdate) {
        updateList.add(fieldUpdate);
        return this;
    }

    /**
     * Adds an Array of FieldUpdates to the list.
     *
     * @param fieldUpdateArray The array, which should be added.
     * @return The instance of the UpdateBatch
     */
    public UpdateBatch addAll(FieldUpdate... fieldUpdateArray) {
        return addAll(Arrays.asList(fieldUpdateArray));
    }

    /**
     * Adds a List of FieldUpdates to the list.
     *
     * @param fieldUpdateList The array, which should be added.
     * @return The instance of the UpdateBatch
     */
    public UpdateBatch addAll(Collection<FieldUpdate> fieldUpdateList) {
        updateList.addAll(fieldUpdateList);
        return this;
    }
}
