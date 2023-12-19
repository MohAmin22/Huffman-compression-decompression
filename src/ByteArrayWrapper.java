import java.io.Serializable;
import java.util.Arrays;

public class ByteArrayWrapper implements Serializable {

    private final byte[] buffer;

    public ByteArrayWrapper(byte[] buffer) {
        this.buffer = buffer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ByteArrayWrapper other)) return false;
        return Arrays.equals(buffer, other.buffer);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(buffer);
    }

    public byte[] getBuffer() {
        return buffer;
    }
}
