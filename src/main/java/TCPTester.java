import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.sql.Timestamp;
import java.util.Random;


public class TCPTester {
   public static final String filename = "test.txt";

    public static void main(String[] args) throws Exception {
        if(args.length<1 || args.length>2){
            System.out.println("Invalid number of arguments.");
            throw new Exception();
        }else if(args.length == 1){ //Create a server socket
            try{
                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
                DatagramSocket dss = new DatagramSocket(Integer.parseInt(args[0]));
                while(true){
                    System.out.println("Listening...");
                    Socket ss = serverSocket.accept();

                    //read size
                    InputStreamReader in = new InputStreamReader(ss.getInputStream());
                    BufferedReader br = new BufferedReader(in);
                    int size = Integer.parseInt(br.readLine());

                    System.out.println("From client(" + ss.getPort()+"): "+ size);

                    //read tcp/udp
                    ss = serverSocket.accept();
                    in = new InputStreamReader(ss.getInputStream());
                    br = new BufferedReader(in);
                    int tcpOrUdp = Integer.parseInt(br.readLine());


                    //Server's response
                    PrintStream out = new PrintStream(ss.getOutputStream());
                    out.println("ACKED size: " + size);

                    byte[] message = new byte[size]; //well known size

                    if(tcpOrUdp == 0){//udp
                        DatagramPacket dPacket = new DatagramPacket(message,size);
                        dss.receive(dPacket);
//                        System.out.println(dPacket.getPort());
//                        String received = new String(dPacket.getData(),0, dPacket.getLength());
//                        dss.send(dPacket);
                    }else{//tcp

                        //Listen for byte array
                        ss = serverSocket.accept();
                        DataInputStream dIn = new DataInputStream(ss.getInputStream());
                        System.out.println("From client(" + ss.getPort()+"): "+ dIn);

                        dIn.readFully(message); //buffer data into byte message

                        //convert message byte into characters and print
    //                    for(byte b: message){
    //                        char c= (char)b;
    //                        System.out.print(c);
    //                    }
    //                    System.out.println();

                        //Server's response
                        out = new PrintStream(ss.getOutputStream());
                        out.println("ACKED message");
                    }


                }


            }catch(IOException e){
                e.printStackTrace();
            }
        }else{//client socket

            try{

                File testFile = new File(filename);


                int port =Integer.parseInt(args[1]);
                int serverPort = Integer.parseInt(args[0]);
                ServerSocket serverSocket = new ServerSocket(port);


                //Request
                System.out.println("Connected to server. Ready to accept commands.");

                while(true){
                    System.out.println("_____________________________\n");
                    int size = 0;
                    int loops = 0;
                    int tcpOrUdp;
                    try{
                        tcpOrUdp = keyboard_readInt("Choose which transport protocol to use. 1 for tcp, 0 for upd.");
                        tcpOrUdp = tcpOrUdp%2;
                        size = keyboard_readInt("Enter number of bytes to send:");
                        loops = keyboard_readInt("Enter number of iteration to send:");
                    }catch(NumberFormatException e){
                        System.out.println("Invalid command. Not a number.");
                        e.printStackTrace();
                        continue;
                    }


                    FileWriter fw = new FileWriter(filename);
                    //iterate sending bytes
                    for(int i =0;i<loops;i++){
                        Socket socket = new Socket("localhost",serverPort);

                        byte[] message = new byte[size];
                        new Random().nextBytes(message); //generate random bytes into the supplied array

                        //send byte array size
                        PrintWriter pr = new PrintWriter(socket.getOutputStream(),true);
                        pr.println(size);

                        //send TCP/UDP option
                        socket = new Socket("localhost",serverPort);
                        pr = new PrintWriter(socket.getOutputStream(),true);
                        pr.println(tcpOrUdp);

                        //get response from server for acked size
                        getServerResponse(socket);
                        long start;
                        if(tcpOrUdp == 1){ //TCP
                            ////send bytes
                            socket = new Socket("localhost",serverPort);
                            OutputStream socketOut = socket.getOutputStream();
                            //start time
                            start = System.nanoTime();
                            socketOut.write(message);
                            //get response from server
                            getServerResponse(socket);
                        }else{ //UDP
                            InetAddress address = InetAddress.getByName("localhost");
                            DatagramSocket dSocket = new DatagramSocket();
                            DatagramPacket packet = new DatagramPacket(message,size,address,serverPort);
                            start = System.nanoTime();
                            dSocket.send(packet);
                        }


                        //record round trip time and throughput
                        long finish = System.nanoTime();
                        double t = getThroughput(start,finish,size);
                        if(Double.isFinite(t)){
                            fw.write((i+1)+", "+ t+"\n");
                        }

                    }
                    fw.close();


                }
//                socket.close();
//                serverSocket.close();
            }catch(IOException e){
                e.printStackTrace();
            }

        }

    }

    public static String keyboard_readLine() throws IOException {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        return keyboard.readLine();
    }
    public static int keyboard_readInt(String s) throws IOException, NumberFormatException {
        System.out.println(s  +" ");
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        int num = Integer.parseInt(keyboard.readLine());
        return num;
    }
    //get response from server
    public static void getServerResponse(Socket s) throws  IOException{
        InputStreamReader in = new InputStreamReader(s.getInputStream());
        BufferedReader bf = new BufferedReader(in);
        String line = bf.readLine();
        System.out.println("Server: " + line);
    }
    public static double getThroughput(long start, long finish,int size){
        System.out.println("Start:" + start + " finish:" + finish);
        double rtt = finish - start;
        double sec = rtt/1_000_000_000;
        double throughput = (8.0*size)/sec; //throughput in bps
        System.out.println("Round Trip Time: " + rtt + "ns");
        System.out.printf("Throughput: %.4fMbps\n",throughput/1000000);
        return throughput;
    }
}
