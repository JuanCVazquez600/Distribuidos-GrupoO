from flask import Flask, jsonify, request
import grpc
from proto import usuarios_pb2, usuarios_pb2_grpc

app = Flask(__name__)

def grpc_login(usuario_email, clave):
    channel = grpc.insecure_channel('localhost:9090')
    stub = usuarios_pb2_grpc.UsuarioServiceStub(channel)
    request_proto = usuarios_pb2.LoginRequest(
        usuarioEmail=usuario_email,
        clave=clave
    )
    try:
        response = stub.Login(request_proto)
        return {
            'exito': response.exito,
            'mensaje': response.mensaje,
            'usuario': getattr(response.usuario, 'nombreUsuario', None)
        }
    except grpc.RpcError as e:
        return {'exito': False, 'mensaje': str(e)}

@app.route('/login', methods=['GET'])
def login():
    usuario_email = request.args.get('usuarioEmail')
    clave = request.args.get('clave')
    if not usuario_email or not clave:
        return jsonify({'exito': False, 'mensaje': 'Faltan parámetros'}), 400
    result = grpc_login(usuario_email, clave)
    return jsonify(result)

if __name__ == '__main__':
    app.run(port=5000, debug=True)
