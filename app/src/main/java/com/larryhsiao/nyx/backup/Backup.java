package com.larryhsiao.nyx.backup;

/**
 * Two operation backup object, save/restore.
 */
public interface Backup {
    /**
     * Back up all data.
     */
    void save();

    /**
     * Restore data from this backup.
     */
    void restore();
}
