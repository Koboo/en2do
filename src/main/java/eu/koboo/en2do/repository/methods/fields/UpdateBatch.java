package eu.koboo.en2do.repository.methods.fields;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents multiple field updates, like set, rename or remove.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateBatch {

    /**
     * Creates a UpdateBatch instance from a List of
     * @see FieldUpdate
     * @param fieldUpdateList The given list, which gets wrapped.
     * @return The new created UpdateBatch instance.
     */
    public static @NotNull UpdateBatch of(@NotNull List<FieldUpdate> fieldUpdateList) {
        return new UpdateBatch(new LinkedList<>()).addAll(fieldUpdateList);
    }

    /**
     * Creates a UpdateBatch instance from an Array of
     * @see FieldUpdate
     * @param fieldUpdateArray The given array, which gets wrapped.
     * @return The new created UpdateBatch instance.
     */
    public static @NotNull UpdateBatch of(@NotNull FieldUpdate... fieldUpdateArray) {
        return new UpdateBatch(new LinkedList<>()).addAll(fieldUpdateArray);
    }

    /**
     * Creates a UpdateBatch instance from a single
     * @see FieldUpdate
     * @param fieldUpdate The given instance, which gets wrapped.
     * @return The new created UpdateBatch instance.
     */
    public static @NotNull UpdateBatch of(@NotNull FieldUpdate fieldUpdate) {
        return new UpdateBatch(new LinkedList<>()).add(fieldUpdate);
    }

    @NotNull
    List<FieldUpdate> updateList;

    /**
     * Adds a single FieldUpdate to the list.
     * @param fieldUpdate The FieldUpdate, which should be added.
     * @return The instance of the UpdateBatch
     */
    public UpdateBatch add(FieldUpdate fieldUpdate) {
        updateList.add(fieldUpdate);
        return this;
    }

    /**
     * Adds an Array of FieldUpdates to the list.
     * @param fieldUpdateArray The array, which should be added.
     * @return The instance of the UpdateBatch
     */
    public UpdateBatch addAll(FieldUpdate... fieldUpdateArray) {
        return addAll(Arrays.asList(fieldUpdateArray));
    }

    /**
     * Adds a List of FieldUpdates to the list.
     * @param fieldUpdateList The array, which should be added.
     * @return The instance of the UpdateBatch
     */
    public UpdateBatch addAll(List<FieldUpdate> fieldUpdateList) {
        updateList.addAll(fieldUpdateList);
        return this;
    }
}
