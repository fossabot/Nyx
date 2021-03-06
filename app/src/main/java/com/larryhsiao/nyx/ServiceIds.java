package com.larryhsiao.nyx;

/**
 * Interface for all Service ids in this app. All available service should reference
 * to this interface for better manage the service ids.
 * <p>
 * Any of two ids should never be same, it will cause service not launching if it does.
 */
public interface ServiceIds {
    /**
     * @see com.larryhsiao.nyx.sync.SyncService
     */
    int SYNC = 1000;

    /**
     * @see com.larryhsiao.nyx.backup.google.DriveBackupService
     */
    int BACKUP_DRIVE = 1001;
}
