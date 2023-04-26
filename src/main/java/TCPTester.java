import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.sql.Timestamp;
import java.util.Random;


public class TCPTester {

    public static void main(String[] args) throws Exception {
        if(args.length<1 || args.length>2){
            System.out.println("Invalid number of arguments.");
            throw new Exception();
        }else if(args.length == 1){ //Create a server socket
            try{
                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
                while(true){
                    System.out.println("Listening...");
                    Socket ss = serverSocket.accept();

                    //read size
                    InputStreamReader in = new InputStreamReader(ss.getInputStream());
                    BufferedReader br = new BufferedReader(in);
                    int size = Integer.parseInt(br.readLine());

                    System.out.println("From client(" + ss.getPort()+"): "+ size);

                    //Server's response
                    PrintStream out = new PrintStream(ss.getOutputStream());
                    out.println("ACKED size: " + size);

                    //Listen for byte array
                    ss = serverSocket.accept();
                    DataInputStream dIn = new DataInputStream(ss.getInputStream());
                    System.out.println("From client(" + ss.getPort()+"): "+ dIn);

                    byte[] message = new byte[size]; //well known size
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


            }catch(IOException e){
                e.printStackTrace();
            }
        }else{
            try{
                int port =Integer.parseInt(args[1]);
                int serverPort = Integer.parseInt(args[0]);
                ServerSocket serverSocket = new ServerSocket(port);


                //Request
                System.out.println("Connected to server. Ready to accept commands.");

                while(true){
                    System.out.println("_____________________________\n");
                    int size = 0;
                    int loops = 0;
                    try{
                        size = keyboard_readInt("Enter number of bytes to send:");
                        loops = keyboard_readInt("Enter number of iteration.");
                    }catch(NumberFormatException e){
                        System.out.println("Invalid command. Not a number.");
                        e.printStackTrace();
                        continue;
                    }



                    Socket socket = new Socket("localhost",serverPort);

                    byte[] message = new byte[size];
                    new Random().nextBytes(message); //generate random bytes into the supplied array

                    //send byte array size
                    PrintWriter pr = new PrintWriter(socket.getOutputStream(),true);
                    pr.println(size);

                    //get response from server
                    getServerResponse(socket);

                    //send array of bytes # of times
                    for(int i =0;i<loops;i++){
                        //send bytes
                        socket = new Socket("localhost",serverPort);
                        OutputStream socketOut = socket.getOutputStream();

                        //start time
                        long start = System.currentTimeMillis();

                        socketOut.write(message);

                        //get response from server
                        getServerResponse(socket);

                        //record round trip time and throughput
                        long finish = System.currentTimeMillis();
                        double rtt = finish - start;
                        double throughput = (8.0*size)/(1000*rtt); //throughput in bps
                        System.out.println("Round Trip Time: " + rtt + "ms");
                        System.out.printf("Throughput: %.4fbps\n",throughput);
                    }
//                socket.close();
//                serverSocket.close();
                    }

            }catch(IOException e){
                e.printStackTrace();
            }

        }

    }

    public static String keyboard_readLine() throws IOException {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        return keyboard.readLine();
    }
    public static int keyboard_readInt() throws IOException, NumberFormatException {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        int num = Integer.parseInt(keyboard.readLine());
        return num;
    }
    public static int keyboard_readInt(String statement) throws IOException, NumberFormatException {
        System.out.println(statement);
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
}
