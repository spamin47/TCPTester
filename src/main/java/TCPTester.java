import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;

public class TCPTester {

    public static void main(String[] args) throws Exception {
        if(args.length<1 || args.length>2){
            System.out.println("Invalid number of arguments.");
            throw new Exception();
        }else if(args.length == 1){ //Create a server socket
            try{
                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
                while(true){
                    Socket ss = serverSocket.accept();
                    InputStreamReader in = new InputStreamReader(ss.getInputStream());
                    BufferedReader br = new BufferedReader(in);
                    String line = br.readLine();
                    System.out.println("From client(" + ss.getPort()+"): "+ line);

                    //Server's response
                    PrintStream out = new PrintStream(ss.getOutputStream());
                    System.out.println("ACKED");
                }


            }catch(IOException e){
                e.printStackTrace();
            }
        }else{
            try{
                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[1]));
                Socket socket = new Socket("localhost",Integer.parseInt(args[0]));

                //Request
                System.out.println("Connected to server. Ready to accept commands.");
                BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
                String command = keyboard.readLine();
                PrintWriter pr = new PrintWriter(socket.getOutputStream(),true);
                pr.println(command);

                //get response from server
                InputStreamReader in = new InputStreamReader(socket.getInputStream());
                BufferedReader bf = new BufferedReader(in);
                String line = bf.readLine();
                System.out.println("Server: " + line);

                socket.close();
                serverSocket.close();
            }catch(IOException e){
                e.printStackTrace();
            }

        }

    }
}
