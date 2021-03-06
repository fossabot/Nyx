package com.larryhsiao.nyx.core.jots;

import com.silverhetch.clotho.Source;

import java.sql.Connection;

/**
 * Db connection source with initial sql.
 */
public class JotsDb implements Source<Connection> {
    private final Source<Connection> connSource;

    public JotsDb(Source<Connection> connSource) {
        this.connSource = connSource;
    }

    @Override
    public Connection value() {
        try {
            final Connection conn = connSource.value();
            conn.createStatement().executeUpdate(
                // language=H2
                "CREATE TABLE IF NOT EXISTS jots(" +
                    "id integer not null auto_increment," +
                    "content text not null, " +
                    "createdTime timestamp with time zone not null, " +
                    "location geometry, " +
                    "mood varchar not null default '', " +
                    "version integer not null default 1, " +
                    "delete integer not null default 0" +
                    ");"
            );
            return conn;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
