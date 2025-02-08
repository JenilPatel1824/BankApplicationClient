package client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.*;

public class BankClient
{
    private static final Logger logger = LoggerFactory.getLogger(BankClient.class);


    public static void main(String[] args)
    {

        Map<String,Object> request = new HashMap<>();

        Set<String> validCommands = new HashSet<>();

        validCommands.add("CHECK");

        validCommands.add("DEPOSIT");

        validCommands.add("WITHDRAW");

        validCommands.add("EXIT");

        try
        {
            Scanner sc = new Scanner(System.in);

            System.out.println("Application Started");

            while (true)
            {
                System.out.println("Enter input CHECK, DEPOSIT, WITHDRAW, EXIT: ");

                String command = sc.nextLine().toUpperCase();

                if(!validCommands.contains(command))
                {
                    logger.warn("Invalid command");

                    continue;
                }

                request.put("command", command);

                System.out.print("Enter User ID: ");

                long userId;

                try
                {
                    String stringUserId = sc.nextLine();

                    userId = Long.parseLong(stringUserId);
                }
                catch (NumberFormatException e)
                {
                    logger.error("Please enter valid user ID");

                    continue;
                }

                System.out.print("Enter Account Number: ");

                long accountNumber;

                try
                {
                    String stringAcountNumber = sc.nextLine();

                    accountNumber = Long.parseLong(stringAcountNumber);
                }
                catch (NumberFormatException e)
                {
                    logger.error("Please enter valid Account Number");

                    continue;
                }

                request.put("userId", userId);

                request.put("accountNumber", accountNumber);

                if (command.equals("DEPOSIT") || command.equals("WITHDRAW"))
                {
                    System.out.print("Enter Amount: ");

                    String stringAmount = sc.nextLine();

                    long amount;

                    try
                    {
                        amount = Long.parseLong(stringAmount);
                    }
                    catch (NumberFormatException e)
                    {
                        logger.error("Please enter valid Amount");

                        continue;
                    }

                    request.put("amount", amount);

                }
                logger.info("trying to connect bank server with: "+request);

                Socket socket;

                try
                {
                    socket = new Socket("localhost", 9999);
                }
                catch (ConnectException e)
                {
                    logger.error("Could not connect to server");

                    continue;
                }

                logger.info("Connected to bank server");

                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

                output.reset();

                output.writeObject(request);

                Map<String, Object> response;

                response = (Map<String, Object>) input.readObject();

                logger.info("Got response: "+response);

                if (response.get("status").toString().equals("fail"))
                {
                    logger.info(response.get("message").toString());
                }
                else
                {
                    logger.info(response.get("balance").toString());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

            logger.error(e.getMessage());
        }
    }
}
