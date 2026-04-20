package ru.yandex.practicum.telemetry.analyzer.integration;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

/**
 * gRPC-клиент для вызова Hub Router.
 */
@Component
public class HubRouterClient {

    private static final Logger log = LoggerFactory.getLogger(HubRouterClient.class);

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub stub;

    public void sendAction(String hubId, String scenarioName, String sensorId, String actionType, Integer value) {
        try {
            DeviceActionProto.Builder action = DeviceActionProto.newBuilder()
                    .setSensorId(sensorId)
                    .setType(ActionTypeProto.valueOf(actionType));
            if (value != null) {
                action.setValue(value);
            }
            long millis = System.currentTimeMillis();
            Timestamp ts = Timestamp.newBuilder()
                    .setSeconds(millis / 1000)
                    .setNanos((int) ((millis % 1000) * 1_000_000))
                    .build();
            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenarioName)
                    .setAction(action.build())
                    .setTimestamp(ts)
                    .build();
            Empty resp = stub.handleDeviceAction(request);
            log.debug("gRPC HubRouter handleDeviceAction ok: hubId={}, scenario={}, sensorId={}, type={}, value={}",
                    hubId, scenarioName, sensorId, actionType, value);
        } catch (Exception e) {
            log.error("Ошибка gRPC вызова HubRouter: hubId={}, scenario={}, sensorId={}, type={}, value={}",
                    hubId, scenarioName, sensorId, actionType, value, e);
        }
    }
}
