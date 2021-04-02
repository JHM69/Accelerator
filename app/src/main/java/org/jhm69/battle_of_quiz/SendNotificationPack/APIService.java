package org.jhm69.battle_of_quiz.SendNotificationPack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAf3aI5C4:APA91bEpOnZMs-BO9XiTIz3_RK3T7epSJtxN5Ew8oTFYHU8MccBXI9WQz7_aq833GPda06BHiIZFJttWBuOR6kUAl0pLROPgOmPlPSDTiRvfsIM5AwXhkvEptbfVe-6qPjkx490dTxmN"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}

