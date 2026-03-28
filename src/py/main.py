from io import BytesIO
import logging
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from face_service import FaceRecognition
from face_service.ttypes import EncodeRequest, EncodeResponse, FaceInfo

import face_recognition
import numpy as np
from PIL import Image


logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class FaceRecognitionHandler:
    def encode(self, req):
        try:
            image_data = req.image_data
            image = Image.open(BytesIO(image_data))
            img_array = np.array(image)

            locations = face_recognition.face_locations(img_array)
            encodings = face_recognition.face_encodings(img_array, locations)

            faces = []
            for encoding, (top, right, bottom, left) in zip(encodings, locations):
                face_info = FaceInfo(
                    encoding=encoding.tolist(),
                    top=top,
                    right=right,
                    bottom=bottom,
                    left=left
                )
                faces.append(face_info)

            logger.info(f"Detected {len(faces)} face(s)")
            return EncodeResponse(
                code=200,
                faces=faces,
                msg="success" if faces else "No face found"
            )

        except Exception as e:
            logger.error(f"Encode error: {e}")
            return EncodeResponse(
                code=500,
                faces=None,
                msg=str(e)
            )


if __name__ == "__main__":
    HOST = "0.0.0.0"
    PORT = 9090

    from thrift.transport import TSocket, TTransport
    from thrift.protocol import TCompactProtocol
    from thrift.server import TServer

    handler = FaceRecognitionHandler()
    processor = FaceRecognition.Processor(handler)

    transport = TSocket.TServerSocket(host=HOST, port=PORT)
    tfactory = TTransport.TBufferedTransportFactory()
    pfactory = TCompactProtocol.TCompactProtocolFactory()

    server = TServer.TThreadPoolServer(processor, transport, tfactory, pfactory)
    logger.info(f"Face Recognition Service started on {HOST}:{PORT}")
    server.serve()
