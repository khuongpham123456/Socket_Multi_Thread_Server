/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author khuong pham
 */
public class Messenger_Server {

    public static volatile ServerThreadBus serverThreadBus;
    public static Socket socketOfServer;
    String PUBLIC_KEY_STRING = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCbPndbAp25koChNaXO9XfZHLBEVKWedG5c2Inio657AePBaYzYISc2ucXwHDzn+xJsFbthGzyt+CYsnVdrtwpVB3Pv7TpWnj2W2l0yG5vrOjsUERVBaC+6Mk1+RNXRimqxCJDtJTtXeB9/bZGXBe4WcPXUhwIB563JPyAGTyeVnwIDAQAB";
    String PRIVATE_KEY_STRING = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAJs+d1sCnbmSgKE1pc71d9kcsERUpZ50blzYieKjrnsB48FpjNghJza5xfAcPOf7EmwVu2EbPK34JiydV2u3ClUHc+/tOlaePZbaXTIbm+s6OxQRFUFoL7oyTX5E1dGKarEIkO0lO1d4H39tkZcF7hZw9dSHAgHnrck/IAZPJ5WfAgMBAAECgYEAkySI8m0vW+W9H49+wgOtfc6QT6O/esm2lS/0uSkVRqfK3NaTVYNO7LL2JphNLj+t/V43xVmQkQAkBqN3abQLCIR961M4eaBwpLAOQtJKALH+fnsiUCCWwbioO3PTfyOpH3injfLvE4NhyoQeazx+AKSkZyro2CG5U/LBsJJWXzECQQDQ6bYILy2WUuqWKwIbGSwDZPdc4T724PFECzdZki1O1gw6PPhdoasUOt0OZrT0rqJ71YF0MdAeykHMYn2PEWtHAkEAvjwOEIgqtFpe6nGNZDiZ+5i/sV5bxW5o/YQWwf106nxR0CQlqfwevrIJvMDUUKs7QTAeMT+pcWnK7eW3DoB+6QJBAICGeA3a8IHd6yKNvRLszo4cDK6giLsbsnK5L8k0TBmHSCiAIBCCiJy+hgb5GvS5h48F0Emq5645ondaVIKzJbsCQQCx4LXF/4zu1xGpZkQvUj2pZEraLsDg+zxw0PH2smiAWX6mgSY2q+iTpyYzuJrOU040xil1I3Hs+l8l04Y3qS8BAkBIHOR887VNtejYOVcwrUHpcKcccVPAKxsoxBBziOxD0alGHtvop7CU1VVfcnQtZ7Dd1sSj4MNgguW92s0/rXNg";
    PrivateKey privateKey = null;
    PublicKey publicKey = null;

    public static void main(String[] args) {
        ServerSocket listener = null;
        serverThreadBus = new ServerThreadBus();
        System.out.println("Server is waiting to accept user...");
        int clientNumber = 0;

        // Mở một ServerSocket tại cổng 7777.
        // Chú ý bạn không thể chọn cổng nhỏ hơn 1023 nếu không là người dùng
        // đặc quyền (privileged users (root)).
        try {
            listener = new ServerSocket(7777);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                10, // corePoolSize
                100, // maximumPoolSize
                10, // thread timeout
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(8) // queueCapacity
        );
        try {
            while (true) {
                // Chấp nhận một yêu cầu kết nối từ phía Client.
                // Đồng thời nhận được một đối tượng Socket tại server.
                socketOfServer = listener.accept();
                ServerThread serverThread = new ServerThread(socketOfServer, clientNumber++);
                serverThreadBus.add(serverThread);
//                System.out.println("Số thread đang chạy là: "+serverThreadBus.getLength());
                executor.execute(serverThread);
                
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                listener.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
