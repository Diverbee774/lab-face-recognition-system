package com.lab.face.controller;

import com.lab.face.thrift.EncodeRequest;
import com.lab.face.thrift.EncodeResponse;
import com.lab.face.thrift.FaceRecognitionClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TestController {

    @Autowired
    private FaceRecognitionClient faceRecognitionClient;

    @PostMapping("/test/encode")
    public String testEncode(@RequestParam("image") MultipartFile image) {
        try {
            byte[] imageData = image.getBytes();
            EncodeRequest request = new EncodeRequest();
            request.setImage_data(imageData);

            EncodeResponse response = faceRecognitionClient.encode(request);

            System.out.println("Code: " + response.getCode());
            System.out.println("Msg: " + response.getMsg());
            System.out.println("Faces count: " + (response.getFaces() != null ? response.getFaces().size() : 0));
            System.out.println("Response: " + response);

            return "Code: " + response.getCode() + ", Msg: " + response.getMsg() + ", Faces: " + (response.getFaces() != null ? response.getFaces().size() : 0);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
