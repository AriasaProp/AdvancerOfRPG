package com.ariasaproject.advancerofrpg;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import com.ariasaproject.advancerofrpg.input.Clipboard;

public class AndroidClipboard implements Clipboard {
    private final ClipboardManager clipboard;

    public AndroidClipboard(Context context) {
        clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public String getContents() {
        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null)
            return null;
        CharSequence text = clip.getItemAt(0).getText();
        if (text == null)
            return null;
        return text.toString();
    }

    @Override
    public void setContents(final String contents) {
        ClipData data = ClipData.newPlainText(contents, contents);
        clipboard.setPrimaryClip(data);
    }

    @Override
    public boolean hasContents() {
        return clipboard.hasPrimaryClip();
    }

}
