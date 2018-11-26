
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.StringRpcServer;

public class Sender {

	public static void main(String... args) throws KeyManagementException, 
			NoSuchAlgorithmException, URISyntaxException {

		String uri = "amqp://sbvfmqsr:TyV5Xs3YxndY8n-jqCIM4eDhFQgqM7gW@otter.rmq.cloudamqp.com/sbvfmqsr";
		ConnectionFactory connFactory = new ConnectionFactory();
		connFactory.setUri(uri);
		
		try {
			Connection conn = connFactory.newConnection();
			final Channel ch = conn.createChannel();

			ch.queueDeclare("SimpleGaussElimination", false, false, false, null);
			System.out.println("Waiting for procedure...");
			StringRpcServer server = new StringRpcServer(ch, "SimpleGaussElimination") {
				@Override
				public String handleStringCall(String request) {
					System.out.println("Got request: " + request);
					return runSimpleGaussElimination(getMatrixFromString(request));
				}

				
			};
			server.mainloop();
		} catch (Exception ex) {
			System.err.println("Main thread caught exception: " + ex);
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
	private static String runSimpleGaussElimination(BigDecimal[][] augmentedMatrix) {
		int n = augmentedMatrix.length;
        BigDecimal multiplier;

        for (int k = 0; k < n - 1; k++) {

            for (int i = k + 1; i < n; i++) {

                multiplier = augmentedMatrix[i][k].divide(augmentedMatrix[k][k],
                        10, RoundingMode.DOWN);

                for (int j = k; j < n + 1; j++)
                    augmentedMatrix[i][j] = augmentedMatrix[i][j].subtract(
                            multiplier.multiply(augmentedMatrix[k][j]));
            }
        }

        return getStringSolution(regressiveSubstitution(augmentedMatrix));
	}
	
	private static String getStringSolution(BigDecimal[] solutionArray) {
		
		StringBuilder solution = new StringBuilder();
		
		for	(int i = 0; i < solutionArray.length; i++) {
			solution.append("X"+ (i + 1));
			solution.append(solutionArray[i].setScale(10, RoundingMode.DOWN));
			solution.append("\n");
		}
		
		return solution.toString();
	}
	
	private static BigDecimal[] regressiveSubstitution(BigDecimal[][] Ab) {

        int n = Ab.length;
        BigDecimal accumulator;

        BigDecimal solution[] = new BigDecimal[n];

        try {
            solution[n - 1] = (Ab[n - 1][n]).divide(Ab[n - 1][n - 1],
            		10, RoundingMode.DOWN);

            for (int i = n - 2; i > -1; i--) {
                accumulator = BigDecimal.ZERO;

                for (int p = i + 1; p < n; p++)
                    accumulator = accumulator.add(
                            Ab[i][p].multiply(solution[p]));

                solution[i] = (Ab[i][n].subtract(accumulator)).divide(Ab[i][i],
                		10, RoundingMode.DOWN);
            }
        } catch(ArithmeticException e) {
            System.out.println("Division por cero");
        }

        return solution;
    }
	
	private static BigDecimal[][] getMatrixFromString(String stringMatrix) {
		
		String[] stringMatrixArray = stringMatrix.split("\\|");
		
		int length = Integer.parseInt(stringMatrixArray
				[stringMatrixArray.length - 1]);
		
		BigDecimal[][] augmentedMatrix = new BigDecimal[length][length + 1];
		
		for (int i = 0; i < augmentedMatrix.length; i++) {
			for (int j = 0; j < augmentedMatrix[0].length; j++) {
				augmentedMatrix[i][j] = new BigDecimal(
						stringMatrixArray[j + (i * (length + 1))]);
			}
		}
		
		return augmentedMatrix;
	}
}
