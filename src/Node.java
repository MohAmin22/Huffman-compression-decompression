import java.util.Arrays;

public class Node {
    //WordFrequencyPair
    private byte[] word;
    private long frequency;
    private Node left;
    private Node right;

    public Node(byte[] word, long frequency) {
        this.word = word;
        this.frequency = frequency;
    }

    public Node(byte[] word, long frequency, Node left, Node right) {
        this.word = word;
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    public byte[] getWord() {
        return word;
    }

    public void setWord(byte[] word) {
        this.word = word;
    }

    public long getFrequency() {
        return frequency;
    }

    public void setFrequency(long frequency) {
        this.frequency = frequency;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Node node)) return false;
        return Arrays.equals(word, node.word);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(word);
    }
}
