import io.swagger.client.model.LiftRide;

public class PostBody {

    private Integer resortID;
    private String seasonID;
    private String dayID;
    private Integer skierID;
    private LiftRide liftRide;

    public PostBody(Integer resortID, String seasonID, String dayID, Integer skierID, LiftRide liftRide) {
        this.resortID = resortID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.skierID = skierID;
        this.liftRide = liftRide;
    }

//    private String key;
//    private LiftRide liftRide;
//
//    public PostBody(String key, LiftRide liftRide) {
//        this.key = key;
//        this.liftRide = liftRide;
//    }
}
