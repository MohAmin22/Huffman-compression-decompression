import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Test {
    private final String inputPath = "/home/mohamed/CSED_25/Year_3/Algo/Huffman Compression/input.txt";
    private long fileSizeInBytes;
    private int numberOfBytesPerWord = 2;

    void testSeparator() {
        System.out.println(File.separatorChar);
    }

    void testEquality() {
        byte[] a1 = new byte[]{(byte) 0b01101111, (byte) 0x01};
        byte[] b1 = new byte[]{(byte) 0b01101111, (byte) 0x01};
        System.out.println(Arrays.equals(a1, b1));
        Node a = new Node(a1, 3);
        Node b = new Node(b1, 100);
        System.out.println(a.equals(b));
        byte c = (byte) 0b11;
        System.out.println(c);
    }

    public void setInput() throws Exception { // OK
        Path path = Paths.get(inputPath);
        this.fileSizeInBytes = Files.size(path);
    }

    private BufferedInputStream createInputStream() throws FileNotFoundException { // OK
        FileInputStream inputStream = new FileInputStream(inputPath);
        return new BufferedInputStream(inputStream);
    }

    public Map<ByteArrayWrapper, Long> constructFrequencyMap() throws IOException { //OK
        // Create input stream
        BufferedInputStream bufferedInputStream = createInputStream();
        Map<ByteArrayWrapper, Long> frequencyTable = new HashMap<>();
        //read byte[numberOfBytesPerWord] from input
        byte[] buffer = new byte[numberOfBytesPerWord];
        int bytesRead = 0;
        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
            //put byte[] in frequency table ------------------------------- numberOfBytesPerWord instead of bytesRead in case of errors
            byte[] currentBuffer = Arrays.copyOf(buffer, bytesRead); // To enforce mutability of map keys
            frequencyTable.put(new ByteArrayWrapper(currentBuffer),
                    frequencyTable.getOrDefault(new ByteArrayWrapper(currentBuffer), 0L) + 1L);
        }
        bufferedInputStream.close();
        //return frequency table
        return frequencyTable;
    }

    private PriorityQueue<Node> convertFrequencyTableMapToPriorityQueue(Map<ByteArrayWrapper, Long> frequencyTable) {
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingLong(Node::getFrequency));
        for (Map.Entry<ByteArrayWrapper, Long> entry : frequencyTable.entrySet()) {
            queue.add(new Node(entry.getKey().getBuffer(), entry.getValue()));
        }
        return queue;
    }

    public PriorityQueue<Node> deepCopy(PriorityQueue<Node> queue) {
        PriorityQueue<Node> queueCopy = new PriorityQueue<>(Comparator.comparingLong(Node::getFrequency));
        for (Node node : queue) {
            queueCopy.add(new Node(node.getWord(), node.getFrequency(), node.getLeft(), node.getRight()));
        }
        return queueCopy;
    }

    private Node constructHuffmanTree(final PriorityQueue<Node> queue) {
        // to enforce mutability performs a deep copy of the queue
        PriorityQueue<Node> queueCopy = deepCopy(queue);

        while (queueCopy.size() > 1) {
            Node left = queueCopy.poll();
            Node right = queueCopy.poll();
            assert right != null; // Throws an Exception when the file is so small that fits in 1 or less byte[n]
            Node parent = new Node(null, left.getFrequency() + right.getFrequency(), left, right);
            queueCopy.add(parent);
        }
        System.out.println("hi");
        return queueCopy.poll();
    }

    void testReadFile() throws IOException {
        String inputPath = "/home/mohamed/CSED_25/Year_3/Algo/Huffman Compression/input.txt";
        Path path = Paths.get(inputPath);
        long fileSizeInBytes = Files.size(path);
        BufferedInputStream bufferedInputStream = createInputStream();
        byte[] buffer = new byte[numberOfBytesPerWord];
        bufferedInputStream.read(buffer);
    }

    private List<WordCodePair> constructHuffmanTable(Node root) {
        List<WordCodePair> huffmanTable = new ArrayList<>();
        buildHuffmanTable(huffmanTable, "", root);
        return huffmanTable;
    }

    private void buildHuffmanTable(List<WordCodePair> huffmanTable, String code, Node node) {
        if (node.getLeft() == null && node.getRight() == null) {
            huffmanTable.add(new WordCodePair(node.getWord(), code));
        }
        if (node.getLeft() != null) {
            buildHuffmanTable(huffmanTable, code + "0", node.getLeft());
        }
        if (node.getRight() != null) {
            buildHuffmanTable(huffmanTable, code + "1", node.getRight());
        }
    }

    private FileOutputStream storeHuffmanTable(List<WordCodePair> huffmanTable) throws IOException {
        // Generate output file path
        String inputFileName = this.inputPath.substring(this.inputPath.lastIndexOf(File.separatorChar) + 1);
        String outputFileName = "20011502" + "." + this.numberOfBytesPerWord + "." + inputFileName + "." + "hc";
        String outputPath = this.inputPath.substring(0, this.inputPath.lastIndexOf('/') + 1) + outputFileName;
        // Save the arrayList in the output file
        FileOutputStream fos = new FileOutputStream(outputPath);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(huffmanTable);
        // Flush the ObjectOutputStream to ensure all buffered data is sent to the OS to write into the file

        //test to append an integer to the end of the file
        oos.writeLong(8);
        byte[] arr = new byte[]{0b011, 0b11, 0b111};
        //oos.writeUTF("hi");
        oos.write(arr);
        oos.writeObject(huffmanTable);


        oos.flush();
        return fos;
    }

    private List<WordCodePair> loadHuffmanTable(String filePath) throws IOException, ClassNotFoundException {
        List<WordCodePair> huffmanTable = null;
        FileInputStream fis = new FileInputStream(filePath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        String s;
        long a;
        List<WordCodePair> h2;
        byte[] buf;
        try {
            huffmanTable = (List<WordCodePair>) ois.readObject();
            a = ois.readLong();
            //s = ois.readUTF();
            buf = ois.readNBytes(3);
            h2 = (List<WordCodePair>) ois.readObject();

        } catch (EOFException e) {
            System.out.println("End of file reached");
        }
        return huffmanTable;
    }

    private String binarySearch(List<WordCodePair> huffmanTable, ByteArrayWrapper word) {
        huffmanTable.sort((o1, o2) -> Arrays.compare(o1.getWord(), o2.getWord()));
        int wordCodeIndex = Collections.binarySearch(huffmanTable, new WordCodePair(word.getBuffer(), ""),
                (o1, o2) -> Arrays.compare(o1.getWord(), o2.getWord()));
        assert wordCodeIndex >= 0;
        return wordCodeIndex >= 0 ? huffmanTable.get(wordCodeIndex).getCode() : "Not found";
    }

    void testOutput() throws IOException {
        FileOutputStream fos = new FileOutputStream("/home/mohamed/CSED_25/Year_3/Algo/Huffman Compression/out.txt");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        byte x = (byte) 0b111;
        byte[] r = new byte[]{x};
        for (int i = 0; i < 10; i++) {
            oos.write(r);
        }
        oos.flush();
        FileInputStream fis = new FileInputStream("/home/mohamed/CSED_25/Year_3/Algo/Huffman Compression/out.txt");
        ObjectInputStream ois = new ObjectInputStream(fis);
        for (int i = 0; i < 10; i++) {
            System.out.println(ois.readByte());
        }
//        int a = -1;
//        oos.writeInt(-1);
//        oos.writeInt(1);
//        oos.writeInt(5);
//        oos.writeInt(3);
    }

    void testOutput2() throws IOException {
        FileOutputStream fos = new FileOutputStream("/home/mohamed/CSED_25/Year_3/Algo/Huffman Compression/out.txt");
        BufferedOutputStream bof = new BufferedOutputStream(fos);
        byte x = (byte) 0b111;
        byte[] r = new byte[]{x};
        for (int i = 0; i < 10; i++) {
            byte[] r1 = new byte[]{(byte) (x + i)};
            bof.write(r1);
        }
        bof.flush();
        FileInputStream fis = new FileInputStream("/home/mohamed/CSED_25/Year_3/Algo/Huffman Compression/out.txt");
        BufferedInputStream ois = new BufferedInputStream(fis);
        byte[] b = new byte[1];
        for (int i = 0; i < 10; i++) {
            System.out.println(ois.read(b));
        }
//        int a = -1;
//        oos.writeInt(-1);
//        oos.writeInt(1);
//        oos.writeInt(5);
//        oos.writeInt(3);
    }

    void test() throws IOException, ClassNotFoundException {
        Map<ByteArrayWrapper, Long> frequencyTable = constructFrequencyMap();
        // Construct huffman tree
        PriorityQueue<Node> queue = convertFrequencyTableMapToPriorityQueue(frequencyTable);
        Node root = constructHuffmanTree(queue);
        //Extract huffman table
        List<WordCodePair> huffmanTable = constructHuffmanTable(root);
        FileOutputStream fos = storeHuffmanTable(huffmanTable);
        List<WordCodePair> outcome = loadHuffmanTable("/home/mohamed/CSED_25/Year_3/Algo/Huffman Compression/20011502.2.input.txt.hc");

        // test binary search
        String code = binarySearch(outcome, new ByteArrayWrapper(new byte[]{105, 111}));
        System.out.println(code);

    }

    public void testIntegerToByteArray() {
        int a = 1000;//101
        byte[] b = new byte[4];
        ByteBuffer buff = ByteBuffer.wrap(b);
        buff.putInt(a);
        System.out.println(b[3]);
    }

    public void storeNumberOfBitsWritten() throws IOException {
        String outputPath = "/home/mohamed/CSED_25/Year_3/Algo/Huffman Compression/try_saving_long.txt";
        RandomAccessFile raf = new RandomAccessFile(outputPath, "rw");
        raf.seek(0);
        raf.writeLong(-1L);
    }

    public Node mockTree() {
        Node root = new Node(null, 0L, null, null);
        Node right = new Node(new byte[]{0b01100001}, 0L, null, null);
        Node left = new Node(new byte[]{0b01100010}, 0L, null, null);
        root.setLeft(left);
        root.setRight(right);
        return root;
    }

    private void extractNumberOfBitsWritten(IBitInputStream bitInputStream) throws IOException {
        bitInputStream.fetch();
        bitInputStream.setGetNumberOfBitsWrittenInCompressedFile(3);
    }

    private void extractNumberOfBytesPerWord(IBitInputStream bitInputStream) throws IOException {
        bitInputStream.fetch();
        numberOfBytesPerWord = 1;
    }

    private void extractLastByteArray(IBitInputStream bitInputStream) throws IOException {
        bitInputStream.fetch();
        int lastByteArraySize = 0;
        byte[] lastByteArray = bitInputStream.readNBytes(lastByteArraySize);
    }

    public void testBitSet() {
        BitSet bitSet = new BitSet();
        System.out.println(bitSet.get(100));
    }
}