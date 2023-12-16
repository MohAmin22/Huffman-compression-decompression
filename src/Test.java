import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Test {
    private final String inputPath = "/home/mohamed/CSED_25/Year_3/Algo/Huffman Compression/input.txt";
    private long fileSizeInBytes;
    private final int numberOfBytesPerWord = 1;

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
        oos.writeInt(5);
        oos.writeUTF("hi");
        oos.writeObject(huffmanTable);


        oos.flush();
        return fos;
    }
    private List<WordCodePair> loadHuffmanTable(String filePath) throws IOException, ClassNotFoundException {
        List<WordCodePair> huffmanTable = null;
        FileInputStream fis = new FileInputStream(filePath);
        ObjectInputStream ois = new ObjectInputStream(fis);
        String s;
        int a;
        List<WordCodePair> h2;
        try {
            huffmanTable = (List<WordCodePair>) ois.readObject();
             a = ois.readInt();
             s = ois.readUTF();
             h2 = (List<WordCodePair>) ois.readObject();

        } catch (EOFException e) {
            System.out.println("End of file reached");
        }
        return huffmanTable;
    }

    void test() throws IOException, ClassNotFoundException {
        Map<ByteArrayWrapper, Long> frequencyTable = constructFrequencyMap();
        // Construct huffman tree
        PriorityQueue<Node> queue = convertFrequencyTableMapToPriorityQueue(frequencyTable);
        Node root = constructHuffmanTree(queue);
        //Extract huffman table
        List<WordCodePair> huffmanTable = constructHuffmanTable(root);
        FileOutputStream fos = storeHuffmanTable(huffmanTable);
        List<WordCodePair> outcome = loadHuffmanTable("/home/mohamed/CSED_25/Year_3/Algo/Huffman Compression/20011502.1.input.txt.hc");
    }
}
//    private Map<byte[], Long> constructFrequencyMap() throws IOException {
//        Map<byte[], Long> frequencyTable = new HashMap<>();
//        //read byte[numberOfBytesPerWord] from input
//        byte[] buffer = new byte[numberOfBytesPerWord];
//        int bytesRead = 0;
//        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
//            //put byte[numberOfBytesPerWord] in frequency table
//            frequencyTable.put(buffer, frequencyTable.getOrDefault(buffer, 0L) + 1);
//        }
//        //return frequency table
//        return frequencyTable;
//    }


//        PriorityQueue<Node> queue = new PriorityQueue<>((o1, o2) -> {
//            return Long.compare(o1.getFrequency(), o2.getFrequency());
//        });