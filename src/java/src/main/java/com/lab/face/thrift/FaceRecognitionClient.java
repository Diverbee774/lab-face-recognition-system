package com.lab.face.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FaceRecognitionClient {

    @Value("${thrift.python.host:localhost}")
    private String host;

    @Value("${thrift.python.port:9090}")
    private int port;

    public EncodeResponse encode(EncodeRequest request) throws TException {
        try (TTransport transport = new TSocket(host, port)) {
            transport.open();
            TProtocol protocol = new TCompactProtocol(transport);
            FaceRecognition.Client client = new FaceRecognition.Client(protocol);

            return client.encode(request);
        }
    }
}
