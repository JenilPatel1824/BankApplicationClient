import org.junit.jupiter.api.Test;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

public class BankClientTest
{
    private static final int connections = 100;

    private static final long userID = 1L;

    private static final long acccountNumber = 101L;

    private static final long depositAmount = 100L;

    CountDownLatch latch = new CountDownLatch(connections);

    @Test
    void testMultipleDeposits() throws InterruptedException
    {
        ExecutorService executor = Executors.newFixedThreadPool(10000);

        for (int i = 0; i < connections; i++)
        {
            executor.execute(() -> {
                try (Socket socket = new Socket("localhost", 9999);
                     ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream input = new ObjectInputStream(socket.getInputStream()))
                {
                    Map<String, Object> request = new HashMap<>();

                    request.put("command", "DEPOSIT");

                    request.put("userId", userID);

                    request.put("accountNumber", acccountNumber);

                    request.put("amount", depositAmount);

                    output.writeObject(request);

                    output.flush();

                }
                catch (Exception e)
                {
                    fail("Client connection failed: " + e.getMessage());
                }
                finally
                {
                    latch.countDown();
                }
            });
        }

        latch.await();

        try (Socket socket = new Socket("localhost", 9999);
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream()))
        {
            Map<String, Object> balanceRequest = new HashMap<>();

            balanceRequest.put("command", "CHECK");

            balanceRequest.put("userId", userID);

            balanceRequest.put("accountNumber", acccountNumber);

            output.writeObject(balanceRequest);

            output.flush();

            Map<String, Object> balanceResponse = (Map<String, Object>) input.readObject();

            assertNotNull(balanceResponse, "Balance response should not be null");

            long balance = (long) balanceResponse.get("balance");

            assertEquals(10100, balance, "Final balance should be the expected value after all deposits");
        }
        catch (Exception e)
        {
            fail("Balance check failed: " + e.getMessage());
        }

        executor.shutdown();

        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS), "Test should complete in 30 seconds");
    }
}
