import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BitOutputStream {
    private BufferedOutputStream bitOutputStream;
    private byte buffer = 0;
    private int operationCounter = 0;

    private List<Byte> outputList = new ArrayList<Byte>();
    public BitOutputStream(FileOutputStream fos) {
        this.bitOutputStream = new BufferedOutputStream(fos);
    }
    void writeBit(boolean bit) {
        if(bit) {
            buffer = (byte) (buffer | (1 << operationCounter));
        }
        operationCounter++;
        if(operationCounter == 8) {
            outputList.add(this.buffer);
            buffer = 0;
            operationCounter = 0;
        }
    }
    /*
     It must be called after all bits have been written to ensure that the last byte is written
     in the case of the last byte is not full so the counter != 8, so it won't be saved automatically
    */
    void endWriting(){
        try {
            if(operationCounter != 0) { // buffer != 0 : condition may cause problems if the last byte is supposed to be zero
                this.buffer = (byte) (buffer << (8 - operationCounter));
                //byte[] buffer = new byte[]{this.buffer};
                //bitOutputStream.write(buffer);
                outputList.add(this.buffer);
            }
            save();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    void close() throws IOException {
        this.bitOutputStream.close();
    }
    void flush() throws IOException {
        this.bitOutputStream.flush();
    }
    private void save() throws IOException {
        byte [] saveBuffer = new byte[this.outputList.size()];
        for (int i = 0; i < saveBuffer.length; i++) {
            saveBuffer[i] = this.outputList.get(i);
        }
        bitOutputStream.write(saveBuffer);
    }
}
