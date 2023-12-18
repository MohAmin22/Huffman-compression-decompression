import java.io.IOException;

public interface IBitOutputStream {
    void writeBit(boolean bit);

    void endWriting();

    void close() throws IOException;

    void flush() throws IOException;

    void save() throws IOException;

    void writeByteArray(byte[] word) throws IOException;

    void writeInt(int length) throws IOException;

    void writeLong(long length) throws IOException;

    long getNumberOfBitsWritten();
}
