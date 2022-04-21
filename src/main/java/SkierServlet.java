import com.google.gson.Gson;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.swagger.client.model.LiftRide;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {
    private Integer resortID;
    private String seasonID;
    private String dayID;
    private Integer skierID;
    private GenericObjectPool<Channel> pool;
//    private final static String QUEUE_NAME = "QUEUE";
    private static final String EXCHANGE_NAME = "messages";

    public void init(ServletConfig config) throws ServletException {

        // Store the ServletConfig object and log the initialization
        super.init(config);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
//        factory.setUsername("radmin");
//        factory.setPassword("radmin");
//        factory.setHost("ec2-54-186-85-234.us-west-2.compute.amazonaws.com");
//        factory.setPort(5672);

        try {
            final Connection conn = factory.newConnection();
            ChannelFactory channelFactory = new ChannelFactory(conn);
            pool = new GenericObjectPool<>(channelFactory);
            pool.setMaxTotal(100);
            pool.setBlockWhenExhausted(true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
//        Gson gson = new Gson();
        String urlPath = req.getPathInfo();
        try {
            // check we have a URL!
            if (urlPath == null || urlPath.isEmpty()) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            String[] urlParts = urlPath.split("/");

            if (!isUrlValid(urlParts)) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                res.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException, NullPointerException {
        res.setContentType("application/json");
        Gson gson = new Gson();
        String urlPath = req.getPathInfo();
        try {
            // check we have a valid postBody as the URL
            if (urlPath == null || urlPath.isEmpty()) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            String[] urlParts = urlPath.split("/");
            if (!isUrlValid(urlParts)) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                res.setStatus(HttpServletResponse.SC_CREATED);
                LiftRide liftRide = gson.fromJson(req.getReader(), LiftRide.class);

                //method 1
                PostBody postBody = new PostBody(resortID, seasonID, dayID, skierID, liftRide);
                String json = new Gson().toJson(postBody);

                //method 2
                //build keyString
//                StringBuilder keyBuilder = new StringBuilder();
//                keyBuilder.append("resortID: ").append(resortID).append(", ")
//                        .append("seasonID: ").append(seasonID).append(", ")
//                        .append("dayID: ").append(dayID).append(", ")
//                        .append("skierID: ").append(skierID);
//                PostBody postBody = new PostBody(keyBuilder.toString(),liftRide);
//                String json = new Gson().toJson(postBody);

                Channel pooledChannel = pool.borrowObject();
                if(pooledChannel != null) {
                    pooledChannel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
                    pooledChannel.basicPublish(EXCHANGE_NAME, "", null, json.getBytes("UTF-8"));
//                System.out.println(" [x] Sent '" + json + "'");
                    pool.returnObject(pooledChannel);
                } else {
//                    System.out.println("The channel pool is empty!");
                }

            }
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }

    }

    private boolean isUrlValid(String[] urlPath) {
        // TODO: validate the request url path according to the API spec
//        /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
        if(urlPath.length == 8) {
            for(int i=1; i<8; i+=2) {
                if(!isInteger(urlPath[i])) {
                    return false;
                }
            }
            resortID = Integer.parseInt(urlPath[1]);
            seasonID = urlPath[3];
            dayID = urlPath[5];
            skierID = Integer.parseInt(urlPath[7]);
            return urlPath[2].equals("seasons")
                    && urlPath[4].equals("days")
                    && urlPath[6].equals("skiers")
                    && Integer.parseInt(urlPath[5]) <= 366
                    && Integer.parseInt(urlPath[5]) >= 1;
        } else if(urlPath.length == 3) {
//            /skiers/{skierID}/vertical
            skierID = Integer.parseInt(urlPath[1]);
            return isInteger(urlPath[1]) && urlPath[2].equals("vertical");

        }
        return false;
    }

    private boolean isInteger(String s) {
        try {
//            Long.parseLong(s);
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        return true;
    }
}
