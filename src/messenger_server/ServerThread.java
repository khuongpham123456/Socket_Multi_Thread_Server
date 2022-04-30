/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package messenger_server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author khuong pham
 */
public class ServerThread implements Runnable{
    private Socket socketOfServer;
    private int clientNumber;
    private BufferedReader is;
    private BufferedWriter os;
    private boolean isClosed;
    String PUBLIC_KEY_STRING = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCbPndbAp25koChNaXO9XfZHLBEVKWedG5c2Inio657AePBaYzYISc2ucXwHDzn+xJsFbthGzyt+CYsnVdrtwpVB3Pv7TpWnj2W2l0yG5vrOjsUERVBaC+6Mk1+RNXRimqxCJDtJTtXeB9/bZGXBe4WcPXUhwIB563JPyAGTyeVnwIDAQAB";
    String PRIVATE_KEY_STRING = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAJs+d1sCnbmSgKE1pc71d9kcsERUpZ50blzYieKjrnsB48FpjNghJza5xfAcPOf7EmwVu2EbPK34JiydV2u3ClUHc+/tOlaePZbaXTIbm+s6OxQRFUFoL7oyTX5E1dGKarEIkO0lO1d4H39tkZcF7hZw9dSHAgHnrck/IAZPJ5WfAgMBAAECgYEAkySI8m0vW+W9H49+wgOtfc6QT6O/esm2lS/0uSkVRqfK3NaTVYNO7LL2JphNLj+t/V43xVmQkQAkBqN3abQLCIR961M4eaBwpLAOQtJKALH+fnsiUCCWwbioO3PTfyOpH3injfLvE4NhyoQeazx+AKSkZyro2CG5U/LBsJJWXzECQQDQ6bYILy2WUuqWKwIbGSwDZPdc4T724PFECzdZki1O1gw6PPhdoasUOt0OZrT0rqJ71YF0MdAeykHMYn2PEWtHAkEAvjwOEIgqtFpe6nGNZDiZ+5i/sV5bxW5o/YQWwf106nxR0CQlqfwevrIJvMDUUKs7QTAeMT+pcWnK7eW3DoB+6QJBAICGeA3a8IHd6yKNvRLszo4cDK6giLsbsnK5L8k0TBmHSCiAIBCCiJy+hgb5GvS5h48F0Emq5645ondaVIKzJbsCQQCx4LXF/4zu1xGpZkQvUj2pZEraLsDg+zxw0PH2smiAWX6mgSY2q+iTpyYzuJrOU040xil1I3Hs+l8l04Y3qS8BAkBIHOR887VNtejYOVcwrUHpcKcccVPAKxsoxBBziOxD0alGHtvop7CU1VVfcnQtZ7Dd1sSj4MNgguW92s0/rXNg";
    PrivateKey privateKey = null;
    PublicKey publicKey = null;

    public BufferedReader getIs() {
        return is;
    }

    public BufferedWriter getOs() {
        return os;
    }

    public int getClientNumber() {
        return clientNumber;
    }

    public ServerThread(Socket socketOfServer, int clientNumber) {
        this.socketOfServer = socketOfServer;
        this.clientNumber = clientNumber;
//        System.out.println("Server thread number " + clientNumber + " Started");
        isClosed = false;
    }

    @Override
    public void run() {
        try {
            privateKey = Hyrid_Encryption.getPrivateKeyRSA(PRIVATE_KEY_STRING);
            publicKey = Hyrid_Encryption.getPublicKeyRSA(PUBLIC_KEY_STRING);
            // Mở luồng vào ra trên Socket tại Server.
            is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));
            
//            System.out.println("Khời động luông mới thành công, ID là: " + clientNumber);
            
            write("get-id" + ";" + this.clientNumber);
            Messenger_Server.serverThreadBus.sendOnlineList();
            Messenger_Server.serverThreadBus.mutilCastSend("global-message"+";"+"---Client "+this.clientNumber+" đã đăng nhập---");
            String message;
            while (!isClosed) {
                message = is.readLine();
                if (message == null) {
                    break;
                }
                HashMap<String, String> readClient = new Gson().fromJson(message, new TypeToken<HashMap<String, String>>() {
                    }.getType());

                String encryptKey = readClient.get("key");
                String valueData = readClient.get("value");
                //Giải mã public key mã hóa dựa trên private key
                String clientKey = Hyrid_Encryption.decryptRSA(encryptKey, privateKey);
                //Giải mã dữ liệu dựa trên public key đã giải mã
                valueData = Hyrid_Encryption.decryptAES(valueData, clientKey);
                
                System.out.println("Server Decript Key: "+ encryptKey);
                System.out.println("Server Decript Value: "+ valueData);
                
                String[] messageSplit = valueData.split(";");
                String data;
                String strSend;
                String send;
                if(messageSplit[0].equals("send-to-global")){
                    data = "Client "+messageSplit[2]+": "+messageSplit[1];
                    //Mã hóa data sử dụng random key client vừa giải mã
                    send = Hyrid_Encryption.encryptAES(data, clientKey);
                    HashMap<String, String> sendClient = new HashMap<>();
                    sendClient.put("key", clientKey);
                    sendClient.put("value", send);
                    strSend = new Gson().toJson(sendClient);
                    System.out.println("Server Encript Value: "+strSend);
                    
                    Messenger_Server.serverThreadBus.boardCast(this.getClientNumber(), strSend);
                }
                if(messageSplit[0].equals("send-to-person")){
                    data = "Client "+ messageSplit[2]+" (tới bạn): "+messageSplit[1];
                    //Mã hóa data sử dụng random key client vừa giải mã
                    send = Hyrid_Encryption.encryptAES(data, clientKey);
                    HashMap<String, String> sendClient = new HashMap<>();
                    sendClient.put("key", clientKey);
                    sendClient.put("value", send);
                    strSend = new Gson().toJson(sendClient);
                    System.out.println("Server Ecript Value: "+strSend);
                    
                    Messenger_Server.serverThreadBus.sendMessageToPersion(Integer.parseInt(messageSplit[3]), strSend);
                }
            }
        } catch (IOException e) {
            isClosed = true;
            Messenger_Server.serverThreadBus.remove(clientNumber);
//            System.out.println(this.clientNumber+" đã thoát");
            Messenger_Server.serverThreadBus.sendOnlineList();
            Messenger_Server.serverThreadBus.mutilCastSend("global-message"+";"+"---Client "+this.clientNumber+" đã thoát---");
        } catch (Exception ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void write(String message) throws IOException{
        os.write(message);
        os.newLine();
        os.flush();
    }
    
    public String executeCommand(String valueData, String clientKey){
        return "";
    }
}
