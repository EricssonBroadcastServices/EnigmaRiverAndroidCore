package android.os;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Parcel {
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private ObjectInputStream inputStream = null;

    public static Parcel obtain() {
        return new Parcel();
    }

    public void writeSerializable(Serializable serializable) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(serializable);
            objectOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Serializable readSerializable() {
        try {
            return (Serializable)inputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] marshall() {
        return byteArrayOutputStream.toByteArray();
    }

    public void unmarshall(byte[] data, int offset, int length) {
        byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(data, offset, length);
        try {
            inputStream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}