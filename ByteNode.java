package FileZipper;


public class  ByteNode implements Comparable<ByteNode> {
    Byte data;      // to store the character 
    int frequency;  // to store the frequecy of the current charcter 
    ByteNode left;  // left child
    ByteNode right; // right child

    public ByteNode(Byte data,int weight){
        this.data = data;
        this.frequency = weight;
    }

    public int compareTo(ByteNode o){
        return this.frequency - o.frequency;
    }
}

