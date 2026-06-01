package com.rebatesync.domain.port.output;

import com.rebatesync.domain.model.SyncResult;

public interface NotificationService {

    /**
     * Sends a notification when sync completes successfully.
     *
     * @param syncResult the sync result
     */
    void notifySuccess(SyncResult syncResult);

    /**
     * Sends a notification when sync fails.
     *
     * @param syncResult the sync result
     */
    void notifyFailure(SyncResult syncResult);

    /**
     * Sends a notification with custom message.
     *
     * @param message the notification message
     */
    void notify(String message);
}
