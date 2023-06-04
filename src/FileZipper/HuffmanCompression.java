package FileZipper;

import java.util.*;
import java.io.*;

public class HuffmanCompression{
    private static StringBuilder sb = new StringBuilder();
    private static Map<Byte, String> huffmap = new HashMap<>();

    // method for taking the file input and generating the compressed file(zip file)
    public static void compress(String src,String dst){
        try{
            FileInputStream inStream = new FileInputStream(src);
            byte[] b = new byte[inStream.available()];
            //to read the data from inStream into the byte array
            inStream.read(b);

            //call createZip method for compressing data
            byte[] huffmanBytes = createZip(b);

            // to output the compressed data into dest folder
            OutputStream outStream = new FileOutputStream(dst);
            ObjectOutputStream objectOutStream = new ObjectOutputStream(outStream);
            
            // compressed data
            objectOutStream.writeObject(huffmanBytes);
            // huffman table for decompression
            objectOutStream.writeObject(huffmap);

            // close all the file-management objects
            inStream.close();
            objectOutStream.close();
            outStream.close();

        }catch(Exception e){
             e.printStackTrace(); 
        }
    }

    // method to follow thw zipping process
    private static byte[] createZip(byte[] bytes){
        MinPriorityQueue<ByteNode> nodes = getByteNodes(bytes);
        ByteNode root = createHuffmanTree(nodes);
        Map<Byte, String> huffmanCodes = getHuffCodes(root);
        byte[] huffmanCodeBytes = zipBytesWithCodes(bytes, huffmanCodes);
        return huffmanCodeBytes;    
    }

    //  method create the min_priority_queue  of byte nodes
    private static MinPriorityQueue<ByteNode> getByteNodes(byte[] bytes) {
            // this que will have the byteNodes for the characters in the file/string 
            MinPriorityQueue<ByteNode> nodes = new MinPriorityQueue<ByteNode>();
    
            // Map to keep track of the frequency of the character in string/file
            Map<Byte , Integer> tempMap = new HashMap<>();
    
            for(byte b:bytes){
                Integer value = tempMap.get(b);
                //if the character appeared first time in the string put it into map
                if(value == null){
                    tempMap.put(b,1);
                }
                //if the curr character has appered before increment count by 1 
                else{
                    tempMap.put(b,value + 1);
                }
            }
    
            //fucntion to create the nodes queue
            for(Map.Entry<Byte, Integer> entry : tempMap.entrySet()){
                nodes.add(new ByteNode(entry.getKey() , entry.getValue()));
            }
    
            return nodes;
        }
    
    //  method to create the huffman tree  
    private static ByteNode createHuffmanTree(MinPriorityQueue<ByteNode> nodes) {
        while(nodes.len() > 1){
            //pop the smallest two from the min_priority_queue
            ByteNode left = nodes.poll();
            ByteNode right = nodes.poll();

            //add the frequency of the lowest two frquencies popped above and create 
            //new parent node of those charcter nodes
            ByteNode parent = new ByteNode(null,left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;

            //again put the parent in the queue
            nodes.add(parent);
        }

        //the only left node in the queue is the root node of the huffman tree
        return nodes.poll();
    }

    //  method to create the huffamn code of each code
    private static Map<Byte, String> getHuffCodes(ByteNode root) {
        if(root == null)
          return null;
        
        // left subtree
        getHuffCodes(root.left , "0" , sb);
        // right subtree
        getHuffCodes(root.right , "1" , sb);

        return huffmap;
    }

    private static void getHuffCodes(ByteNode node, String code, StringBuilder sb1) {
        StringBuilder sb2 = new StringBuilder(sb1);
        sb2.append(code);
        if (node != null) {
            if (node.data == null) {
                getHuffCodes(node.left, "0", sb2);
                getHuffCodes(node.right, "1", sb2);
            } else
                huffmap.put(node.data, sb2.toString());
        }
    }

    
    // method to compress the bytes of the code
    private static byte[] zipBytesWithCodes(byte[] bytes, Map<Byte, String> huffCodes) {
        StringBuilder strBuilder = new StringBuilder();

        //create the string with the huffman codes...
        for(byte b : bytes){
            strBuilder.append(huffCodes.get(b));
        }

        //calculate the length of the new compressed data
        int len = (strBuilder.length()+7)/8;

        byte[] huffCodeBytes = new byte[len];
        int idx = 0;

        for(int i=0 ; i<strBuilder.length() ; i += 8){
            String strByte;
            if(i+8 > strBuilder.length())
                strByte = strBuilder.substring(i);
            else
                strByte = strBuilder.substring(i,i+8);

            // here we convert the binary string value into the integer and then convert it into te byte code 
            huffCodeBytes[idx] = (byte)Integer.parseInt(strByte , 2);
            idx++;

        }
        return huffCodeBytes;
    }
    

    // ............................................................... DECOMPRESSION OF THE FILE DONE HERE..................................................................................

    public static void decompress(String src,String dst){
        try{
            FileInputStream inStream = new FileInputStream(src);
            ObjectInputStream objectInStream = new ObjectInputStream(inStream);
            byte[] huffmanBytes = (byte[]) objectInStream.readObject();
            Map<Byte, String> huffmanCodes = (Map<Byte, String>) objectInStream.readObject();

            byte[] bytes = decomp(huffmanCodes , huffmanBytes);

            OutputStream outStream = new FileOutputStream(dst);
            outStream.write(bytes);
            inStream.close();
            objectInStream.close();
            outStream.close();
            

        }catch(Exception e){ e.printStackTrace();}
    }

    // decompression method
    private static byte[] decomp(Map<Byte, String> huffmanCodes, byte[] huffmanBytes) {
        StringBuilder sb1 = new StringBuilder();

        for(int i=0 ; i<huffmanBytes.length ; i++){
            byte b = huffmanBytes[i];
            boolean flag  = (i == huffmanBytes.length-1);
            sb1.append(convertbyteInBits(!flag , b));
        }

        //this map is complement of the huffmap here the codes in huffmap are the keys and the key are values
        Map<String , Byte> map = new HashMap<>();
        for(Map.Entry<Byte , String> entry : huffmanCodes.entrySet()){
            map.put(entry.getValue() , entry.getKey());
        }
        
        
        List<Byte> list = new ArrayList<>();
        //loop to convert the compresssed data into original data using the complemented map 
        for(int i=0 ; i<sb1.length() ;){
            int count = 1;
            boolean flag = true;
            Byte b = null;
            //loop til we get the valid charcter in the map
            while(flag){
                String key = sb1.substring(i,i+count);
                b = map.get(key);
                //if we get the valid character in the map stop else continue
                if(b == null)
                    count++;
                else
                    flag = false;
            }

            list.add(b);
            i += count;
        }

        // create the byte array of the decompressed data
        byte b[] = new byte[list.size()];
        for(int i=0 ; i<b.length ; i++){
            b[i] = list.get(i);
        }

        return b;
    }

    // method that converts the byte into bits 
    private static Object convertbyteInBits(boolean flag, byte b) {
        int byte0 = b;

        if(flag)
            byte0 |= 256;
        String str0 = Integer.toBinaryString(byte0);
           
        if (flag || byte0 < 0)
                return str0.substring(str0.length() - 8);
        else return str0;
    }

    public static void main(String args[]){
        decompress("C:\\Users\\ysalu\\OneDrive\\Desktop\\zip.txt" ,"C:\\Users\\ysalu\\OneDrive\\Desktop\\decomp.txt" );
    }
}



