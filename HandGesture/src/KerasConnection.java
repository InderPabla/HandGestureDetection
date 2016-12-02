import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class KerasConnection implements Runnable{
	ServerSocket server = null;
	Socket socket = null;
	BufferedReader in = null;
	OutputStreamWriter out = null;
	
	int wait = 0;
	public KerasConnection(){
		
	}
	
	@Override
	public void run() {
		System.out.println("h1");
		try {
			server = new ServerSocket(12345);
			socket = server.accept();
			out = new OutputStreamWriter(socket.getOutputStream());
		} catch (IOException e) {
			
		}
		
		//System.out.println("h2");
		
		while(true){
			try {
				System.out.println("h3");
				while (wait == 0){System.out.println(wait+"aa");};
				System.out.println("h4");
				out.write(1);
				in = new BufferedReader(
				        new InputStreamReader(socket.getInputStream()));
				
				String message= in.readLine();
				System.out.println(message);
				//in.close();
				
				wait = 0;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		
	}

}
