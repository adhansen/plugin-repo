package com.WildernessPlayerAlarm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tracks the state of a user-requested alarm silence
 */
public class SilencedState {
    public boolean isSilenced;
    public Set<Integer> playerIdsAtTimeOfSilence;

    public SilencedState() {
        this.isSilenced = false;
        this.playerIdsAtTimeOfSilence = Collections.checkedSet(new HashSet<>(), Integer.class);
    }

    /**
     * Updates the silence based on the current surrounding players, and returns whether the silence is still
     * in effect.
     */
    public boolean updateAndCheck(Set<Integer> currentlyOffendingPlayers) {
        if (!isSilenced) {
            return false;
        }

        if (playerIdsAtTimeOfSilence.containsAll(currentlyOffendingPlayers)) {
            playerIdsAtTimeOfSilence.retainAll(currentlyOffendingPlayers);
            return true;
        }

        // There's been a new player after the silence, cancel silence to notify user of change.
        isSilenced = false;
        playerIdsAtTimeOfSilence.clear();
        return false;
    }

    public void silence(Set<Integer> currentlyOffendingPlayers) {
        playerIdsAtTimeOfSilence.clear();
        if (currentlyOffendingPlayers.isEmpty()) {
            // If nothing is currently triggering the alarm, then silencing has no meaning.
            isSilenced = false;
            return;
        }

        isSilenced = true;
        playerIdsAtTimeOfSilence.addAll(currentlyOffendingPlayers);
    }
}
