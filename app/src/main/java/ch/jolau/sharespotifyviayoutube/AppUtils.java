package ch.jolau.sharespotifyviayoutube;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by ravikumar on 10/20/2014.
 */
public class AppUtils {

    public static void showToast(Context context, String iMessage) {
        Toast.makeText(context, iMessage, Toast.LENGTH_SHORT).show();
    }
}
