package com.redbeemedia.enigma.core.video;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.redbeemedia.enigma.core.http.IHttpHandler;

/** ISpriteImageRepository implementation for handling Bitmap images. */
public class BitmapImageRepository extends SpriteImageRepository<Bitmap> {

    private final BitmapFactory.Options bitmapFactoryOptions = new BitmapFactory.Options();

    BitmapImageRepository(IHttpHandler httpHandler) {
        super(httpHandler);
        this.bitmapFactoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
    }

    @Override
    protected <T> void doReleaseImage(T image) {
        Bitmap bitmap = (Bitmap)image;
        bitmap.recycle();
    }

    @Override
    protected <T> T doGetSprite(T masterImage, SpriteData.Frame frame) {
        return (T)Bitmap.createBitmap((Bitmap)masterImage, frame.x, frame.y, frame.width, frame.height);
    }

    @Override
    protected Bitmap doDecodeImage(byte []data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length,  bitmapFactoryOptions);
    }

    /** Interface of <code>ISpriteRepository.SpriteListener</code> used for `Bitmap` sprites. */
    public interface BitmapSpriteListener extends ISpriteRepository.SpriteListener<Bitmap> { }
}
