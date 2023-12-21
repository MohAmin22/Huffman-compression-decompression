import java.util.BitSet;

public class AppendBitSet implements IAppendBitset{
    private BitSet bitSet = new BitSet();
    private int index = 0; // index of first available bit in bitSet
    @Override
    public IAppendBitset append(boolean bit) {
        if(bit) bitSet.set(index);
        index++;
        return this;
    }

    @Override
    public boolean get(int index) {
        return bitSet.get(index);
    }
}
