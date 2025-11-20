package jdplus.toolkit.base.api.data;

import org.jspecify.annotations.Nullable;

/**
 * Defines the ability to get a cause from an empty sequence.
 */
public interface HasEmptyCause extends BaseSeq {

    /**
     * Gets a message explaining why the sequence is empty.
     *
     * @return a message if the sequence is empty, null otherwise
     */
    @Nullable String getEmptyCause();
}
