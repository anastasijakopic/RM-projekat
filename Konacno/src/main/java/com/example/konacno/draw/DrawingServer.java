package com.example.konacno.draw;

import java.io.*;
import java.net.*;
import java.util.*;

public class DrawingServer {
    private static final int PORT = 12345;
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    private static List<String> wordsList = new ArrayList<>(); // Lista za čuvanje reči

    public static void main(String[] args) {
        System.out.println("Server je pokrenut...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                InputStream input = socket.getInputStream();
                out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(input));
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Prijem: " + message);

                    if (message.startsWith("Rijec koju je dobio igrac: ")) {
                        String rijec = message.substring(26).strip(); // izdvoji riječ iz poruke
                        synchronized (wordsList) {
                            wordsList.add(rijec); // Dodaj reč u listu
                        }
                        System.out.println("Nova reč dodata u listu: " + rijec);
                    } else if (message.startsWith("Pokusaj: ")) {
                        String pokusaj = message.substring(9); // iizvuci pokušaj
                        System.out.println("pokusaj: " + pokusaj);
                        //int i=0;
                        String zadnjaRijec = wordsList.get(wordsList.size() - 1).strip(); // riječ za pogađanje
                        System.out.println("zadnjaRijec: " + zadnjaRijec);

                        if (pokusaj.equalsIgnoreCase(zadnjaRijec)) {
                            System.out.println("TACNO!");

                            // pošalji poruku svim klijentima da je riječ pogođena
                            synchronized (clientWriters) {
                                for (PrintWriter writer : clientWriters) {
                                    writer.println("TACNO"); // ssvi klijenti primaju poruku
                                }
                            }
                        } else {
                            System.out.println("NETACNO!");
                            out.println("NETACNO"); // pošalji odgovarajućem klijentu ako je netačno
                        }
                    }

                    // salji svim klijentima sve ostale poruke
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println(message);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }
    }
}
