package com.larryhsiao.nyx.sync;

import android.content.Context;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.larryhsiao.nyx.core.attachments.AllAttachments;
import com.larryhsiao.nyx.core.attachments.Attachment;
import com.larryhsiao.nyx.core.attachments.ConstAttachment;
import com.larryhsiao.nyx.core.attachments.NewAttachmentById;
import com.larryhsiao.nyx.core.attachments.QueriedAttachments;
import com.larryhsiao.nyx.core.attachments.UpdateAttachment;
import com.silverhetch.clotho.Action;
import com.silverhetch.clotho.Source;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Action to sync attachments.
 */
public class SyncAttachments implements Action {
    private final Context context;
    private final String uid;
    private final Source<Connection> db;

    public SyncAttachments(Context context, String uid, Source<Connection> db) {
        this.context = context;
        this.uid = uid;
        this.db = db;
    }

    @Override
    public void fire() {
        CollectionReference remoteDb = FirebaseFirestore.getInstance()
            .collection(uid + "/data/attachments");
        remoteDb.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sync(remoteDb, task.getResult());
            }
        });
    }

    private void sync(CollectionReference remoteDb, QuerySnapshot result) {
        Map<Long, Attachment> dbItems = new QueriedAttachments(
            new AllAttachments(db, true)
        ).value().stream().collect(Collectors.toMap(Attachment::id, attachment -> attachment));
        for (QueryDocumentSnapshot remoteItem : result) {
            final Attachment dbItem = dbItems.get(Long.valueOf(remoteItem.getId()));
            if (dbItem == null) {
                newLocalItem(remoteItem);
            } else {
                long remoteVersion = remoteItem.getLong("version");
                if (dbItem.version() < remoteVersion) {
                    updateLocalItem(remoteItem);
                    dbItems.remove(dbItem.id());
                } else if (dbItem.version() == remoteVersion) {
                    dbItems.remove(dbItem.id());
                }
            }
        }
        // new Items or local version is newer
        updateRemoteItem(remoteDb, dbItems.values().iterator());
    }

    private void updateLocalItem(QueryDocumentSnapshot remoteItem) {
        new UpdateAttachment(
            db,
            new ConstAttachment(
                Long.parseLong(remoteItem.getId()),
                remoteItem.getLong("jot_id"),
                remoteItem.getString("uri"),
                remoteItem.getLong("version").intValue(),
                remoteItem.getLong("delete").intValue()
            ), false
        ).fire();
    }

    private void newLocalItem(QueryDocumentSnapshot remoteItem) {
        new NewAttachmentById(
            db,
            new ConstAttachment(
                Long.parseLong(remoteItem.getId()),
                remoteItem.getLong("jot_id"),
                remoteItem.getString("uri"),
                remoteItem.getLong("version").intValue(),
                remoteItem.getLong("delete").intValue()
            )
        ).fire();
    }

    private void updateRemoteItem(CollectionReference remoteDb, Iterator<Attachment> iterator) {
        if (!iterator.hasNext()) {
            new LocalFileSync(context, db, integer -> null).fire(); // for deleted items
            new RemoteFileSync(context, db, uid).fire();
            return;
        }
        Attachment attachment = iterator.next();
        Map<String, Object> data = new HashMap<>();
        data.put("delete", attachment.deleted() ? 1 : 0);
        data.put("version", attachment.version());
        data.put("jot_id", attachment.jotId());
        data.put("uri", attachment.uri());
        remoteDb.document(attachment.id() + "")
            .set(data)
            .addOnCompleteListener(it -> {
                updateRemoteItem(remoteDb, iterator);
            });
    }
}
