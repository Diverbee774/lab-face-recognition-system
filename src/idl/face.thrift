namespace py face_service
namespace java com.lab.face.thrift

struct FaceInfo {
    1: list<double> encoding,
    2: i32 top,
    3: i32 right,
    4: i32 bottom,
    5: i32 left
}

struct EncodeRequest {
    1: required binary image_data
}

struct EncodeResponse {
    1: required i32 code,
    2: optional list<FaceInfo> faces,
    3: required string msg
}

service FaceRecognition {
    EncodeResponse encode(1: EncodeRequest req)
}
