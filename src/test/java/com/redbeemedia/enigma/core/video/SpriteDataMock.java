// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.video;

import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.session.ISession;

import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/** Contains mock classes for Sprites. */
public class SpriteDataMock {

    /** Sleeps a 100 ms. */
    static void sleep100() {
        try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
    }
    /** Wait for the asynchronous sprite fetch to finish. */
    static void waitForSprites(SpriteRepository spriteRepository, long timeout) throws TimeoutException {
        long timoutCounter = 0;
        while(spriteRepository.isLoading()) {
            if (timoutCounter > timeout) { throw new TimeoutException("Sprite fetch did time out."); }
            sleep100();
            timoutCounter += 100;
        }
    }

    static byte[] createSerializedMockImage(int width, int height) throws IOException {
        MockImage mockImage = new MockImage(width, height);
        return mockImage.serialize();
    }

    public static class MockSpriteRepository implements ISpriteRepository {

        public Collection<SpriteData> sprites;
        private ISpriteImageRepository<?> imageRepository;

        @Override
        public void activate(int width, @Nullable ISpriteRepository.MetadataListener metadataListener) {
            if (metadataListener != null) { metadataListener.onDone(sprites); }
        }

        public void activate(@Nullable ISpriteRepository.MetadataListener metadataListener) {
            activate(0, metadataListener);
        }

        @Override
        public void setVTTUrls(Map<Integer, String> vttUrls, ISession session) { }

        @Override
        public void clear() {
            sprites = new ArrayList<>();
        }

        @Override
        @Nullable public SpriteData getSpriteData(ITimelinePosition position) {
            if(sprites == null) { return null; }
            for(SpriteData sprite : sprites) {
                if (sprite.position.beforeOrEqual(position) && position.beforeOrEqual(sprite.position.add(sprite.duration))) {
                    return sprite;
                }
            }
            return null;
        }

        @Override
        public void getSprite(ITimelinePosition position, SpriteListener delegate) {
            delegate.onDone(new MockImage(10,10));
        }

        @Override
        public void getSprite(long milliseconds, SpriteListener delegate) {
            delegate.onDone(new MockImage(10,10));
        }

        @Override
        public @Nullable Collection<Integer> getWidths() { return null; }

        @Override
        public <T> void setImageRepository(ISpriteImageRepository<T> imageRepository) {
            this.imageRepository = imageRepository;
        }
    }

    static class MockImage implements Serializable {

        public int width, height;
        MockImage(int width, int height) {
            this.width = width;
            this.height = height;
        }

        byte [] serialize() throws IOException  {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            return bos.toByteArray();
        }

    }

    static class MockImageRepository extends SpriteImageRepository<MockImage> {

        MockImageRepository(IHttpHandler httpHandler) {
            super(httpHandler);
        }

        @Override
        protected MockImage doDecodeImage(byte[] data) {
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInput in = null;
            try {
                in = new ObjectInputStream(bis);
                return (MockImage) in.readObject();
            } catch(Exception e) { e.printStackTrace(); return null;}
            finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) { e.printStackTrace(); }
            }
        }

        @Override
        protected <T> T doGetSprite(T masterImage, SpriteData.Frame frame) {
            return (T)new MockImage(frame.width, frame.height);
        }

        @Override
        protected <T> void doReleaseImage(T image) { }

    }
}
