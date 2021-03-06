package game.engine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Dustin on 8/25/2015.
 */
public class Texture {

    private Context p_context;
    private Bitmap p_bitmap;

    public Texture(Context context) {
        p_context = context;
        p_bitmap = null;
    }

    public Bitmap getBitmap() {
        return p_bitmap;
    }

    public boolean loadFromAssets(String filename) {
        InputStream istream=null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            istream = p_context.getAssets().open(filename);
            p_bitmap = BitmapFactory.decodeStream(istream, null, options);
            istream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
