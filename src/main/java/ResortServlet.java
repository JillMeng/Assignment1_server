import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "ResortServlet", value = "/ResortServlet")
public class ResortServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        Gson gson = new Gson();
        String urlPath = req.getPathInfo();
        try {
            ResponseCode responseCode = new ResponseCode();
            // check we have a URL!
            if (urlPath == null || urlPath.isEmpty()) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                responseCode.setSuccess(false);
                responseCode.setDescription("Missing URL");
            }
            String[] urlParts = urlPath.split("/");
            // and now validate url path and return the response status code
            // (and maybe also some value if input is valid)

            if (!isUrlValid(urlParts)) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                responseCode.setSuccess(false);
                responseCode.setDescription("Server is not found.");
            } else {
                res.setStatus(HttpServletResponse.SC_OK);
                // do any sophisticated processing with urlParts which contains all the url params
                // TODO: process url params in `urlParts`
                responseCode.setSuccess(true);
                responseCode.setDescription("Server is connected");
            }
            res.getOutputStream().print(gson.toJson(responseCode));
            res.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            ResponseCode responseCode = new ResponseCode();
            responseCode.setSuccess(false);
            responseCode.setDescription(e.getMessage());
            res.getOutputStream().print(gson.toJson(responseCode));
            res.getOutputStream().flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    private boolean isUrlValid(String[] urlPath) {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
//  /resorts/{resortID}/seasons/{seasonID}/day/{dayID}/skiers
        if(urlPath.length == 7) {
            for(int i=1; i<7; i+=2) {
                if(!isInteger(urlPath[i])) {
                    return false;
                }
            }
            return urlPath[2].equals("seasons")
                    && urlPath[4].equals("day")
                    && urlPath[6].equals("skiers");
        } else if(urlPath.length == 3) {
//            /resorts/{resortID}/seasons
            return isInteger(urlPath[1]) && urlPath[2].equals("seasons");

        }
        return urlPath.length == 1;
    }

    private boolean isInteger(String s) {
        try {
            Long.parseLong(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        return true;
    }
}
