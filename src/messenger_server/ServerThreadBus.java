/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package messenger_server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author khuong pham
 */
public class ServerThreadBus {
    private List<ServerThread> listServerThreads;

    public List<ServerThread> getListServerThreads() {
        return listServerThreads;
    }

    public ServerThreadBus() {
        listServerThreads = new ArrayList<>();
    }

    public void add(ServerThread serverThread){
        listServerThreads.add(serverThread);
    }
    
    public void mutilCastSend(String message){ //like sockets.emit in socket.io
        for(ServerThread serverThread : Messenger_Server.serverThreadBus.getListServerThreads()){
            try {
                serverThread.write(message);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void boardCast(int id, String message){
        for(ServerThread serverThread : Messenger_Server.serverThreadBus.getListServerThreads()){
            if (serverThread.getClientNumber() == id) {
                continue;
            } else {
                try {
                    serverThread.write("send-all-user"+";"+message);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    public int getLength(){
        return listServerThreads.size();
    }
    
    public void sendOnlineList(){
        String res = "";
        List<ServerThread> threadbus = Messenger_Server.serverThreadBus.getListServerThreads();
        for(ServerThread serverThread : threadbus){
            res+=serverThread.getClientNumber()+"-";
        }
        Messenger_Server.serverThreadBus.mutilCastSend("update-online-list"+";"+res);
    }
    public void sendMessageToPersion(int id, String message){
        for(ServerThread serverThread : Messenger_Server.serverThreadBus.getListServerThreads()){
            if(serverThread.getClientNumber()==id){
                try {
                    serverThread.write("message-from-user"+";"+message);
                    break;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    public void remove(int id){
        for(int i=0; i<Messenger_Server.serverThreadBus.getLength(); i++){
            if(Messenger_Server.serverThreadBus.getListServerThreads().get(i).getClientNumber()==id){
                Messenger_Server.serverThreadBus.listServerThreads.remove(i);
            }
        }
    }
}
