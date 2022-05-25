package com.ariasaproject.advancerofrpg;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

public class AndroidNet extends Net {
    final AndroidApplication app;

    public AndroidNet(AndroidApplication app) {
        this.app = app;
    }

    @Override
    public boolean openURI(String URI) {
        final Uri uri = Uri.parse(URI);
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        PackageManager pm = app.getPackageManager();
        if (pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
            app.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    app.startActivity(intent);
                }
            });
            return true;
        }
        return false;
    }
}
