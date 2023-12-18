import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class BitInputStream implements BitInputStreamInf{
    BufferedInputStream bitInputStream;
    private byte buffer = 0;
    private int operationCounter = 0;
    public BitInputStream(FileInputStream fis) {
        this.bitInputStream = new BufferedInputStream(fis);
    }

    @Override
    public boolean readBit() {
        return false;
    }

    @Override
    public byte readByte() {
        return 0;
    }

    @Override
    public byte[] readWord() {
        return new byte[0];
    }
    @Override
    public int readInt(){
        return 0;
    }
    @Override
    public byte[] readNBytes(int n){
        return new byte[0];
    }
}
