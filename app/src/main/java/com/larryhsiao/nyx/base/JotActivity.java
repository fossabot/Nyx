package com.larryhsiao.nyx.base;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.larryhsiao.nyx.JotApplication;
import com.silverhetch.aura.AuraActivity;
import com.silverhetch.aura.fingerprint.Fingerprint;
import com.silverhetch.aura.fingerprint.FingerprintImpl;
import com.silverhetch.clotho.Source;
import com.silverhetch.clotho.storage.MemoryCeres;

import java.sql.Connection;

/**
 * Activity for Jot.
 */
public abstract class JotActivity extends AuraActivity {
    protected Source<Connection> db;
    protected FirebaseRemoteConfig remoteConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JotApplication app = ((JotApplication) getApplicationContext());
        db = app.db;
        remoteConfig = app.remoteConfig;
        remoteConfig.fetchAndActivate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fingerprint fingerprint = new FingerprintImpl(
            FingerprintManagerCompat.from(this), new MemoryCeres()
        );
        fingerprint.enable(true); // enabled by default
        JotApplication app = (JotApplication) getApplicationContext();
        if (fingerprint.isEnabled() && 300000 < System.currentTimeMillis() - app.lastAuthed) {
            Intent intent = new Intent(this, AuthActivity.class);
            startActivity(intent);
        }
    }
}
