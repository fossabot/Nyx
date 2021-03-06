package com.larryhsiao.nyx.core.tags;

import com.silverhetch.clotho.Action;
import com.silverhetch.clotho.Source;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Action to remove all attached tag from jot.
 */
public class JotTagsRemoval implements Action {
    private final Source<Connection> conn;
    private final long jotId;

    public JotTagsRemoval(Source<Connection> conn, long jotId) {
        this.conn = conn;
        this.jotId = jotId;
    }

    @Override
    public void fire() {
        try (PreparedStatement stmt = conn.value().prepareStatement(
            // language=H2
            "UPDATE tag_jot " +
                "SET DELETE = 1,  VERSION = VERSION + 1" +
                " WHERE jot_id=?"
        )) {
            stmt.setLong(1, jotId);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
